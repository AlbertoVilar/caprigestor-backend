package com.devmaster.goatfarm.authority.api.controller;

import com.devmaster.goatfarm.authority.api.dto.*;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.facade.UserFacade;
import com.devmaster.goatfarm.authority.conveter.UserDTOConverter;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.RoleRepository;
import com.devmaster.goatfarm.config.security.JwtService;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.converters.GoatFarmDTOConverter;
import com.devmaster.goatfarm.farm.facade.GoatFarmFacade;
import com.devmaster.goatfarm.address.converter.AddressDTOConverter;
import com.devmaster.goatfarm.phone.converter.PhoneDTOConverter;
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

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserFacade userFacade;
    private final GoatFarmFacade farmFacade;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final JwtDecoder jwtDecoder;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, 
                         UserFacade userFacade, GoatFarmFacade farmFacade, PasswordEncoder passwordEncoder,
                         RoleRepository roleRepository, JwtDecoder jwtDecoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userFacade = userFacade;
        this.farmFacade = farmFacade;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.jwtDecoder = jwtDecoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        logger.info("🔍 LOGIN: Tentativa de login para: {}", loginRequest.getEmail());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            
            logger.info("🔍 LOGIN: Autenticação bem-sucedida para: {}", loginRequest.getEmail());

            // Get authenticated user
            User user = (User) authentication.getPrincipal();
            logger.debug("🔍 LOGIN: Usuário obtido: {}, Roles: {}", user.getEmail(), user.getRoles().size());

            // Gerar tokens
            logger.debug("🔍 LOGIN: Gerando tokens JWT...");
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            logger.debug("🔍 LOGIN: Tokens gerados com sucesso");

            // Create response
            List<String> roles = user.getRoles().stream()
                .map(Role::getAuthority)
                .toList();
            
            UserResponseDTO userResponse = new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                roles
            );

            LoginResponseDTO response = new LoginResponseDTO(
                accessToken,
                refreshToken,
                "Bearer",
                3600L, // 1 hora
                userResponse
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            logger.warn("🔍 LOGIN ERROR: Credenciais inválidas para: {}", loginRequest.getEmail());
            throw new com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException(
                "Email ou senha inválidos");
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
        UserRequestVO userVO = new UserRequestVO(
            registerRequest.getName(),
            registerRequest.getEmail(),
            registerRequest.getCpf(),
            registerRequest.getPassword(), // Será criptografada no DAO
            registerRequest.getConfirmPassword(),
            List.of("ROLE_FARM_OWNER") // Default role for new users
        );
        
        var createdUser = userFacade.saveUser(userVO);
        UserResponseDTO response = UserDTOConverter.toDTO(createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshRequest) {
        try {
            // Validar e decodificar o refresh token
            var jwt = jwtDecoder.decode(refreshRequest.getRefreshToken());
            String email = jwt.getSubject();
            
            // Check if it's a refresh token
            String scope = jwt.getClaimAsString("scope");
            if (!"REFRESH".equals(scope)) {
                throw new RuntimeException("Token inválido");
            }

            // Find user
            var userVO = userFacade.findByEmail(email);
            if (userVO == null) {
                throw new RuntimeException("Usuário não encontrado");
            }

            // Converter para User entity (simulado)
            User user = new User();
            user.setId(userVO.getId());
            user.setName(userVO.getName());
            user.setEmail(userVO.getEmail());
            
            // Adicionar roles
            for (String roleName : userVO.getRoles()) {
                Role role = roleRepository.findByAuthority(roleName)
                    .orElseThrow(() -> new RuntimeException("Role não encontrada: " + roleName));
                user.addRole(role);
            }

            // Gerar novos tokens
            String newAccessToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            LoginResponseDTO response = new LoginResponseDTO(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                3600L,
                null // We don't need to return user data on refresh
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
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

                UserResponseDTO response = UserDTOConverter.toDTO(userVO);
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
                GoatFarmDTOConverter.toVO(farmRequest.getFarm()),
                UserDTOConverter.toVO(farmRequest.getUser()),
                AddressDTOConverter.toVO(farmRequest.getAddress()),
                farmRequest.getPhones().stream().map(PhoneDTOConverter::toVO).toList()
            );

            // Converter para DTO de resposta
            GoatFarmFullResponseDTO response = GoatFarmDTOConverter.toFullDTO(farmResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Erro ao registrar fazenda: " + e.getMessage());
            error.put("error", "FARM_REGISTRATION_ERROR");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}