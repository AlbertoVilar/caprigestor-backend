package com.devmaster.goatfarm.authority.business.usersbusiness;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.dao.UserDAO;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.repository.RoleRepository;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import com.devmaster.goatfarm.config.exceptions.custom.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class UserBusiness {

    private final UserDAO userDAO;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserBusiness(UserDAO userDAO, UserRepository userRepository, 
                       RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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
        // Lógica de negócio: validar campos obrigatórios
        validateRequiredFields(vo);
        
        // Lógica de negócio: validar dados específicos
        validateUserData(vo);
        
        // Lógica de negócio: verificar duplicidade de email
        if (userRepository.findByEmail(vo.getEmail().trim()).isPresent()) {
            throw new DuplicateEntityException(
                "Já existe um usuário cadastrado com o email: " + vo.getEmail());
        }

        // Lógica de negócio: verificar duplicidade de CPF
        if (userRepository.findByCpf(vo.getCpf().trim()).isPresent()) {
            throw new DuplicateEntityException(
                "Já existe um usuário cadastrado com o CPF: " + vo.getCpf());
        }

        // Lógica de negócio: criptografar senha
        String encryptedPassword = passwordEncoder.encode(vo.getPassword());
        
        // Lógica de negócio: resolver roles
        Set<Role> resolvedRoles = resolveUserRoles(vo);
        
        // Delegar operação CRUD para o DAO
        return userDAO.saveUser(vo, encryptedPassword, resolvedRoles);
    }

    @Transactional
    public UserResponseVO updateUser(Long userId, UserRequestVO vo) {
        // Lógica de negócio: validar dados específicos
        validateUserData(vo);
        
        // Lógica de negócio: buscar usuário existente
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));

        // Lógica de negócio: verificar duplicidade de email se alterado
        if (!existingUser.getEmail().equals(vo.getEmail().trim())) {
            if (userRepository.findByEmail(vo.getEmail().trim()).isPresent()) {
                throw new DuplicateEntityException(
                    "Já existe outro usuário cadastrado com o email: " + vo.getEmail());
            }
        }

        // Lógica de negócio: criptografar nova senha se fornecida
        String encryptedPassword = null;
        if (vo.getPassword() != null && !vo.getPassword().trim().isEmpty()) {
            encryptedPassword = passwordEncoder.encode(vo.getPassword());
        }

        // Lógica de negócio: resolver roles se fornecidas
        Set<Role> resolvedRoles = null;
        if (vo.getRoles() != null && !vo.getRoles().isEmpty()) {
            resolvedRoles = resolveUserRoles(vo);
        }

        // Delegar operação CRUD para o DAO
        return userDAO.updateUser(userId, vo, encryptedPassword, resolvedRoles);
    }

    public UserResponseVO findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    public UserResponseVO findById(Long userId) {
        return userDAO.findById(userId);
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
        // Validar se password e confirmPassword são iguais
        if (vo.getPassword() != null && vo.getConfirmPassword() != null) {
            if (!vo.getPassword().equals(vo.getConfirmPassword())) {
                throw new IllegalArgumentException("As senhas não coincidem");
            }
        }
        
        // Validar formato do CPF mais rigorosamente
        if (vo.getCpf() != null && !vo.getCpf().matches("^\\d{11}$")) {
            throw new IllegalArgumentException("CPF deve conter exatamente 11 dígitos numéricos");
        }
        
        // Validar se roles são válidas
        if (vo.getRoles() != null) {
            for (String role : vo.getRoles()) {
                if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_OPERATOR")) {
                    throw new IllegalArgumentException("Role inválida: " + role + ". Roles válidas: ROLE_ADMIN, ROLE_OPERATOR");
                }
            }
        }
    }

    private Set<Role> resolveUserRoles(UserRequestVO vo) {
        User tempUser = new User();
        
        if (vo.getRoles() != null && !vo.getRoles().isEmpty()) {
            vo.getRoles().forEach(roleName -> {
                Optional<Role> optionalRole = roleRepository.findByAuthority(roleName);
                if (optionalRole.isEmpty()) {
                    throw new RuntimeException("Role não encontrada: " + roleName);
                }
                tempUser.addRole(optionalRole.get());
            });
        } else {
            // Atribuir ROLE_OPERATOR por padrão quando nenhuma role é fornecida
            Optional<Role> defaultRole = roleRepository.findByAuthority("ROLE_OPERATOR");
            if (defaultRole.isEmpty()) {
                throw new RuntimeException("Role padrão ROLE_OPERATOR não encontrada no sistema");
            }
            tempUser.addRole(defaultRole.get());
        }
        
        return tempUser.getRoles();
    }
}
