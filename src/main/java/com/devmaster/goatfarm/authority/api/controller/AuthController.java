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
        System.out.println("🔍 LOGIN: Tentativa de login para: " + loginRequest.getEmail());
        
        try {
            // Autenticar usuário
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            
            System.out.println("🔍 LOGIN: Autenticação bem-sucedida para: " + loginRequest.getEmail());

            // Obter usuário autenticado
            User user = (User) authentication.getPrincipal();
            System.out.println("🔍 LOGIN: Usuário obtido: " + user.getEmail() + ", Roles: " + user.getRoles().size());

            // Gerar tokens
            System.out.println("🔍 LOGIN: Gerando tokens JWT...");
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            System.out.println("🔍 LOGIN: Tokens gerados com sucesso");

            // Criar resposta
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
            System.out.println("🔍 LOGIN ERROR: Credenciais inválidas para: " + loginRequest.getEmail());
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

        // Criar usuário com role padrão FARM_OWNER
        UserRequestVO userVO = new UserRequestVO(
            registerRequest.getName(),
            registerRequest.getEmail(),
            registerRequest.getCpf(),
            registerRequest.getPassword(), // Será criptografada no DAO
            registerRequest.getConfirmPassword(),
            List.of("ROLE_FARM_OWNER") // Role padrão para novos usuários
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
            
            // Verificar se é um refresh token
            String scope = jwt.getClaimAsString("scope");
            if (!"REFRESH".equals(scope)) {
                throw new RuntimeException("Token inválido");
            }

            // Buscar usuário
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
                null // Não precisamos retornar dados do usuário no refresh
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
    @Operation(summary = "Registrar nova fazenda e usuário", description = "Endpoint público para criar uma nova fazenda junto com seu usuário proprietário")
    public ResponseEntity<?> registerFarm(@Valid @RequestBody GoatFarmFullRequestDTO farmRequest) {
        try {
            // Criar a fazenda completa usando os dados do DTO
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