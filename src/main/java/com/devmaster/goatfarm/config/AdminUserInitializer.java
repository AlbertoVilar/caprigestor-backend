package com.devmaster.goatfarm.config;

import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info(">>> Iniciando AdminUserInitializer <<<");
        
        // Find existing user Alberto Vilar
        Optional<User> adminUserOptional = userRepository.findByEmail("albertovilar1@gmail.com");
        
        if (adminUserOptional.isPresent()) {
            User adminUser = adminUserOptional.get();
            
            // Update password to encrypted '132747'
            String newPassword = passwordEncoder.encode("132747");
            adminUser.setPassword(newPassword);
            
            userRepository.save(adminUser);
            
            logger.info(">>> Senha do usuário Alberto Vilar atualizada com sucesso! <<<");
            logger.info(">>> Email: albertovilar1@gmail.com | Senha: 132747 <<<");
        } else {
            logger.warn(">>> AVISO: Usuário Alberto Vilar não encontrado no banco de dados! <<<");
        }
    }
}