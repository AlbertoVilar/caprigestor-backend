package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.authority.api.dto.LoginRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.LoginResponseDTO;
import com.devmaster.goatfarm.authority.api.dto.RefreshTokenRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.authority.business.bo.UserResponseVO;
import com.devmaster.goatfarm.authority.facade.UserFacade;
import com.devmaster.goatfarm.authority.model.entity.Role;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.dao.RoleDAO;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthBusiness {

    private static final Logger logger = LoggerFactory.getLogger(AuthBusiness.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserFacade userFacade;
    private final RoleDAO roleDAO;
    private final JwtDecoder jwtDecoder;

    public AuthBusiness(AuthenticationManager authenticationManager, JwtService jwtService,
                       UserFacade userFacade, RoleDAO roleDAO, JwtDecoder jwtDecoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userFacade = userFacade;
        this.roleDAO = roleDAO;
        this.jwtDecoder = jwtDecoder;
    }

    public LoginResponseDTO authenticateUser(LoginRequestDTO loginRequest) {
        logger.info("🔍 LOGIN: Tentativa de login para: {}", loginRequest.getEmail());
        
        try {
            // Lógica de negócio: autenticar credenciais
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            
            logger.info("🔍 LOGIN: Autenticação bem-sucedida para: {}", loginRequest.getEmail());

            // Lógica de negócio: obter usuário autenticado
            User user = (User) authentication.getPrincipal();
            logger.debug("🔍 LOGIN: Usuário obtido: {}, Roles: {}", user.getEmail(), user.getRoles().size());

            // Lógica de negócio: gerar tokens JWT
            logger.debug("🔍 LOGIN: Gerando tokens JWT...");
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            logger.debug("🔍 LOGIN: Tokens gerados com sucesso");

            // Lógica de negócio: criar resposta com dados do usuário
            List<String> roles = user.getRoles().stream()
                .map(Role::getAuthority)
                .toList();
            
            UserResponseDTO userResponse = new UserResponseDTO();
            userResponse.setId(user.getId());
            userResponse.setName(user.getName());
            userResponse.setEmail(user.getEmail());
            userResponse.setCpf(user.getCpf());
            userResponse.setRoles(roles);

            LoginResponseDTO response = new LoginResponseDTO();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(3600L); // 1 hora
            response.setUser(userResponse);
            return response;

        } catch (BadCredentialsException e) {
            logger.warn("🔍 LOGIN ERROR: Credenciais inválidas para: {}", loginRequest.getEmail());
            throw new InvalidArgumentException("Email ou senha inválidos");
        }
    }

    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest) {
        logger.info("🔄 REFRESH: Tentativa de refresh token");
        
        try {
            // Lógica de negócio: validar e decodificar o refresh token
            logger.debug("🔄 REFRESH: Decodificando refresh token...");
            var jwt = jwtDecoder.decode(refreshRequest.getRefreshToken());
            String email = jwt.getSubject();
            logger.debug("🔄 REFRESH: Email extraído do token: {}", email);
            
            // Lógica de negócio: verificar se é um refresh token
            String scope = jwt.getClaimAsString("scope");
            if (!"REFRESH".equals(scope)) {
                logger.warn("🔄 REFRESH ERROR: Token não é um refresh token. Scope: {}", scope);
                throw new RuntimeException("Token inválido - não é um refresh token");
            }
            logger.debug("🔄 REFRESH: Token validado como refresh token");

            // Lógica de negócio: buscar usuário
            logger.debug("🔄 REFRESH: Buscando usuário por email: {}", email);
            var userVO = userFacade.findByEmail(email);
            if (userVO == null) {
                logger.warn("🔄 REFRESH ERROR: Usuário não encontrado: {}", email);
                throw new RuntimeException("Usuário não encontrado");
            }
            logger.debug("🔄 REFRESH: Usuário encontrado: {}", userVO.getName());

            // Lógica de negócio: converter para User entity
            User user = new User();
            user.setId(userVO.getId());
            user.setName(userVO.getName());
            user.setEmail(userVO.getEmail());
            
            // Lógica de negócio: adicionar roles
            logger.debug("🔄 REFRESH: Adicionando roles ao usuário...");
            for (String roleName : userVO.getRoles()) {
                Role role = roleDAO.findByAuthority(roleName)
                    .orElseThrow(() -> {
                        logger.error("🔄 REFRESH ERROR: Role não encontrada: {}", roleName);
                        return new RuntimeException("Role não encontrada: " + roleName);
                    });
                user.addRole(role);
            }
            logger.debug("🔄 REFRESH: {} roles adicionadas", userVO.getRoles().size());

            // Lógica de negócio: gerar novos tokens
            logger.debug("🔄 REFRESH: Gerando novos tokens...");
            String newAccessToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);
            logger.info("🔄 REFRESH: Tokens renovados com sucesso para: {}", email);

            LoginResponseDTO response = new LoginResponseDTO();
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(3600L);
            response.setUser(null); // Não retornamos dados do usuário no refresh
            return response;

        } catch (Exception e) {
            logger.error("🔄 REFRESH ERROR: Erro ao renovar token: {}", e.getMessage(), e);
            throw new RuntimeException("Token inválido ou expirado");
        }
    }
}