package com.devmaster.goatfarm.authority.business.usersbusiness;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.application.ports.out.RolePersistencePort;
import com.devmaster.goatfarm.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.mapper.UserMapper;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserBusiness implements com.devmaster.goatfarm.application.ports.in.UserManagementUseCase {
    private final UserPersistencePort userPort;
    private final RolePersistencePort rolePort;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserBusiness(UserPersistencePort userPort, RolePersistencePort rolePort, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userPort = userPort;
        this.rolePort = rolePort;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponseVO saveUser(UserRequestVO vo) {
        validateUserData(vo, true);

        if (userPort.findByEmail(vo.getEmail().trim()).isPresent()) {
            throw new DuplicateEntityException("Já existe um usuário cadastrado com o email: " + vo.getEmail());
        }

        if (userPort.findByCpf(vo.getCpf().trim()).isPresent()) {
            throw new DuplicateEntityException("Já existe um usuário cadastrado com o CPF: " + vo.getCpf());
        }

        String encryptedPassword = passwordEncoder.encode(vo.getPassword());
        Set<Role> resolvedRoles = resolveUserRoles(vo);

        User user = userMapper.toEntity(vo);
        user.setPassword(encryptedPassword);
        user.getRoles().clear();
        user.getRoles().addAll(resolvedRoles);
        User saved = userPort.save(user);
        return userMapper.toResponseVO(saved);
    }

    @Transactional
    public UserResponseVO updateUser(Long userId, UserRequestVO vo) {
        validateUserData(vo, false);

        User existingUser = userPort.findById(userId)
                .orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));

        if (!existingUser.getEmail().equals(vo.getEmail().trim())) {
            if (userPort.findByEmail(vo.getEmail().trim()).isPresent()) {
                throw new DuplicateEntityException("Já existe outro usuário cadastrado com o email: " + vo.getEmail());
            }
        }

        if (!existingUser.getCpf().equals(vo.getCpf().trim())) {
            if (userPort.findByCpf(vo.getCpf().trim()).isPresent()) {
                throw new DuplicateEntityException("Já existe outro usuário cadastrado com o CPF: " + vo.getCpf());
            }
        }

        String encryptedPassword = null;
        if (vo.getPassword() != null && !vo.getPassword().trim().isEmpty()) {
            encryptedPassword = passwordEncoder.encode(vo.getPassword());
        }

        Set<Role> resolvedRoles = null;
        if (vo.getRoles() != null && !vo.getRoles().isEmpty()) {
            resolvedRoles = resolveUserRoles(vo);
        }

        existingUser.setName(vo.getName());
        existingUser.setEmail(vo.getEmail());
        existingUser.setCpf(vo.getCpf());

        if (encryptedPassword != null) {
            existingUser.setPassword(encryptedPassword);
        }

        if (resolvedRoles != null) {
            User current = getAuthenticatedEntity();
            boolean isAdmin = current.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getAuthority()));
            if (!isAdmin) {
                throw new UnauthorizedException("Apenas administradores podem alterar roles de usuários.");
            }
            existingUser.getRoles().clear();
            existingUser.getRoles().addAll(resolvedRoles);
        }

        User updated = userPort.save(existingUser);
        return userMapper.toResponseVO(updated);
    }

    @Transactional
    public UserResponseVO getMe() {
        User current = getAuthenticatedEntity();
        return userMapper.toResponseVO(current);
    }

    @Transactional(readOnly = true)
    public UserResponseVO findByEmail(String email) {
        return userPort.findByEmail(email)
                .map(userMapper::toResponseVO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public UserResponseVO findById(Long userId) {
        return userPort.findById(userId)
                .map(userMapper::toResponseVO)
                .orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));
    }

    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            ValidationError ve = new ValidationError(Instant.now(), 422, "Erro de validação");
            ve.addError("password", "Senha é obrigatória e não pode estar em branco");
            throw new ValidationException(ve);
        }
        String encrypted = passwordEncoder.encode(newPassword);

        User current = getAuthenticatedEntity();
        boolean isAdmin = current.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getAuthority()));
        if (!isAdmin && (current.getId() == null || !current.getId().equals(userId))) {
            throw new UnauthorizedException("Apenas administradores ou o próprio usuário podem atualizar a senha.");
        }

        userPort.updatePassword(userId, encrypted);
    }

    @Transactional
    public UserResponseVO updateRoles(Long userId, java.util.List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            ValidationError ve = new ValidationError(Instant.now(), 422, "Erro de validação");
            ve.addError("roles", "É necessário informar ao menos uma role");
            throw new ValidationException(ve);
        }
        java.util.Set<Role> resolved = roles.stream()
                .map(roleName -> rolePort.findByAuthority(roleName)
                        .orElseThrow(() -> new RuntimeException("Role não encontrada: " + roleName)))
                .collect(java.util.stream.Collectors.toSet());

        User current = getAuthenticatedEntity();
        boolean isAdmin = current.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getAuthority()));
        if (!isAdmin) {
            throw new UnauthorizedException("Apenas administradores podem alterar roles de usuários.");
        }

        User user = userPort.findById(userId)
                .orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));
        user.getRoles().clear();
        user.getRoles().addAll(resolved);
        User saved = userPort.save(user);
        return userMapper.toResponseVO(saved);
    }

    @Transactional
    public void deleteRolesFromOtherUsers(Long adminId) {
        userPort.deleteRolesFromOtherUsers(adminId);
    }

    @Transactional
    public void deleteOtherUsers(Long adminId) {
        userPort.deleteOtherUsers(adminId);
    }

    private void validateUserData(UserRequestVO vo, boolean isCreation) {
        ValidationError validationError = new ValidationError(Instant.now(), 422, "Erro de validação");

        if (isCreation) {
            if (vo.getName() == null || vo.getName().trim().isEmpty()) {
                validationError.addError("name", "Nome é obrigatório e não pode estar em branco");
            }
            if (vo.getEmail() == null || vo.getEmail().trim().isEmpty()) {
                validationError.addError("email", "Email é obrigatório e não pode estar em branco");
            }
            if (vo.getCpf() == null || vo.getCpf().trim().isEmpty()) {
                validationError.addError("cpf", "CPF é obrigatório e não pode estar em branco");
            }
            if (vo.getPassword() == null || vo.getPassword().trim().isEmpty()) {
                validationError.addError("password", "Senha é obrigatória e não pode estar em branco");
            }
        }

        if (vo.getPassword() != null && !vo.getPassword().isEmpty() && vo.getConfirmPassword() != null) {
            if (!vo.getPassword().equals(vo.getConfirmPassword())) {
                validationError.addError("confirmPassword", "As senhas não coincidem");
            }
        }

        if (vo.getCpf() != null && !vo.getCpf().matches("^\\d{11}$")) {
            validationError.addError("cpf", "CPF deve conter exatamente 11 dígitos numéricos");
        }

        if (vo.getRoles() != null) {
            for (String role : vo.getRoles()) {
                if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_OPERATOR") && !role.equals("ROLE_FARM_OWNER") && !role.equals("ROLE_VIEWER")) {
                    validationError.addError("roles", "Role inválida: " + role + ". Roles válidas: ROLE_ADMIN, ROLE_OPERATOR, ROLE_FARM_OWNER, ROLE_VIEWER");
                }
            }
        }

        if (!validationError.getErrors().isEmpty()) {
            throw new ValidationException(validationError);
        }
    }

    private Set<Role> resolveUserRoles(UserRequestVO vo) {
        if (vo.getRoles() != null && !vo.getRoles().isEmpty()) {
            return vo.getRoles().stream()
                    .map(roleName -> rolePort.findByAuthority(roleName)
                            .orElseThrow(() -> new RuntimeException("Role não encontrada: " + roleName)))
                    .collect(Collectors.toSet());
        } else {
            Role defaultRole = rolePort.findByAuthority("ROLE_OPERATOR")
                    .orElseThrow(() -> new RuntimeException("Role padrão ROLE_OPERATOR não encontrada no sistema"));
            return Set.of(defaultRole);
        }
    }

    @Transactional
    public User findOrCreateUser(UserRequestVO vo) {
        validateUserData(vo, true);
        return userPort.findByEmail(vo.getEmail())
                .orElseGet(() -> {
                    User user = userMapper.toEntity(vo);
                    Set<Role> roles = resolveUserRoles(vo);
                    roles.forEach(user::addRole);
                    user.setPassword(passwordEncoder.encode(vo.getPassword()));
                    return userPort.save(user);
                });
    }

    @Transactional(readOnly = true)
    public java.util.Optional<User> findUserByEmail(String email) {
        return userPort.findByEmail(email);
    }

    private User getAuthenticatedEntity() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName();
            return userPort.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado: " + email));
        }
        throw new UnauthorizedException("Usuário não autenticado");
    }
}

