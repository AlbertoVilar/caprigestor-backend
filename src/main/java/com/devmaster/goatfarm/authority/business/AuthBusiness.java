package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.authority.api.dto.LoginRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.LoginResponseDTO;
import com.devmaster.goatfarm.authority.api.dto.RefreshTokenRequestDTO;
import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.api.mapper.AuthMapper;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.config.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

@Service
public class AuthBusiness implements com.devmaster.goatfarm.authority.application.ports.in.AuthManagementUseCase {

    private static final Logger logger = LoggerFactory.getLogger(AuthBusiness.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserPersistencePort userPort;
    private final AuthMapper authMapper;
    private final JwtDecoder jwtDecoder;

    public AuthBusiness(AuthenticationManager authenticationManager, JwtService jwtService,
                        UserPersistencePort userPort, AuthMapper authMapper, JwtDecoder jwtDecoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userPort = userPort;
        this.authMapper = authMapper;
        this.jwtDecoder = jwtDecoder;
    }

    public LoginResponseDTO authenticateUser(LoginRequestDTO loginRequest) {
        logger.info("🔒 LOGIN: Tentativa de login para: {}", loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            User user = (User) authentication.getPrincipal();
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return authMapper.toLoginResponseDTO(user, accessToken, refreshToken);

        } catch (BadCredentialsException e) {
            logger.warn("🔒 LOGIN ERROR: Credenciais inválidas para: {}", loginRequest.getEmail());
            throw new InvalidArgumentException("Email ou senha inválidos");
        }
    }

    public LoginResponseDTO refreshToken(RefreshTokenRequestDTO refreshRequest) {
        logger.info("🔄 REFRESH: Tentativa de refresh token");

        try {
            var jwt = jwtDecoder.decode(refreshRequest.getRefreshToken());
            String email = jwt.getSubject();
            String scope = jwt.getClaimAsString("scope");

            if (!"REFRESH".equals(scope)) {
                throw new UnauthorizedException("Token inválido - não é um refresh token");
            }

            User user = userPort.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado para refresh: " + email));

            String newAccessToken = jwtService.generateToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);

            return authMapper.toLoginResponseDTO(newAccessToken, newRefreshToken);

        } catch (Exception e) {
            logger.error("🔄 REFRESH ERROR: Erro ao renovar token: {}", e.getMessage(), e);
            throw new UnauthorizedException("Token inválido ou expirado");
        }
    }

    @Override
    public LoginResponseDTO login(com.devmaster.goatfarm.authority.api.dto.LoginRequestDTO loginRequest) {
        return authenticateUser(loginRequest);
    }
}