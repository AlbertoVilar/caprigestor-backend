package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.RefreshSessionPersistencePort;
import com.devmaster.goatfarm.authority.business.bo.LoginRequestVO;
import com.devmaster.goatfarm.authority.business.bo.LoginResponseVO;
import com.devmaster.goatfarm.authority.business.bo.RefreshTokenRequestVO;
import com.devmaster.goatfarm.authority.business.mapper.AuthorityBusinessMapper;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.entity.RefreshSession;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.config.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class AuthBusiness implements com.devmaster.goatfarm.authority.application.ports.in.AuthManagementUseCase {

    private static final Logger logger = LoggerFactory.getLogger(AuthBusiness.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserPersistencePort userPort;
    private final AuthorityBusinessMapper authorityBusinessMapper;
    private final JwtDecoder refreshJwtDecoder;
    private final RefreshSessionPersistencePort refreshSessionPort;
    private final Clock clock;

    @Autowired
    public AuthBusiness(AuthenticationManager authenticationManager, JwtService jwtService,
                        UserPersistencePort userPort, AuthorityBusinessMapper authorityBusinessMapper,
                        @Qualifier("refreshJwtDecoder") JwtDecoder refreshJwtDecoder,
                        RefreshSessionPersistencePort refreshSessionPort) {
        this(authenticationManager, jwtService, userPort, authorityBusinessMapper, refreshJwtDecoder,
                refreshSessionPort, Clock.systemUTC());
    }

    AuthBusiness(AuthenticationManager authenticationManager, JwtService jwtService,
                 UserPersistencePort userPort, AuthorityBusinessMapper authorityBusinessMapper,
                 JwtDecoder refreshJwtDecoder, RefreshSessionPersistencePort refreshSessionPort, Clock clock) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userPort = userPort;
        this.authorityBusinessMapper = authorityBusinessMapper;
        this.refreshJwtDecoder = refreshJwtDecoder;
        this.refreshSessionPort = refreshSessionPort;
        this.clock = clock;
    }

    @Transactional
    public LoginResponseVO authenticateUser(LoginRequestVO loginRequest) {
        logger.info("event=login_attempt");

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            User user = (User) authentication.getPrincipal();
            String accessToken = jwtService.generateToken(user);
            JwtService.IssuedRefreshToken refreshToken = jwtService.issueRefreshToken(user, null);
            persistRefreshSession(user, refreshToken);
            logger.info("event=login_succeeded userId={}", user.getId());

            return authorityBusinessMapper.toLoginResponseVO(user, accessToken, refreshToken.token(), jwtService.getAccessTokenDurationSeconds());

        } catch (BadCredentialsException e) {
            logger.warn("event=login_failed reason=bad_credentials");
            throw new InvalidArgumentException("Email ou senha inválidos");
        }
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public LoginResponseVO refreshToken(RefreshTokenRequestVO refreshRequest) {
        logger.info("event=token_refresh_attempt");

        String rawToken = refreshRequest.getRefreshToken();
        RefreshSession currentSession;
        String email;
        try {
            var jwt = refreshJwtDecoder.decode(rawToken);
            email = jwt.getSubject();
            String scope = jwt.getClaimAsString("scope");

            if (!"REFRESH".equals(scope) || !"refresh".equals(jwt.getClaimAsString("typ"))) {
                throw new UnauthorizedException("Token inválido - não é um refresh token");
            }

            currentSession = refreshSessionPort.findByTokenHashForUpdate(hashToken(rawToken))
                    .orElseThrow(() -> new UnauthorizedException("Token inválido ou expirado"));
            validateSessionClaims(currentSession, jwt.getId(), jwt.getClaimAsString("familyId"), jwt.getClaimAsString("userId"));
        } catch (UnauthorizedException exception) {
            throw exception;
        } catch (Exception exception) {
            logger.warn("event=token_refresh_failed exception={}", exception.getClass().getSimpleName());
            throw new UnauthorizedException("Token inválido ou expirado");
        }

        Instant now = Instant.now(clock);
        if (isSessionInactive(currentSession, now) || !refreshSessionPort.consumeIfActive(currentSession.getId(), now)) {
            refreshSessionPort.revokeFamily(currentSession.getFamilyId(), now, "refresh_replay_or_inactive");
            logger.warn("event=refresh_replay_detected familyId={}", currentSession.getFamilyId());
            throw new UnauthorizedException("Token inválido ou expirado");
        }

        User user = currentSession.getUser();
        if (!email.equals(user.getEmail())) {
            refreshSessionPort.revokeFamily(currentSession.getFamilyId(), now, "refresh_subject_mismatch");
            throw new UnauthorizedException("Token inválido ou expirado");
        }

        String newAccessToken = jwtService.generateToken(user);
        JwtService.IssuedRefreshToken newRefreshToken = jwtService.issueRefreshToken(user, currentSession.getFamilyId());
        RefreshSession replacement = persistRefreshSession(user, newRefreshToken);
        refreshSessionPort.setReplacement(currentSession.getId(), replacement.getId());

        return authorityBusinessMapper.toLoginResponseVO(user, newAccessToken, newRefreshToken.token(), jwtService.getAccessTokenDurationSeconds());
    }

    @Override
    public LoginResponseVO login(LoginRequestVO loginRequest) {
        return authenticateUser(loginRequest);
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequestVO refreshRequest) {
        try {
            var jwt = refreshJwtDecoder.decode(refreshRequest.getRefreshToken());
            if (!"refresh".equals(jwt.getClaimAsString("typ"))) {
                throw new UnauthorizedException("Token inválido ou expirado");
            }
            RefreshSession session = refreshSessionPort.findByTokenHashForUpdate(hashToken(refreshRequest.getRefreshToken()))
                    .orElseThrow(() -> new UnauthorizedException("Token inválido ou expirado"));
            Instant now = Instant.now(clock);
            refreshSessionPort.revokeFamily(session.getFamilyId(), now, "logout");
            logger.info("event=logout_succeeded userId={}", session.getUser().getId());
        } catch (UnauthorizedException exception) {
            throw exception;
        } catch (Exception exception) {
            logger.warn("event=logout_failed exception={}", exception.getClass().getSimpleName());
            throw new UnauthorizedException("Token inválido ou expirado");
        }
    }

    private RefreshSession persistRefreshSession(User user, JwtService.IssuedRefreshToken issuedToken) {
        return refreshSessionPort.save(RefreshSession.builder()
                .user(user)
                .tokenHash(hashToken(issuedToken.token()))
                .tokenId(issuedToken.tokenId())
                .familyId(issuedToken.familyId())
                .issuedAt(issuedToken.issuedAt())
                .expiresAt(issuedToken.expiresAt())
                .build());
    }

    private void validateSessionClaims(RefreshSession session, String tokenId, String familyId, String userId) {
        try {
            if (!session.getTokenId().equals(UUID.fromString(tokenId))
                    || !session.getFamilyId().equals(UUID.fromString(familyId))
                    || !session.getUser().getId().equals(Long.valueOf(userId))) {
                throw new UnauthorizedException("Token inválido ou expirado");
            }
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedException("Token inválido ou expirado");
        }
    }

    private boolean isSessionInactive(RefreshSession session, Instant now) {
        return session.getConsumedAt() != null || session.getRevokedAt() != null
                || session.getExpiresAt() == null || !session.getExpiresAt().isAfter(now);
    }

    private String hashToken(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não disponível no ambiente", exception);
        }
    }
}
