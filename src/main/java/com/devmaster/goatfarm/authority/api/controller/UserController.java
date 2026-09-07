package com.devmaster.goatfarm.authority.api.controller;

import com.devmaster.goatfarm.authority.api.dto.UserRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserUpdateRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.authority.application.ports.in.UserManagementUseCase;
import com.devmaster.goatfarm.authority.api.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import com.devmaster.goatfarm.authority.api.dto.UserPasswordUpdateDTO;
import com.devmaster.goatfarm.authority.api.dto.UserRolesUpdateDTO;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserManagementUseCase userUseCase;
    private final UserMapper userMapper;
    public UserController(UserManagementUseCase userUseCase, UserMapper userMapper) {
        this.userUseCase = userUseCase;
        this.userMapper = userMapper;
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody @Valid UserPasswordUpdateDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException("As senhas não coincidem");
        }
        userUseCase.updatePassword(id, dto.getPassword());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/roles")
    public ResponseEntity<UserResponseDTO> updateRoles(@PathVariable Long id, @RequestBody @Valid UserRolesUpdateDTO dto) {
        return ResponseEntity.ok(userMapper.toResponseDTO(userUseCase.updateRoles(id, dto.getRoles())));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMe() {
        return ResponseEntity.ok(userMapper.toResponseDTO(userUseCase.getMe()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        logger.info("Iniciando busca por usuário com ID: {}", id);
        try {
            return ResponseEntity.ok(userMapper.toResponseDTO(userUseCase.findById(id)));
        } catch (Exception e) {
            logger.error("ERRO COMPLETO ao buscar usuário com ID {}: {}", id, e.getMessage());
            logger.error("Stack trace completo:", e);
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                userMapper.toResponseDTO(userUseCase.saveUser(userMapper.toRequestVO(dto)))
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateRequestDTO dto) {
        logger.info("Iniciando atualização do usuário com ID: {}", id);
        return ResponseEntity.ok(
                userMapper.toResponseDTO(userUseCase.updateUser(id, userMapper.toRequestVO(dto)))
        );
    }
}
