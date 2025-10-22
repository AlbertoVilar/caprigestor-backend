package com.devmaster.goatfarm.authority.dao;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.conveter.UserEntityConverter;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.repository.RoleRepository;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Service
public class UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserDAO(UserRepository repository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    protected User authenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName();
            return repository.findByEmail(email)
                    .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado: " + email));
        }
        throw new UnauthorizedException("Usuário não autenticado");
    }

    @Transactional(readOnly = true)
    public UserResponseVO getMe() {
        User user = authenticated();
        return UserEntityConverter.toVO(user);
    }

    @Transactional(readOnly = true)
    public User findUserByUsername(String username) {
        // Usar o método com @EntityGraph para carregar as roles
        User user = repository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado com email: " + username));
        
        logger.debug("🔍 UserDAO: Usuário carregado: {}", user.getEmail());
        logger.debug("🔍 UserDAO: Roles carregadas: {}", user.getRoles().size());
        user.getRoles().forEach(role -> logger.debug("🔍 UserDAO: Role: {}", role.getAuthority()));
        
        return user;
    }

    @Transactional(readOnly = true)
    public UserResponseVO findByEmail(String email) {
        User user = repository.findByEmail(email)
                .orElse(null);
        
        if (user == null) {
            return null;
        }
        
        return UserEntityConverter.toVO(user);
    }

    @Transactional
    public UserResponseVO saveUser(UserRequestVO vo, String encryptedPassword, Set<Role> resolvedRoles) {
        // Operação CRUD mecânica: converter VO para entidade
        User user = UserEntityConverter.fromVO(vo);
        
        // Aplicar senha criptografada e roles resolvidas pelo Business
        user.setPassword(encryptedPassword);
        user.getRoles().clear();
        user.getRoles().addAll(resolvedRoles);

        try {
            User savedUser = repository.save(user);
            return UserEntityConverter.toVO(savedUser);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.DatabaseException(
                "Erro ao salvar usuário: Violação de integridade dos dados", e);
        }
    }



    @Transactional(readOnly = true)
    public UserResponseVO findById(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Usuário com ID " + userId + " não encontrado."));
        return UserEntityConverter.toVO(user);
    }

    @Transactional
    public UserResponseVO updateUser(Long userId, UserRequestVO vo, String encryptedPassword, Set<Role> resolvedRoles) {
        // Operação CRUD mecânica: buscar usuário existente
        User userToUpdate = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário com ID " + userId + " não encontrado."));

        // Atualizar dados básicos
        userToUpdate.setName(vo.getName());
        userToUpdate.setEmail(vo.getEmail());
        
        // Atualizar senha se fornecida pelo Business
        if (encryptedPassword != null) {
            userToUpdate.setPassword(encryptedPassword);
        }

        // Atualizar roles se fornecidas pelo Business
        if (resolvedRoles != null) {
            userToUpdate.getRoles().clear();
            userToUpdate.getRoles().addAll(resolvedRoles);
        }

        User updatedUser = repository.save(userToUpdate);
        return UserEntityConverter.toVO(updatedUser);
    }

}
