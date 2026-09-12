package com.devmaster.goatfarm.authority.business;

import com.devmaster.goatfarm.authority.application.ports.out.AuthTokenPort;
import com.devmaster.goatfarm.authority.application.ports.out.CredentialAuthenticationPort;
import com.devmaster.goatfarm.authority.application.ports.out.RefreshSessionPersistencePort;
import com.devmaster.goatfarm.authority.application.ports.out.UserPersistencePort;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.business.bo.IssuedRefreshToken;
import com.devmaster.goatfarm.authority.business.bo.LoginRequestVO;
import com.devmaster.goatfarm.authority.business.bo.LoginResponseVO;
import com.devmaster.goatfarm.authority.business.bo.RefreshTokenRequestVO;
import com.devmaster.goatfarm.authority.business.bo.RefreshTokenClaims;
import com.devmaster.goatfarm.authority.business.mapper.AuthorityBusinessMapper;
import com.devmaster.goatfarm.authority.persistence.entity.RefreshSession;
import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthBusinessCharacterizationTest {

    private static final Instant NOW = Instant.parse("2026-09-12T12:00:00Z");
    private static final UUID SESSION_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FAMILY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID TOKEN_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock private CredentialAuthenticationPort credentialAuthenticationPort;
    @Mock private AuthTokenPort authTokenPort;
    @Mock private UserPersistencePort userPort;
    @Mock private AuthorityBusinessMapper mapper;
    @Mock private RefreshSessionPersistencePort refreshSessionPort;

    private AuthBusiness authBusiness;
    private User user;
    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        authBusiness = new AuthBusiness(credentialAuthenticationPort, authTokenPort, userPort, mapper,
                refreshSessionPort, Clock.fixed(NOW, ZoneOffset.UTC));
        Role operator = new Role();
        operator.setAuthority("ROLE_OPERATOR");
        user = new User();
        user.setId(7L);
        user.setEmail("operator@example.com");
        user.setName("Operator");
        user.addRole(operator);
        principal = new AuthenticatedPrincipal(7L, user.getEmail(), user.getName(), Set.of("ROLE_OPERATOR"));
    }

    @Test
    void successfulAuthenticationIssuesTokensAndPersistsRefreshSession() {
        IssuedRefreshToken issued = new IssuedRefreshToken("refresh-token", TOKEN_ID, FAMILY_ID, NOW, NOW.plusSeconds(600));
        LoginResponseVO response = LoginResponseVO.builder().accessToken("access-token").refreshToken("refresh-token").build();
        when(credentialAuthenticationPort.authenticate("operator@example.com", "secret")).thenReturn(principal);
        when(userPort.findById(7L)).thenReturn(Optional.of(user));
        when(authTokenPort.issueAccessToken(principal)).thenReturn("access-token");
        when(authTokenPort.issueRefreshToken(principal, null)).thenReturn(issued);
        when(authTokenPort.accessTokenDurationSeconds()).thenReturn(900L);
        when(refreshSessionPort.save(any(RefreshSession.class))).thenAnswer(invocation -> {
            RefreshSession session = invocation.getArgument(0);
            session.setId(SESSION_ID.getMostSignificantBits());
            return session;
        });
        when(mapper.toLoginResponseVO(user, "access-token", "refresh-token", 900L)).thenReturn(response);

        assertThat(authBusiness.authenticateUser(LoginRequestVO.builder().email("operator@example.com").password("secret").build()))
                .isSameAs(response);
        verify(authTokenPort).issueRefreshToken(principal, null);
        verify(refreshSessionPort).save(any(RefreshSession.class));
    }

    @Test
    void badCredentialsKeepApplicationFailureSemantics() {
        when(credentialAuthenticationPort.authenticate("operator@example.com", "wrong"))
                .thenThrow(new InvalidArgumentException("Email ou senha inválidos"));

        assertThrows(InvalidArgumentException.class,
                () -> authBusiness.authenticateUser(LoginRequestVO.builder().email("operator@example.com").password("wrong").build()));
        verify(userPort, never()).findById(any());
        verify(authTokenPort, never()).issueAccessToken(any());
    }

    @Test
    void refreshRotationPreservesFamilyAndReplacementLink() {
        RefreshTokenClaims claims = new RefreshTokenClaims(user.getEmail(), TOKEN_ID.toString(), FAMILY_ID.toString(), "7", "refresh", "REFRESH");
        RefreshSession current = session(42L, TOKEN_ID, FAMILY_ID);
        IssuedRefreshToken replacementToken = new IssuedRefreshToken("new-refresh", UUID.randomUUID(), FAMILY_ID, NOW, NOW.plusSeconds(600));
        RefreshSession replacement = session(43L, replacementToken.tokenId(), FAMILY_ID);
        when(authTokenPort.decodeRefreshToken("old-refresh")).thenReturn(claims);
        when(refreshSessionPort.findByTokenHashForUpdate(any(String.class))).thenReturn(Optional.of(current));
        when(refreshSessionPort.consumeIfActive(42L, NOW)).thenReturn(true);
        when(authTokenPort.issueAccessToken(principal)).thenReturn("new-access");
        when(authTokenPort.issueRefreshToken(principal, FAMILY_ID)).thenReturn(replacementToken);
        when(refreshSessionPort.save(any(RefreshSession.class))).thenReturn(replacement);
        when(authTokenPort.accessTokenDurationSeconds()).thenReturn(900L);
        when(mapper.toLoginResponseVO(user, "new-access", "new-refresh", 900L)).thenReturn(LoginResponseVO.builder().build());

        authBusiness.refreshToken(RefreshTokenRequestVO.builder().refreshToken("old-refresh").build());

        verify(refreshSessionPort).consumeIfActive(42L, NOW);
        verify(refreshSessionPort).setReplacement(42L, 43L);
        verify(refreshSessionPort, never()).revokeFamily(any(), any(), eq("refresh_replay_or_inactive"));
    }

    @Test
    void replayRevokesRefreshFamily() {
        RefreshSession current = session(42L, TOKEN_ID, FAMILY_ID);
        when(authTokenPort.decodeRefreshToken("replayed")).thenReturn(
                new RefreshTokenClaims(user.getEmail(), TOKEN_ID.toString(), FAMILY_ID.toString(), "7", "refresh", "REFRESH"));
        when(refreshSessionPort.findByTokenHashForUpdate(any(String.class))).thenReturn(Optional.of(current));
        when(refreshSessionPort.consumeIfActive(42L, NOW)).thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> authBusiness.refreshToken(RefreshTokenRequestVO.builder().refreshToken("replayed").build()));
        verify(refreshSessionPort).revokeFamily(FAMILY_ID, NOW, "refresh_replay_or_inactive");
    }

    @Test
    void invalidRefreshTypeIsRejectedBeforePersistenceLookup() {
        when(authTokenPort.decodeRefreshToken("access-token")).thenReturn(
                new RefreshTokenClaims(user.getEmail(), TOKEN_ID.toString(), FAMILY_ID.toString(), "7", "access", "ROLE_OPERATOR"));

        assertThrows(UnauthorizedException.class,
                () -> authBusiness.refreshToken(RefreshTokenRequestVO.builder().refreshToken("access-token").build()));
        verify(refreshSessionPort, never()).findByTokenHashForUpdate(any());
    }

    @Test
    void sessionClaimMismatchIsRejectedWithoutConsumption() {
        RefreshSession current = session(42L, TOKEN_ID, FAMILY_ID);
        when(authTokenPort.decodeRefreshToken("mismatch")).thenReturn(
                new RefreshTokenClaims(user.getEmail(), UUID.randomUUID().toString(), FAMILY_ID.toString(), "7", "refresh", "REFRESH"));
        when(refreshSessionPort.findByTokenHashForUpdate(any(String.class))).thenReturn(Optional.of(current));

        assertThrows(UnauthorizedException.class,
                () -> authBusiness.refreshToken(RefreshTokenRequestVO.builder().refreshToken("mismatch").build()));
        verify(refreshSessionPort, never()).consumeIfActive(any(), any());
    }

    @Test
    void logoutRevokesRefreshFamily() {
        RefreshSession current = session(42L, TOKEN_ID, FAMILY_ID);
        when(authTokenPort.decodeRefreshToken("logout-token")).thenReturn(
                new RefreshTokenClaims(user.getEmail(), TOKEN_ID.toString(), FAMILY_ID.toString(), "7", "refresh", "REFRESH"));
        when(refreshSessionPort.findByTokenHashForUpdate(any(String.class))).thenReturn(Optional.of(current));

        authBusiness.logout(RefreshTokenRequestVO.builder().refreshToken("logout-token").build());

        verify(refreshSessionPort).revokeFamily(FAMILY_ID, NOW, "logout");
    }

    private RefreshSession session(Long id, UUID tokenId, UUID familyId) {
        return RefreshSession.builder().id(id).user(user).tokenId(tokenId).familyId(familyId)
                .issuedAt(NOW.minusSeconds(60)).expiresAt(NOW.plusSeconds(600)).build();
    }
}
