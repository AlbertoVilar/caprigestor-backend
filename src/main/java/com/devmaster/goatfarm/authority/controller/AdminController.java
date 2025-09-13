package com.devmaster.goatfarm.authority.controller;

import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.RoleRepository;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;



    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestParam String email, @RequestParam String newPassword) {
        try {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            user.setPassword(passwordEncoder.encode(newPassword)); // Senha criptografada
            userRepository.save(user);

            logger.info("[ADMIN] Senha atualizada para usuário: {}", email);

            return ResponseEntity.ok("Senha atualizada com sucesso para " + email);
        } catch (Exception e) {
            logger.error("[ADMIN] Erro ao atualizar senha: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }

    @GetMapping("/user-info/{email}")
    public ResponseEntity<String> getUserInfo(@PathVariable String email) {
        try {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok("Email: " + user.getEmail() + ", Hash: " + user.getPassword());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }

    @PostMapping("/clean-users")
    public ResponseEntity<String> cleanUsers() {
        try {
            // Primeiro, encontra o usuário admin
            User adminUser = userRepository.findByEmail("albertovilar1@gmail.com").orElse(null);
            if (adminUser == null) {
                return ResponseEntity.badRequest().body("Usuário admin não encontrado!");
            }

            // Atualiza o CPF e senha do admin
            adminUser.setCpf("05202259450");
            adminUser.setPassword(passwordEncoder.encode("password")); // Senha criptografada
            userRepository.save(adminUser);
            
            logger.info("[ADMIN] Senha atualizada para: password");

            // Limpa o banco em etapas para evitar violação de chave estrangeira
            userRepository.cleanDatabaseStepByStep(adminUser.getId());
            
            // Recriar as roles se não existirem
            Role adminRole = roleRepository.findByAuthority("ROLE_ADMIN").orElse(null);
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setAuthority("ROLE_ADMIN");
                adminRole = roleRepository.save(adminRole);
                logger.info("[ADMIN] Role ROLE_ADMIN criada");
            }
            
            Role operatorRole = roleRepository.findByAuthority("ROLE_OPERATOR").orElse(null);
            if (operatorRole == null) {
                operatorRole = new Role();
                operatorRole.setAuthority("ROLE_OPERATOR");
                operatorRole = roleRepository.save(operatorRole);
                logger.info("[ADMIN] Role ROLE_OPERATOR criada");
            }
            
            // Adicionar as roles ao usuário admin
            adminUser.addRole(adminRole);
            adminUser.addRole(operatorRole);
            logger.info("[ADMIN] Roles ROLE_ADMIN e ROLE_OPERATOR adicionadas ao usuário");
            
            userRepository.save(adminUser);
            logger.info("[ADMIN] Roles do usuário admin recriadas");

            logger.info("[ADMIN] Banco limpo! Mantido apenas: {} com CPF: {}", adminUser.getEmail(), adminUser.getCpf());
            
            return ResponseEntity.ok("Banco limpo com sucesso! Mantido apenas albertovilar1@gmail.com com CPF 05202259450");
        } catch (Exception e) {
            logger.error("[ADMIN] Erro ao limpar banco: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }
}