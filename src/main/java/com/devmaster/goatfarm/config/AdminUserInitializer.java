package com.devmaster.goatfarm.config;

import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import com.devmaster.goatfarm.authority.repository.RoleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Profile("!test")
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
        
                Role adminRole = ensureRoleExists("ROLE_ADMIN");
        Role operatorRole = ensureRoleExists("ROLE_OPERATOR");
        Role farmOwnerRole = ensureRoleExists("ROLE_FARM_OWNER");
        ensureRoleExists("ROLE_VIEWER");
        
                Optional<User> adminUserOptional = userRepository.findByEmail("albertovilar1@gmail.com");
        
        if (adminUserOptional.isPresent()) {
            User adminUser = adminUserOptional.get();
            
                        String newPassword = passwordEncoder.encode("132747");
            adminUser.setPassword(newPassword);
            
                        if (!adminUser.hasRole("ROLE_ADMIN")) {
                adminUser.addRole(adminRole);
            }
            if (!adminUser.hasRole("ROLE_OPERATOR")) {
                adminUser.addRole(operatorRole);
            }
            
            userRepository.save(adminUser);
            
            logger.info(">>> Senha do usuário Alberto Vilar atualizada com sucesso! <<<");
            logger.info(">>> Email: albertovilar1@gmail.com | Senha: (omitida) <<<");
        } else {
                        User adminUser = new User();
            adminUser.setName("Alberto Vilar");
            adminUser.setEmail("albertovilar1@gmail.com");
            adminUser.setCpf("05202259450");
            adminUser.setPassword(passwordEncoder.encode("132747"));
            
                        adminUser.addRole(adminRole);
            adminUser.addRole(operatorRole);
            
            userRepository.save(adminUser);
            
            logger.info(">>> Usuário Alberto Vilar criado com sucesso! <<<");
            logger.info(">>> Email: albertovilar1@gmail.com | Senha: (omitida) <<<");
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