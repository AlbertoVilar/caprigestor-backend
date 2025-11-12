package com.devmaster.goatfarm.authority.business.usersbusiness;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.dao.RoleDAO;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.mapper.UserMapper;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserBusiness {

    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserBusiness(UserDAO userDAO, RoleDAO roleDAO, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // ... (outros métodos mantidos como estão)

    @Transactional
    public UserResponseVO saveUser(UserRequestVO vo) {
        validateUserData(vo, true);

        if (userDAO.findUserByEmail(vo.getEmail().trim()).isPresent()) {
            throw new DuplicateEntityException("Já existe um usuário cadastrado com o email: " + vo.getEmail());
        }

        if (userDAO.findUserByCpf(vo.getCpf().trim()).isPresent()) {
            throw new DuplicateEntityException("Já existe um usuário cadastrado com o CPF: " + vo.getCpf());
        }

        String encryptedPassword = passwordEncoder.encode(vo.getPassword());
        Set<Role> resolvedRoles = resolveUserRoles(vo);

        return userDAO.saveUser(vo, encryptedPassword, resolvedRoles);
    }

    @Transactional
    public UserResponseVO updateUser(Long userId, UserRequestVO vo) {
        validateUserData(vo, false);

        User existingUser = userDAO.findUserEntityById(userId);

        if (!existingUser.getEmail().equals(vo.getEmail().trim())) {
            if (userDAO.findUserByEmail(vo.getEmail().trim()).isPresent()) {
                throw new DuplicateEntityException("Já existe outro usuário cadastrado com o email: " + vo.getEmail());
            }
        }

        if (!existingUser.getCpf().equals(vo.getCpf().trim())) {
            if (userDAO.findUserByCpf(vo.getCpf().trim()).isPresent()) {
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

        return userDAO.updateUser(userId, vo, encryptedPassword, resolvedRoles);
    }

    @Transactional(readOnly = true)
    public UserResponseVO getMe() {
        return userDAO.getMe();
    }

    @Transactional(readOnly = true)
    public UserResponseVO findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public UserResponseVO findById(Long userId) {
        return userDAO.findById(userId);
    }

    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            ValidationError ve = new ValidationError(Instant.now(), 422, "Erro de validação", ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURI());
            ve.addError("password", "Senha é obrigatória e não pode estar em branco");
            throw new ValidationException(ve);
        }
        String encrypted = passwordEncoder.encode(newPassword);
        userDAO.updateUserPassword(userId, encrypted);
    }

    @Transactional
    public UserResponseVO updateRoles(Long userId, java.util.List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            ValidationError ve = new ValidationError(Instant.now(), 422, "Erro de validação", ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest().getRequestURI());
            ve.addError("roles", "É necessário informar ao menos uma role");
            throw new ValidationException(ve);
        }
        java.util.Set<Role> resolved = roles.stream()
                .map(roleName -> roleDAO.findByAuthority(roleName)
                        .orElseThrow(() -> new RuntimeException("Role não encontrada: " + roleName)))
                .collect(java.util.stream.Collectors.toSet());
        return userDAO.updateUserRoles(userId, resolved);
    }

    @Transactional
    public void deleteRolesFromOtherUsers(Long adminId) {
        userDAO.deleteRolesFromOtherUsers(adminId);
    }

    @Transactional
    public void deleteOtherUsers(Long adminId) {
        userDAO.deleteOtherUsers(adminId);
    }

    private void validateUserData(UserRequestVO vo, boolean isCreation) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        ValidationError validationError = new ValidationError(Instant.now(), 422, "Erro de validação", request.getRequestURI());

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
                if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_OPERATOR")) {
                    validationError.addError("roles", "Role inválida: " + role + ". Roles válidas: ROLE_ADMIN, ROLE_OPERATOR");
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
                    .map(roleName -> roleDAO.findByAuthority(roleName)
                            .orElseThrow(() -> new RuntimeException("Role não encontrada: " + roleName)))
                    .collect(Collectors.toSet());
        } else {
            Role defaultRole = roleDAO.findByAuthority("ROLE_OPERATOR")
                    .orElseThrow(() -> new RuntimeException("Role padrão ROLE_OPERATOR não encontrada no sistema"));
            return Set.of(defaultRole);
        }
    }

    @Transactional
    public User findOrCreateUser(UserRequestVO vo) {
        return userDAO.findUserByEmail(vo.getEmail())
                .orElseGet(() -> {
                    User user = userMapper.toEntity(vo);
                    Set<Role> roles = resolveUserRoles(vo);
                    roles.forEach(user::addRole);
                    user.setPassword(passwordEncoder.encode(vo.getPassword()));
                    return userDAO.save(user);
                });
    }
}
