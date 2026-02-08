package com.devmaster.goatfarm.authority.business.usersbusiness;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.application.ports.out.RolePersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.business.mapper.AuthorityBusinessMapper;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserBusiness implements com.devmaster.goatfarm.authority.application.ports.in.UserManagementUseCase {
    private final UserPersistencePort userPort;
    private final RolePersistencePort rolePort;
    private final AuthorityBusinessMapper authorityBusinessMapper;
    private final PasswordEncoder passwordEncoder;

    public UserBusiness(UserPersistencePort userPort, RolePersistencePort rolePort, AuthorityBusinessMapper authorityBusinessMapper, PasswordEncoder passwordEncoder) {
        this.userPort = userPort;
        this.rolePort = rolePort;
        this.authorityBusinessMapper = authorityBusinessMapper;
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

        User user = authorityBusinessMapper.toEntity(vo);
        user.setPassword(encryptedPassword);
        user.getRoles().clear();
        user.getRoles().addAll(resolvedRoles);
        User saved = userPort.save(user);
        return authorityBusinessMapper.toResponseVO(saved);
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
        return authorityBusinessMapper.toResponseVO(updated);
    }

    @Transactional
    public UserResponseVO getMe() {
        User current = getAuthenticatedEntity();
        return authorityBusinessMapper.toResponseVO(current);
    }

    @Transactional(readOnly = true)
    public UserResponseVO findByEmail(String email) {
        return userPort.findByEmail(email)
                .map(authorityBusinessMapper::toResponseVO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public UserResponseVO findById(Long userId) {
        return userPort.findById(userId)
                .map(authorityBusinessMapper::toResponseVO)
                .orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));
    }

    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new InvalidArgumentException("password", "Senha é obrigatória e não pode estar em branco");
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
            throw new InvalidArgumentException("roles", "É necessário informar ao menos uma role");
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
        return authorityBusinessMapper.toResponseVO(saved);
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
        if (isCreation) {
            if (vo.getName() == null || vo.getName().trim().isEmpty()) {
                throw new InvalidArgumentException("name", "Nome é obrigatório e não pode estar em branco");
            }
            if (vo.getEmail() == null || vo.getEmail().trim().isEmpty()) {
                throw new InvalidArgumentException("email", "Email é obrigatório e não pode estar em branco");
            }
            if (vo.getCpf() == null || vo.getCpf().trim().isEmpty()) {
                throw new InvalidArgumentException("cpf", "CPF é obrigatório e não pode estar em branco");
            }
            if (vo.getPassword() == null || vo.getPassword().trim().isEmpty()) {
                throw new InvalidArgumentException("password", "Senha é obrigatória e não pode estar em branco");
            }
        }

        if (vo.getPassword() != null && !vo.getPassword().isEmpty()) {
            if (vo.getConfirmPassword() == null || vo.getConfirmPassword().trim().isEmpty()) {
                throw new InvalidArgumentException("confirmPassword", "Confirmação de senha é obrigatória");
            } else if (!vo.getPassword().equals(vo.getConfirmPassword())) {
                throw new InvalidArgumentException("confirmPassword", "As senhas não coincidem");
            }
        }

        if (vo.getCpf() != null && !vo.getCpf().matches("^\\d{11}$")) {
            throw new InvalidArgumentException("cpf", "CPF deve conter exatamente 11 dígitos numéricos");
        }

        if (vo.getRoles() != null) {
            for (String role : vo.getRoles()) {
                if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_OPERATOR") && !role.equals("ROLE_FARM_OWNER") && !role.equals("ROLE_VIEWER")) {
                    throw new BusinessRuleException("roles", "Role inválida: " + role + ". Roles válidas: ROLE_ADMIN, ROLE_OPERATOR, ROLE_FARM_OWNER, ROLE_VIEWER");
                }
            }
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
                    User user = authorityBusinessMapper.toEntity(vo);
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
