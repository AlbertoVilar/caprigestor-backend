package com.devmaster.goatfarm.authority.controller;

import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.RoleRepository;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;



    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestParam String email, @RequestParam String newPassword) {
        try {
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            user.setPassword(newPassword); // Senha sem criptografia
            userRepository.save(user);

            System.out.println("[ADMIN] Senha atualizada para usuário: " + email);

            return ResponseEntity.ok("Senha atualizada com sucesso para " + email);
        } catch (Exception e) {
            System.out.println("[ADMIN] Erro ao atualizar senha: " + e.getMessage());
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
            adminUser.setPassword("password"); // Senha sem criptografia
            userRepository.save(adminUser);
            
            System.out.println("[ADMIN] Senha atualizada para: password");

            // Limpa o banco em etapas para evitar violação de chave estrangeira
            userRepository.cleanDatabaseStepByStep(adminUser.getId());
            
            // Recriar as roles se não existirem
            Role adminRole = roleRepository.findByAuthority("ROLE_ADMIN").orElse(null);
            if (adminRole == null) {
                adminRole = new Role();
                adminRole.setAuthority("ROLE_ADMIN");
                adminRole = roleRepository.save(adminRole);
                System.out.println("[ADMIN] Role ROLE_ADMIN criada");
            }
            
            Role operatorRole = roleRepository.findByAuthority("ROLE_OPERATOR").orElse(null);
            if (operatorRole == null) {
                operatorRole = new Role();
                operatorRole.setAuthority("ROLE_OPERATOR");
                operatorRole = roleRepository.save(operatorRole);
                System.out.println("[ADMIN] Role ROLE_OPERATOR criada");
            }
            
            // Adicionar as roles ao usuário admin
            adminUser.addRole(adminRole);
            adminUser.addRole(operatorRole);
            System.out.println("[ADMIN] Roles ROLE_ADMIN e ROLE_OPERATOR adicionadas ao usuário");
            
            userRepository.save(adminUser);
            System.out.println("[ADMIN] Roles do usuário admin recriadas");

            System.out.println("[ADMIN] Banco limpo! Mantido apenas: " + adminUser.getEmail() + " com CPF: " + adminUser.getCpf());
            
            return ResponseEntity.ok("Banco limpo com sucesso! Mantido apenas albertovilar1@gmail.com com CPF 05202259450");
        } catch (Exception e) {
            System.out.println("[ADMIN] Erro ao limpar banco: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }
}