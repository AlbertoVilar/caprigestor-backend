package com.devmaster.goatfarm.authority.business.usersbusiness;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.application.ports.out.RolePersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.RefreshSessionPersistencePort;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
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
    private final RefreshSessionPersistencePort refreshSessionPersistencePort;
    private final CurrentPrincipalQueryUseCase currentPrincipalQuery;

    public UserBusiness(UserPersistencePort userPort, RolePersistencePort rolePort, AuthorityBusinessMapper authorityBusinessMapper,
                        PasswordEncoder passwordEncoder, RefreshSessionPersistencePort refreshSessionPersistencePort,
                        CurrentPrincipalQueryUseCase currentPrincipalQuery) {
        this.userPort = userPort;
        this.rolePort = rolePort;
        this.authorityBusinessMapper = authorityBusinessMapper;
        this.passwordEncoder = passwordEncoder;
        this.refreshSessionPersistencePort = refreshSessionPersistencePort;
        this.currentPrincipalQuery = currentPrincipalQuery;
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

        boolean rolesUpdateRequested = vo.getRoles() != null && !vo.getRoles().isEmpty();
        if (rolesUpdateRequested) {
            requireAdmin("Apenas administradores podem alterar roles de usuários.");
        }

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
        if (rolesUpdateRequested) {
            resolvedRoles = resolveUserRoles(vo);
        }

        existingUser.setName(vo.getName());
        existingUser.setEmail(vo.getEmail());
        existingUser.setCpf(vo.getCpf());

        if (encryptedPassword != null) {
            existingUser.setPassword(encryptedPassword);
            refreshSessionPersistencePort.revokeAllForUser(existingUser.getId(), Instant.now(), "password_changed");
        }

        if (resolvedRoles != null) {
            existingUser.getRoles().clear();
            existingUser.getRoles().addAll(resolvedRoles);
            refreshSessionPersistencePort.revokeAllForUser(existingUser.getId(), Instant.now(), "roles_changed");
        }

        User updated = userPort.save(existingUser);
        return authorityBusinessMapper.toResponseVO(updated);
    }

    @Transactional
    public UserResponseVO getMe() {
        AuthenticatedPrincipal principal = currentPrincipalQuery.requireCurrent();
        User current = userPort.findById(principal.id())
                .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado: " + principal.email()));
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

        requireAdmin("Apenas administradores podem atualizar senhas pela API administrativa.");

        String encrypted = passwordEncoder.encode(newPassword);
        userPort.updatePassword(userId, encrypted);
        refreshSessionPersistencePort.revokeAllForUser(userId, Instant.now(), "password_changed_by_admin");
    }

    @Transactional
    public UserResponseVO updateRoles(Long userId, java.util.List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new InvalidArgumentException("roles", "É necessário informar ao menos uma role");
        }

        requireAdmin("Apenas administradores podem alterar roles de usuários.");

        java.util.Set<Role> resolved = roles.stream()
                .map(roleName -> rolePort.findByAuthority(roleName)
                        .orElseThrow(() -> new RuntimeException("Role não encontrada: " + roleName)))
                .collect(java.util.stream.Collectors.toSet());

        User user = userPort.findById(userId)
                .orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));
        user.getRoles().clear();
        user.getRoles().addAll(resolved);
        refreshSessionPersistencePort.revokeAllForUser(user.getId(), Instant.now(), "roles_changed");
        User saved = userPort.save(user);
        return authorityBusinessMapper.toResponseVO(saved);
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

    private void requireAdmin(String message) {
        if (!currentPrincipalQuery.requireCurrent().hasAuthority("ROLE_ADMIN")) {
            throw new UnauthorizedException(message);
        }
    }
}
