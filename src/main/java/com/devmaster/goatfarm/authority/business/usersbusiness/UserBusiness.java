package com.devmaster.goatfarm.authority.business.usersbusiness;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.dao.RoleDAO;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class UserBusiness {

    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    private final PasswordEncoder passwordEncoder;

    public UserBusiness(UserDAO userDAO, RoleDAO roleDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.roleDAO = roleDAO;
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
            throw new DuplicateEntityException("JÃ¡ existe um usuÃ¡rio cadastrado com o email: " + vo.getEmail());
        }

        if (userDAO.findUserByCpf(vo.getCpf().trim()).isPresent()) {
            throw new DuplicateEntityException("JÃ¡ existe um usuÃ¡rio cadastrado com o CPF: " + vo.getCpf());
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
                throw new DuplicateEntityException("JÃ¡ existe outro usuÃ¡rio cadastrado com o email: " + vo.getEmail());
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
            throw new IllegalArgumentException("Nome Ã© obrigatÃ³rio e nÃ£o pode estar em branco");
        }
        
        if (vo.getEmail() == null || vo.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email Ã© obrigatÃ³rio e nÃ£o pode estar em branco");
        }
        
        if (vo.getCpf() == null || vo.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("CPF Ã© obrigatÃ³rio e nÃ£o pode estar em branco");
        }
        
        if (vo.getPassword() == null || vo.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Senha Ã© obrigatÃ³ria e nÃ£o pode estar em branco");
        }
    }
    
    private void validateUserData(UserRequestVO vo) {
        if (vo.getPassword() != null && vo.getConfirmPassword() != null) {
            if (!vo.getPassword().equals(vo.getConfirmPassword())) {
                throw new IllegalArgumentException("As senhas nÃ£o coincidem");
            }
        }
        
        if (vo.getCpf() != null && !vo.getCpf().matches("^\\d{11}$")) {
            throw new IllegalArgumentException("CPF deve conter exatamente 11 dÃ­gitos numÃ©ricos");
        }
        
        if (vo.getRoles() != null) {
            for (String role : vo.getRoles()) {
                if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_OPERATOR")) {
                    throw new IllegalArgumentException("Role invÃ¡lida: " + role + ". Roles vÃ¡lidas: ROLE_ADMIN, ROLE_OPERATOR");
                }
            }
        }
    }

    private Set<Role> resolveUserRoles(UserRequestVO vo) {
        User tempUser = new User();
        
        if (vo.getRoles() != null && !vo.getRoles().isEmpty()) {
            vo.getRoles().forEach(roleName -> {
                Optional<Role> optionalRole = roleDAO.findByAuthority(roleName);
                if (optionalRole.isEmpty()) {
                    throw new RuntimeException("Role nÃ£o encontrada: " + roleName);
                }
                tempUser.addRole(optionalRole.get());
            });
        } else {
            Optional<Role> defaultRole = roleDAO.findByAuthority("ROLE_OPERATOR");
            if (defaultRole.isEmpty()) {
                throw new RuntimeException("Role padrÃ£o ROLE_OPERATOR nÃ£o encontrada no sistema");
            }
            tempUser.addRole(defaultRole.get());
        }
        
        return tempUser.getRoles();
    }

    @Transactional
    public User findOrCreateUser(UserRequestVO vo) {
        Optional<User> existingUser = userDAO.findUserByEmail(vo.getEmail());
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        User user = new User();
        user.setName(vo.getName());
        user.setEmail(vo.getEmail());
        user.setCpf(vo.getCpf());

        user.getRoles().clear();
        if (vo.getRoles() != null && !vo.getRoles().isEmpty()) {
            for (String roleName : vo.getRoles()) {
                Optional<Role> optionalRole = roleDAO.findByAuthority(roleName);
                if (optionalRole.isEmpty()) {
                    throw new RuntimeException("Role nÃ£o encontrada: " + roleName);
                }
                user.addRole(optionalRole.get());
            }
        } else {
            Optional<Role> defaultRole = roleDAO.findByAuthority("ROLE_OPERATOR");
            if (defaultRole.isEmpty()) {
                throw new RuntimeException("Role padrÃ£o ROLE_OPERATOR nÃ£o encontrada no sistema");
            }
            user.addRole(defaultRole.get());
        }

        user.setPassword(passwordEncoder.encode(vo.getPassword()));

        return userDAO.save(user);
    }
}
