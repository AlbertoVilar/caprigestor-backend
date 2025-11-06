package com.devmaster.goatfarm.authority.api.controller;

import com.devmaster.goatfarm.authority.api.dto.UserRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.authority.facade.UserFacade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import com.devmaster.goatfarm.authority.api.dto.UserPasswordUpdateDTO;
import com.devmaster.goatfarm.authority.api.dto.UserRolesUpdateDTO;
import com.devmaster.goatfarm.authority.facade.UserFacade;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserFacade userFacade;

    public UserController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or #id == principal.id")
    @PatchMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody @Valid UserPasswordUpdateDTO dto) {
        try {
            if (!dto.getPassword().equals(dto.getConfirmPassword())) {
                throw new IllegalArgumentException("As senhas não coincidem");
            }
            userFacade.updatePassword(id, dto.getPassword());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> validationErrors = new HashMap<>();
            validationErrors.put("validation", e.getMessage());
            throw new com.devmaster.goatfarm.config.exceptions.custom.ValidationException(
                "Dados inválidos", validationErrors);
        }
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}/roles")
    public ResponseEntity<UserResponseDTO> updateRoles(@PathVariable Long id, @RequestBody @Valid UserRolesUpdateDTO dto) {
        return ResponseEntity.ok(userFacade.updateRoles(id, dto.getRoles()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMe() {
        return ResponseEntity.ok(userFacade.getMe());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        logger.info("Iniciando busca por usuÃ¡rio com ID: {}", id);
        try {
            return ResponseEntity.ok(userFacade.findById(id));
        } catch (Exception e) {
            logger.error("ERRO COMPLETO ao buscar usuÃ¡rio com ID {}: {}", id, e.getMessage());
            logger.error("Stack trace completo:", e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserRequestDTO dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(userFacade.saveUser(dto));
        } catch (IllegalArgumentException e) {
            Map<String, String> validationErrors = new HashMap<>();
            validationErrors.put("validation", e.getMessage());
            throw new com.devmaster.goatfarm.config.exceptions.custom.ValidationException(
                "Dados invÃ¡lidos", validationErrors);
        } catch (Exception e) {
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody @Valid UserRequestDTO dto) {
        logger.info("Iniciando atualizaÃ§Ã£o do usuÃ¡rio com ID: {}", id);
        try {
            return ResponseEntity.ok(userFacade.updateUser(id, dto));
        } catch (IllegalArgumentException e) {
            Map<String, String> validationErrors = new HashMap<>();
            validationErrors.put("validation", e.getMessage());
            throw new com.devmaster.goatfarm.config.exceptions.custom.ValidationException(
                "Dados invÃ¡lidos", validationErrors);
        } catch (Exception e) {
            logger.error("ERRO ao atualizar usuÃ¡rio com ID {}: {}", id, e.getMessage());
            logger.error("Stack trace completo:", e);
            throw e;
        }
    }

        @GetMapping("/debug/{email}")
    public ResponseEntity<Map<String, Object>> debugUserRoles(@PathVariable String email) {
        try {
            UserResponseDTO user = userFacade.findByEmail(email);
            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("email", user.getEmail());
            debugInfo.put("name", user.getName());
            debugInfo.put("rolesCount", user.getRoles().size());
            debugInfo.put("roles", user.getRoles());
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            errorInfo.put("email", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorInfo);
        }
    }
}
