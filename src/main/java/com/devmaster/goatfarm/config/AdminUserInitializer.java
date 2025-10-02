package com.devmaster.goatfarm.config;

import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import com.devmaster.goatfarm.authority.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AdminUserInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info(">>> Iniciando AdminUserInitializer <<<");
        
        // Criar roles se não existirem
        Role adminRole = ensureRoleExists("ROLE_ADMIN");
        Role operatorRole = ensureRoleExists("ROLE_OPERATOR");
        
        // Find existing user Alberto Vilar
        Optional<User> adminUserOptional = userRepository.findByEmail("albertovilar1@gmail.com");
        
        if (adminUserOptional.isPresent()) {
            User adminUser = adminUserOptional.get();
            
            // Update password to encrypted '132747'
            String newPassword = passwordEncoder.encode("132747");
            adminUser.setPassword(newPassword);
            
            // Garantir que o usuário tem as roles necessárias
            if (!adminUser.hasRole("ROLE_ADMIN")) {
                adminUser.addRole(adminRole);
            }
            if (!adminUser.hasRole("ROLE_OPERATOR")) {
                adminUser.addRole(operatorRole);
            }
            
            userRepository.save(adminUser);
            
            logger.info(">>> Senha do usuário Alberto Vilar atualizada com sucesso! <<<");
            logger.info(">>> Email: albertovilar1@gmail.com | Senha: 132747 <<<");
        } else {
            // Criar o usuário admin se não existir
            User adminUser = new User();
            adminUser.setName("Alberto Vilar");
            adminUser.setEmail("albertovilar1@gmail.com");
            adminUser.setCpf("05202259450");
            adminUser.setPassword(passwordEncoder.encode("132747"));
            
            // Adicionar roles
            adminUser.addRole(adminRole);
            adminUser.addRole(operatorRole);
            
            userRepository.save(adminUser);
            
            logger.info(">>> Usuário Alberto Vilar criado com sucesso! <<<");
            logger.info(">>> Email: albertovilar1@gmail.com | Senha: 132747 <<<");
        }
    }
    
    private Role ensureRoleExists(String authority) {
        Role role = roleRepository.findByAuthority(authority).orElse(null);
        if (role == null) {
            role = new Role();
            role.setAuthority(authority);
            role = roleRepository.save(role);
            logger.info(">>> Role {} criada <<<", authority);
        }
        return role;
    }
}