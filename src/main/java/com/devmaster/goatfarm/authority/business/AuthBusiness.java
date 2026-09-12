package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.RefreshSessionPersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.CredentialAuthenticationPort;
import com.devmaster.goatfarm.authority.application.ports.out.AuthTokenPort;
import com.devmaster.goatfarm.authority.business.bo.LoginRequestVO;
import com.devmaster.goatfarm.authority.business.bo.LoginResponseVO;
import com.devmaster.goatfarm.authority.business.bo.RefreshTokenRequestVO;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.business.bo.AuthorityAccount;
import com.devmaster.goatfarm.authority.business.bo.IssuedRefreshToken;
import com.devmaster.goatfarm.authority.business.bo.RefreshTokenClaims;
import com.devmaster.goatfarm.authority.business.bo.RefreshSessionRecord;
import com.devmaster.goatfarm.authority.business.mapper.AuthorityBusinessMapper;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final CredentialAuthenticationPort credentialAuthenticationPort;
    private final AuthTokenPort authTokenPort;
    private final UserPersistencePort userPort;
    private final AuthorityBusinessMapper authorityBusinessMapper;
    private final RefreshSessionPersistencePort refreshSessionPort;
    private final Clock clock;

    @Autowired
    public AuthBusiness(CredentialAuthenticationPort credentialAuthenticationPort, AuthTokenPort authTokenPort,
                        UserPersistencePort userPort, AuthorityBusinessMapper authorityBusinessMapper,
                        RefreshSessionPersistencePort refreshSessionPort) {
        this(credentialAuthenticationPort, authTokenPort, userPort, authorityBusinessMapper,
                refreshSessionPort, Clock.systemUTC());
    }

    AuthBusiness(CredentialAuthenticationPort credentialAuthenticationPort, AuthTokenPort authTokenPort,
                 UserPersistencePort userPort, AuthorityBusinessMapper authorityBusinessMapper,
                 RefreshSessionPersistencePort refreshSessionPort, Clock clock) {
        this.credentialAuthenticationPort = credentialAuthenticationPort;
        this.authTokenPort = authTokenPort;
        this.userPort = userPort;
        this.authorityBusinessMapper = authorityBusinessMapper;
        this.refreshSessionPort = refreshSessionPort;
        this.clock = clock;
    }

    @Transactional
    public LoginResponseVO authenticateUser(LoginRequestVO loginRequest) {
        logger.info("event=login_attempt");

        AuthenticatedPrincipal principal;
        try {
            principal = credentialAuthenticationPort.authenticate(
                    loginRequest.getEmail(), loginRequest.getPassword());
        } catch (InvalidArgumentException exception) {
            logger.warn("event=login_failed reason=bad_credentials");
            throw exception;
        }
        AuthorityAccount user = userPort.findById(principal.id())
                .orElseThrow(() -> new UnauthorizedException("Usuário autenticado não encontrado: " + principal.email()));
        String accessToken = authTokenPort.issueAccessToken(principal);
        IssuedRefreshToken refreshToken = authTokenPort.issueRefreshToken(principal, null);
        persistRefreshSession(user, refreshToken);
        logger.info("event=login_succeeded userId={}", user.id());

        return authorityBusinessMapper.toLoginResponseVO(user, accessToken, refreshToken.token(), authTokenPort.accessTokenDurationSeconds());
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public LoginResponseVO refreshToken(RefreshTokenRequestVO refreshRequest) {
        logger.info("event=token_refresh_attempt");

        String rawToken = refreshRequest.getRefreshToken();
        RefreshSessionRecord currentSession;
        String email;
        try {
            RefreshTokenClaims claims = authTokenPort.decodeRefreshToken(rawToken);
            email = claims.subject();
            String scope = claims.scope();

            if (!"REFRESH".equals(scope) || !"refresh".equals(claims.type())) {
                throw new UnauthorizedException("Token inválido - não é um refresh token");
            }

            currentSession = refreshSessionPort.findByTokenHashForUpdate(hashToken(rawToken))
                    .orElseThrow(() -> new UnauthorizedException("Token inválido ou expirado"));
            validateSessionClaims(currentSession, claims.tokenId(), claims.familyId(), claims.userId());
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

        AuthorityAccount user = currentSession.getUser();
        if (!email.equals(user.email())) {
            refreshSessionPort.revokeFamily(currentSession.getFamilyId(), now, "refresh_subject_mismatch");
            throw new UnauthorizedException("Token inválido ou expirado");
        }

        AuthenticatedPrincipal principal = toPrincipal(user);
        String newAccessToken = authTokenPort.issueAccessToken(principal);
        IssuedRefreshToken newRefreshToken = authTokenPort.issueRefreshToken(principal, currentSession.getFamilyId());
        RefreshSessionRecord replacement = persistRefreshSession(user, newRefreshToken);
        refreshSessionPort.setReplacement(currentSession.getId(), replacement.getId());

        return authorityBusinessMapper.toLoginResponseVO(user, newAccessToken, newRefreshToken.token(), authTokenPort.accessTokenDurationSeconds());
    }

    @Override
    public LoginResponseVO login(LoginRequestVO loginRequest) {
        return authenticateUser(loginRequest);
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequestVO refreshRequest) {
        try {
            RefreshTokenClaims claims = authTokenPort.decodeRefreshToken(refreshRequest.getRefreshToken());
            if (!"refresh".equals(claims.type())) {
                throw new UnauthorizedException("Token inválido ou expirado");
            }
            RefreshSessionRecord session = refreshSessionPort.findByTokenHashForUpdate(hashToken(refreshRequest.getRefreshToken()))
                    .orElseThrow(() -> new UnauthorizedException("Token inválido ou expirado"));
            Instant now = Instant.now(clock);
            refreshSessionPort.revokeFamily(session.getFamilyId(), now, "logout");
            logger.info("event=logout_succeeded userId={}", session.getUser().id());
        } catch (UnauthorizedException exception) {
            throw exception;
        } catch (Exception exception) {
            logger.warn("event=logout_failed exception={}", exception.getClass().getSimpleName());
            throw new UnauthorizedException("Token inválido ou expirado");
        }
    }

    private RefreshSessionRecord persistRefreshSession(AuthorityAccount user, IssuedRefreshToken issuedToken) {
        return refreshSessionPort.save(new RefreshSessionRecord(null, user, hashToken(issuedToken.token()),
                issuedToken.tokenId(), issuedToken.familyId(), issuedToken.issuedAt(), issuedToken.expiresAt(), null, null));
    }

    private void validateSessionClaims(RefreshSessionRecord session, String tokenId, String familyId, String userId) {
        try {
            if (!session.getTokenId().equals(UUID.fromString(tokenId))
                    || !session.getFamilyId().equals(UUID.fromString(familyId))
                    || !session.getUser().id().equals(Long.valueOf(userId))) {
                throw new UnauthorizedException("Token inválido ou expirado");
            }
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedException("Token inválido ou expirado");
        }
    }

    private boolean isSessionInactive(RefreshSessionRecord session, Instant now) {
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

    private AuthenticatedPrincipal toPrincipal(AuthorityAccount user) {
        return new AuthenticatedPrincipal(
                user.id(),
                user.email(),
                user.name(),
                user.roles()
        );
    }
}
