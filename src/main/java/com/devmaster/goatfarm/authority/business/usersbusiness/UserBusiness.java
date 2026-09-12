package com.devmaster.goatfarm.authority.business.usersbusiness;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.application.ports.out.RolePersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.RefreshSessionPersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.PasswordHashingPort;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.business.bo.AuthorityAccount;
import com.devmaster.goatfarm.authority.business.bo.AuthorityRole;
import com.devmaster.goatfarm.authority.business.mapper.AuthorityBusinessMapper;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
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
    private final PasswordHashingPort passwordHashingPort;
    private final RefreshSessionPersistencePort refreshSessionPersistencePort;
    private final CurrentPrincipalQueryUseCase currentPrincipalQuery;

    public UserBusiness(UserPersistencePort userPort, RolePersistencePort rolePort, AuthorityBusinessMapper authorityBusinessMapper,
                        PasswordHashingPort passwordHashingPort, RefreshSessionPersistencePort refreshSessionPersistencePort,
                        CurrentPrincipalQueryUseCase currentPrincipalQuery) {
        this.userPort = userPort;
        this.rolePort = rolePort;
        this.authorityBusinessMapper = authorityBusinessMapper;
        this.passwordHashingPort = passwordHashingPort;
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

        String encryptedPassword = passwordHashingPort.hash(vo.getPassword());
        Set<String> resolvedRoles = resolveUserRoles(vo);
        AuthorityAccount account = authorityBusinessMapper.toAccount(vo);
        account = new AuthorityAccount(account.id(), account.name(), account.email(), account.cpf(), encryptedPassword, resolvedRoles);
        AuthorityAccount saved = userPort.save(account);
        return authorityBusinessMapper.toResponseVO(saved);
    }

    @Transactional
    public UserResponseVO updateUser(Long userId, UserRequestVO vo) {
        validateUserData(vo, false);

        boolean rolesUpdateRequested = vo.getRoles() != null && !vo.getRoles().isEmpty();
        if (rolesUpdateRequested) {
            requireAdmin("Apenas administradores podem alterar roles de usuários.");
        }

        AuthorityAccount existingUser = userPort.findById(userId)
                .orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));

        if (!existingUser.email().equals(vo.getEmail().trim())) {
            if (userPort.findByEmail(vo.getEmail().trim()).isPresent()) {
                throw new DuplicateEntityException("Já existe outro usuário cadastrado com o email: " + vo.getEmail());
            }
        }

        if (!existingUser.cpf().equals(vo.getCpf().trim())) {
            if (userPort.findByCpf(vo.getCpf().trim()).isPresent()) {
                throw new DuplicateEntityException("Já existe outro usuário cadastrado com o CPF: " + vo.getCpf());
            }
        }

        String encryptedPassword = null;
        if (vo.getPassword() != null && !vo.getPassword().trim().isEmpty()) {
            encryptedPassword = passwordHashingPort.hash(vo.getPassword());
        }

        Set<String> resolvedRoles = null;
        if (rolesUpdateRequested) {
            resolvedRoles = resolveUserRoles(vo);
        }

        String password = existingUser.encodedPassword();
        if (encryptedPassword != null) {
            password = encryptedPassword;
            refreshSessionPersistencePort.revokeAllForUser(existingUser.id(), Instant.now(), "password_changed");
        }
        Set<String> roles = resolvedRoles == null ? existingUser.roles() : resolvedRoles;
        if (resolvedRoles != null) {
            refreshSessionPersistencePort.revokeAllForUser(existingUser.id(), Instant.now(), "roles_changed");
        }
        AuthorityAccount updated = userPort.save(new AuthorityAccount(existingUser.id(), vo.getName(), vo.getEmail(), vo.getCpf(), password, roles));
        return authorityBusinessMapper.toResponseVO(updated);
    }

    @Transactional
    public UserResponseVO getMe() {
        AuthenticatedPrincipal principal = currentPrincipalQuery.requireCurrent();
        AuthorityAccount current = userPort.findById(principal.id())
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

        String encrypted = passwordHashingPort.hash(newPassword);
        userPort.updatePassword(userId, encrypted);
        refreshSessionPersistencePort.revokeAllForUser(userId, Instant.now(), "password_changed_by_admin");
    }

    @Transactional
    public UserResponseVO updateRoles(Long userId, java.util.List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new InvalidArgumentException("roles", "É necessário informar ao menos uma role");
        }

        requireAdmin("Apenas administradores podem alterar roles de usuários.");

        java.util.Set<String> resolved = roles.stream()
                .map(roleName -> rolePort.findByAuthority(roleName)
                        .orElseThrow(() -> new RuntimeException("Role não encontrada: " + roleName)).authority())
                .collect(java.util.stream.Collectors.toSet());

        AuthorityAccount user = userPort.findById(userId)
                .orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));
        refreshSessionPersistencePort.revokeAllForUser(user.id(), Instant.now(), "roles_changed");
        AuthorityAccount saved = userPort.save(new AuthorityAccount(user.id(), user.name(), user.email(), user.cpf(), user.encodedPassword(), resolved));
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

    private Set<String> resolveUserRoles(UserRequestVO vo) {
        if (vo.getRoles() != null && !vo.getRoles().isEmpty()) {
            return vo.getRoles().stream()
                    .map(roleName -> rolePort.findByAuthority(roleName)
                            .orElseThrow(() -> new RuntimeException("Role não encontrada: " + roleName)).authority())
                    .collect(Collectors.toSet());
        } else {
            AuthorityRole defaultRole = rolePort.findByAuthority("ROLE_OPERATOR")
                    .orElseThrow(() -> new RuntimeException("Role padrão ROLE_OPERATOR não encontrada no sistema"));
            return Set.of(defaultRole.authority());
        }
    }

    @Transactional
    public AuthorityAccount findOrCreateUser(UserRequestVO vo) {
        validateUserData(vo, true);
        return userPort.findByEmail(vo.getEmail())
                .orElseGet(() -> {
                    AuthorityAccount account = authorityBusinessMapper.toAccount(vo);
                    Set<String> roles = resolveUserRoles(vo);
                    return userPort.save(new AuthorityAccount(account.id(), account.name(), account.email(), account.cpf(),
                            passwordHashingPort.hash(vo.getPassword()), roles));
                });
    }

    @Transactional(readOnly = true)
    public java.util.Optional<AuthorityAccount> findUserByEmail(String email) {
        return userPort.findByEmail(email);
    }

    private void requireAdmin(String message) {
        if (!currentPrincipalQuery.requireCurrent().hasAuthority("ROLE_ADMIN")) {
            throw new UnauthorizedException(message);
        }
    }
}
