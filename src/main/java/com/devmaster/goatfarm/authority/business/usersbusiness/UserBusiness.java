package com.devmaster.goatfarm.authority.business.usersbusiness;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.dao.RoleDAO;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.mapper.UserMapper;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
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

    public User findByUsername(String username) {
        return userDAO.findUserByUsername(username);
    }

    public UserResponseVO getMe() {
        return userDAO.getMe();
    }

    @Transactional
    public UserResponseVO saveUser(UserRequestVO vo) {
        validateRequiredFields(vo);
        validateUserData(vo);

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
        validateUserData(vo);

        User existingUser = userDAO.findUserEntityById(userId);

        if (!existingUser.getEmail().equals(vo.getEmail().trim())) {
            if (userDAO.findUserByEmail(vo.getEmail().trim()).isPresent()) {
                throw new DuplicateEntityException("Já existe outro usuário cadastrado com o email: " + vo.getEmail());
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

    public UserResponseVO findByEmail(String email) {
        return userDAO.findByEmail(email) != null ? userDAO.findByEmail(email) : null;
    }

    public UserResponseVO findById(Long userId) {
        return userDAO.findById(userId);
    }

    @Transactional(readOnly = true)
    public User getEntityById(Long id) {
        return userDAO.findUserEntityById(id);
    }

    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userDAO.findUserEntityById(id);
    }

    @Transactional
    public void deleteRolesFromOtherUsers(Long adminId) {
        userDAO.deleteRolesFromOtherUsers(adminId);
    }

    @Transactional
    public void deleteOtherUsers(Long adminId) {
        userDAO.deleteOtherUsers(adminId);
    }

    private void validateRequiredFields(UserRequestVO vo) {
        if (vo.getName() == null || vo.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório e não pode estar em branco");
        }
        if (vo.getEmail() == null || vo.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório e não pode estar em branco");
        }
        if (vo.getCpf() == null || vo.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("CPF é obrigatório e não pode estar em branco");
        }
        if (vo.getPassword() == null || vo.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória e não pode estar em branco");
        }
    }

    private void validateUserData(UserRequestVO vo) {
        if (vo.getPassword() != null && vo.getConfirmPassword() != null) {
            if (!vo.getPassword().equals(vo.getConfirmPassword())) {
                throw new IllegalArgumentException("As senhas não coincidem");
            }
        }
        if (vo.getCpf() != null && !vo.getCpf().matches("^\\d{11}$")) {
            throw new IllegalArgumentException("CPF deve conter exatamente 11 dígitos numéricos");
        }
        if (vo.getRoles() != null) {
            for (String role : vo.getRoles()) {
                if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_OPERATOR")) {
                    throw new IllegalArgumentException("Role inválida: " + role + ". Roles válidas: ROLE_ADMIN, ROLE_OPERATOR");
                }
            }
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
                    user.setRoles(resolveUserRoles(vo));
                    user.setPassword(passwordEncoder.encode(vo.getPassword()));
                    return userDAO.save(user);
                });
    }
}
