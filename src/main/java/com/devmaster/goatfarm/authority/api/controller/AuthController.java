package com.devmaster.goatfarm.authority.api.controller;

import com.devmaster.goatfarm.authority.api.dto.*;
import com.devmaster.goatfarm.authority.facade.AuthFacade;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthFacade authFacade;

    public AuthController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            return ResponseEntity.ok(authFacade.login(loginRequest));
        } catch (com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(authFacade.register(registerRequest));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Erro ao registrar usuário: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshRequest) {
        try {
            return ResponseEntity.ok(authFacade.refreshToken(refreshRequest));
        } catch (Exception e) {
            logger.error("🔄 REFRESH ERROR: Erro ao renovar token: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("message", "Token inválido ou expirado");
            error.put("error", "INVALID_REFRESH_TOKEN");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            return ResponseEntity.ok(authFacade.getMe());
        } catch (com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/register-farm")
    @Operation(summary = "Register new farm and user", description = "Public endpoint to create a new farm along with its owner user")
    public ResponseEntity<?> registerFarm(@Valid @RequestBody GoatFarmFullRequestDTO farmRequest) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(authFacade.registerFarm(farmRequest));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Erro ao registrar fazenda: " + e.getMessage());
            error.put("error", "FARM_REGISTRATION_ERROR");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}