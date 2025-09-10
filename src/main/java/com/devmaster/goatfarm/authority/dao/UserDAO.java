package com.devmaster.goatfarm.authority.dao;

import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.conveter.UserEntityConverter;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.RoleRepository;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserDAO {

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
                    .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado: " + email));
        }
        throw new RuntimeException("Usuário não autenticado");
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
        
        System.out.println("🔍 UserDAO: Usuário carregado: " + user.getEmail());
        System.out.println("🔍 UserDAO: Roles carregadas: " + user.getRoles().size());
        user.getRoles().forEach(role -> System.out.println("🔍 UserDAO: Role: " + role.getAuthority()));
        
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
    public UserResponseVO saveUser(UserRequestVO vo) {
        // Validar campos obrigatórios não nulos e não vazios
        validateRequiredFields(vo);
        
        // Verificar se já existe usuário com o mesmo email
        if (repository.findByEmail(vo.getEmail().trim()).isPresent()) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.DuplicateEntityException(
                "Já existe um usuário cadastrado com o email: " + vo.getEmail());
        }

        // Verificar se já existe usuário com o mesmo CPF
        if (repository.findByCpf(vo.getCpf().trim()).isPresent()) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.DuplicateEntityException(
                "Já existe um usuário cadastrado com o CPF: " + vo.getCpf());
        }

        User user = UserEntityConverter.fromVO(vo);
        
        // Criptografar a senha
        user.setPassword(passwordEncoder.encode(vo.getPassword()));

        // Resolver roles já salvas no banco
        user.getRoles().clear(); // evitar acúmulo ou roles duplicadas
        
        if (vo.getRoles() != null && !vo.getRoles().isEmpty()) {
            vo.getRoles().forEach(roleName -> {
                Optional<Role> optionalRole = roleRepository.findByAuthority(roleName);
                if (optionalRole.isEmpty()) {
                    throw new RuntimeException("Role não encontrada: " + roleName);
                }
                user.addRole(optionalRole.get());
            });
        } else {
            // Atribuir ROLE_OPERATOR por padrão quando nenhuma role é fornecida
            Optional<Role> defaultRole = roleRepository.findByAuthority("ROLE_OPERATOR");
            if (defaultRole.isEmpty()) {
                throw new RuntimeException("Role padrão ROLE_OPERATOR não encontrada no sistema");
            }
            user.addRole(defaultRole.get());
        }

        // Senha já foi criptografada acima na linha 92

        try {
            User savedUser = repository.save(user);
            return UserEntityConverter.toVO(savedUser);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.DatabaseException(
                "Erro ao salvar usuário: Violação de integridade dos dados", e);
        }
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
        
        if (vo.getRoles() == null || vo.getRoles().isEmpty()) {
            throw new IllegalArgumentException("Pelo menos uma role deve ser selecionada");
        }
        
        // Validar formato do CPF (apenas dígitos, 11 caracteres)
        String cpfClean = vo.getCpf().trim().replaceAll("\\D", "");
        if (cpfClean.length() != 11) {
            throw new IllegalArgumentException("CPF deve conter exatamente 11 dígitos numéricos");
        }
        
        // Validar formato do email
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!vo.getEmail().trim().matches(emailRegex)) {
            throw new IllegalArgumentException("Email deve ter formato válido");
        }
        
        // Validar tamanho mínimo da senha
        if (vo.getPassword().length() < 6) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }
        
        // Validar tamanho do nome
        if (vo.getName().trim().length() < 2 || vo.getName().trim().length() > 100) {
            throw new IllegalArgumentException("Nome deve ter entre 2 e 100 caracteres");
        }
    }

    @Transactional
    public UserResponseVO updateUser(Long userId, UserRequestVO vo) {
        User userToUpdate = repository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário com ID " + userId + " não encontrado."));

        // Atualizar dados básicos
        userToUpdate.setName(vo.getName());
        userToUpdate.setEmail(vo.getEmail());
        
        // Atualizar senha apenas se fornecida
        if (vo.getPassword() != null && !vo.getPassword().trim().isEmpty()) {
            userToUpdate.setPassword(passwordEncoder.encode(vo.getPassword())); // Senha criptografada
        }

        // Atualizar roles se fornecidas
        if (vo.getRoles() != null && !vo.getRoles().isEmpty()) {
            userToUpdate.getRoles().clear();
            vo.getRoles().forEach(roleName -> {
                Optional<Role> optionalRole = roleRepository.findByAuthority(roleName);
                if (optionalRole.isEmpty()) {
                    throw new RuntimeException("Role não encontrada: " + roleName);
                }
                userToUpdate.addRole(optionalRole.get());
            });
        }

        User updatedUser = repository.save(userToUpdate);
        return UserEntityConverter.toVO(updatedUser);
    }

    @Transactional
    public User findOrCreateUser(UserRequestVO vo) {
        // Primeiro, tenta encontrar usuário pelo email
        Optional<User> existingUser = repository.findByEmail(vo.getEmail());
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Se não encontrou, cria um novo usuário
        User user = UserEntityConverter.fromVO(vo);

        // Resolver roles já salvas no banco
        user.getRoles().clear();
        
        if (vo.getRoles() != null && !vo.getRoles().isEmpty()) {
            vo.getRoles().forEach(roleName -> {
                Optional<Role> optionalRole = roleRepository.findByAuthority(roleName);
                if (optionalRole.isEmpty()) {
                    throw new RuntimeException("Role não encontrada: " + roleName);
                }
                user.addRole(optionalRole.get());
            });
        } else {
            // Atribuir ROLE_OPERATOR por padrão quando nenhuma role é fornecida
            Optional<Role> defaultRole = roleRepository.findByAuthority("ROLE_OPERATOR");
            if (defaultRole.isEmpty()) {
                throw new RuntimeException("Role padrão ROLE_OPERATOR não encontrada no sistema");
            }
            user.addRole(defaultRole.get());
        }

        // Criptografar senha antes de salvar
        user.setPassword(passwordEncoder.encode(vo.getPassword())); // Senha criptografada

        return repository.save(user);
    }
}
