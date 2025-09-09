package com.devmaster.goatfarm.config;

import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println(">>> Iniciando AdminUserInitializer <<<");
        
        // Buscar o usuário Alberto Vilar existente
        Optional<User> adminUserOptional = userRepository.findByEmail("albertovilar1@gmail.com");
        
        if (adminUserOptional.isPresent()) {
            User adminUser = adminUserOptional.get();
            
            // Atualizar a senha para '132747' criptografada
            String newPassword = passwordEncoder.encode("132747");
            adminUser.setPassword(newPassword);
            
            userRepository.save(adminUser);
            
            System.out.println(">>> Senha do usuário Alberto Vilar atualizada com sucesso! <<<");
            System.out.println(">>> Email: albertovilar1@gmail.com | Senha: 132747 <<<");
        } else {
            System.out.println(">>> AVISO: Usuário Alberto Vilar não encontrado no banco de dados! <<<");
        }
    }
}