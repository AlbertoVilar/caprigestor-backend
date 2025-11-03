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
        logger.info("ðŸ” LOGIN: Tentativa de login para: {}", loginRequest.getEmail());
        
        try {
                        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
            
            logger.info("ðŸ” LOGIN: AutenticaÃ§Ã£o bem-sucedida para: {}", loginRequest.getEmail());

                        User user = (User) authentication.getPrincipal();
            logger.debug("ðŸ” LOGIN: UsuÃ¡rio obtido: {}, Roles: {}", user.getEmail(), user.getRoles().size());

                        logger.debug("ðŸ” LOGIN: Gerando tokens JWT...");
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            logger.debug("ðŸ” LOGIN: Tokens gerados com sucesso");

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
            response.setExpiresIn(3600L);             response.setUser(userResponse);
            return response;

        } catch (BadCredentialsException e) {
            logger.warn("ðŸ” LOGIN ERROR: Credenciais invÃ¡lidas para: {}", loginRequest.getEmail());
            throw new InvalidArgumentException("Email ou senha invÃ¡lidos");
        }
    }

    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest) {
        logger.info("ðŸ”„ REFRESH: Tentativa de refresh token");
        
        try {
                        logger.debug("ðŸ”„ REFRESH: Decodificando refresh token...");
            var jwt = jwtDecoder.decode(refreshRequest.getRefreshToken());
            String email = jwt.getSubject();
            logger.debug("ðŸ”„ REFRESH: Email extraÃ­do do token: {}", email);
            
                        String scope = jwt.getClaimAsString("scope");
            if (!"REFRESH".equals(scope)) {
                logger.warn("ðŸ”„ REFRESH ERROR: Token nÃ£o Ã© um refresh token. Scope: {}", scope);
                throw new RuntimeException("Token invÃ¡lido - nÃ£o Ã© um refresh token");
            }
            logger.debug("ðŸ”„ REFRESH: Token validado como refresh token");

                        logger.debug("ðŸ”„ REFRESH: Buscando usuÃ¡rio por email: {}", email);
            var userVO = userFacade.findByEmail(email);
            if (userVO == null) {
                logger.warn("ðŸ”„ REFRESH ERROR: UsuÃ¡rio nÃ£o encontrado: {}", email);
                throw new RuntimeException("UsuÃ¡rio nÃ£o encontrado");
            }
            logger.debug("ðŸ”„ REFRESH: UsuÃ¡rio encontrado: {}", userVO.getName());

                        User user = new User();
            user.setId(userVO.getId());
            user.setName(userVO.getName());
            user.setEmail(userVO.getEmail());
            
                        logger.debug("ðŸ”„ REFRESH: Adicionando roles ao usuÃ¡rio...");
            for (String roleName : userVO.getRoles()) {
                Role role = roleDAO.findByAuthority(roleName)
                    .orElseThrow(() -> {
                        logger.error("ðŸ”„ REFRESH ERROR: Role nÃ£o encontrada: {}", roleName);
                        return new RuntimeException("Role nÃ£o encontrada: " + roleName);
                    });
                user.addRole(role);
            }
            logger.debug("ðŸ”„ REFRESH: {} roles adicionadas", userVO.getRoles().size());

                        logger.debug("ðŸ”„ REFRESH: Gerando novos tokens...");
            String newAccessToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);
            logger.info("ðŸ”„ REFRESH: Tokens renovados com sucesso para: {}", email);

            LoginResponseDTO response = new LoginResponseDTO();
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(newRefreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(3600L);
            response.setUser(null);             return response;

        } catch (Exception e) {
            logger.error("ðŸ”„ REFRESH ERROR: Erro ao renovar token: {}", e.getMessage(), e);
            throw new RuntimeException("Token invÃ¡lido ou expirado");
        }
    }
}
