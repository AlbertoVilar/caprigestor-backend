package com.devmaster.goatfarm.authority.api.controller;

import com.devmaster.goatfarm.authority.api.dto.*;
import com.devmaster.goatfarm.authority.business.AuthBusiness;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.facade.UserFacade;
import com.devmaster.goatfarm.authority.mapper.UserMapper;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.mapper.GoatFarmMapper;
import com.devmaster.goatfarm.farm.facade.GoatFarmFacade;
import com.devmaster.goatfarm.address.mapper.AddressMapper;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import com.devmaster.goatfarm.config.security.JwtService;
import com.devmaster.goatfarm.authority.repository.RoleRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthBusiness authBusiness;
    private final UserFacade userFacade;
    private final GoatFarmFacade farmFacade;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final GoatFarmMapper farmMapper;
    private final AddressMapper addressMapper;
    private final PhoneMapper phoneMapper;

    public AuthController(AuthBusiness authBusiness, UserFacade userFacade, GoatFarmFacade farmFacade, 
                         PasswordEncoder passwordEncoder, UserMapper userMapper, GoatFarmMapper farmMapper, 
                         AddressMapper addressMapper, PhoneMapper phoneMapper) {
        this.authBusiness = authBusiness;
        this.userFacade = userFacade;
        this.farmFacade = farmFacade;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.farmMapper = farmMapper;
        this.addressMapper = addressMapper;
        this.phoneMapper = phoneMapper;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            LoginResponseDTO response = authBusiness.authenticateUser(loginRequest);
            return ResponseEntity.ok(response);
        } catch (com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException e) {
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        // Validar se as senhas coincidem
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException(
                "As senhas não coincidem");
        }

        // Create user with default role FARM_OWNER
        UserRequestVO userVO = UserRequestVO.builder()
            .name(registerRequest.getName())
            .email(registerRequest.getEmail())
            .cpf(registerRequest.getCpf())
            .password(registerRequest.getPassword()) // Será criptografada no DAO
            .confirmPassword(registerRequest.getConfirmPassword())
            .roles(List.of("ROLE_OPERATOR")) // Default role for new users
            .build();
        
        var createdUser = userFacade.saveUser(userVO);
        UserResponseVO userVO_response = new UserResponseVO(createdUser.getId(), createdUser.getName(), createdUser.getEmail(), createdUser.getCpf(), createdUser.getRoles());
        UserResponseDTO response = userMapper.toResponseDTO(userVO_response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshRequest) {
        try {
            LoginResponseDTO response = authBusiness.refreshToken(refreshRequest);
            return ResponseEntity.ok(response);
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
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                var userVO = userFacade.findByEmail(email);
                
                if (userVO == null) {
                    throw new RuntimeException("Usuário não encontrado");
                }

                UserResponseVO userResponseVO = new UserResponseVO(userVO.getId(), userVO.getName(), userVO.getEmail(), userVO.getCpf(), userVO.getRoles());
                UserResponseDTO response = userMapper.toResponseDTO(userResponseVO);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Erro ao obter usuário atual");
            error.put("error", "USER_FETCH_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/register-farm")
    @Operation(summary = "Register new farm and user", description = "Public endpoint to create a new farm along with its owner user")
    public ResponseEntity<?> registerFarm(@Valid @RequestBody GoatFarmFullRequestDTO farmRequest) {
        try {
            // Create complete farm using DTO data
            GoatFarmFullResponseVO farmResponse = farmFacade.createFullGoatFarm(
                farmMapper.toRequestVO(farmRequest.getFarm()),
                userMapper.toRequestVO(farmRequest.getUser()),
                addressMapper.toVO(farmRequest.getAddress()),
                farmRequest.getPhones().stream().map(phoneMapper::toRequestVO).toList()
            );

            // Converter para DTO de resposta
            GoatFarmFullResponseDTO response = farmMapper.toFullDTO(farmResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Erro ao registrar fazenda: " + e.getMessage());
            error.put("error", "FARM_REGISTRATION_ERROR");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}