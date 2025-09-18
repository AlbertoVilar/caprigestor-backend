package com.devmaster.goatfarm.authority.controller;

import com.devmaster.goatfarm.authority.business.AdminBusiness;
import com.devmaster.goatfarm.authority.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminBusiness adminBusiness;



    @PostMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestParam String email, @RequestParam String newPassword) {
        try {
            boolean updated = adminBusiness.updateUserPassword(email, newPassword);
            if (!updated) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok("Senha atualizada com sucesso para " + email);
        } catch (Exception e) {
            logger.error("[ADMIN] Erro ao atualizar senha: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }

    @GetMapping("/user-info/{email}")
    public ResponseEntity<String> getUserInfo(@PathVariable String email) {
        try {
            User user = adminBusiness.getUserByEmail(email);
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
            boolean success = adminBusiness.cleanDatabaseAndSetupAdmin();
            if (success) {
                return ResponseEntity.ok("Banco limpo com sucesso! Mantido apenas albertovilar1@gmail.com com CPF 05202259450");
            } else {
                return ResponseEntity.internalServerError().body("Erro ao limpar banco de dados");
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("[ADMIN] Erro ao limpar banco: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Erro: " + e.getMessage());
        }
    }
}