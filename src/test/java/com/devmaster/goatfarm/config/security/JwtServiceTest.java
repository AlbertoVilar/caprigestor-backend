package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Test
    void shouldApplyConfiguredDurationsSeparatelyToAccessAndRefreshTokens() {
        when(jwtEncoder.encode(any())).thenReturn(encodedToken());
        JwtService service = new JwtService(jwtEncoder, 90, 180, "https://caprigestor.local", "caprigestor-api", "caprigestor-current");
        User user = user();
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);

        service.generateToken(user);
        service.issueRefreshToken(user, null);

        org.mockito.Mockito.verify(jwtEncoder, org.mockito.Mockito.times(2)).encode(captor.capture());
        JwtClaimsSet accessClaims = captor.getAllValues().getFirst().getClaims();
        JwtClaimsSet refreshClaims = captor.getAllValues().get(1).getClaims();

        assertEquals(90, Duration.between(accessClaims.getIssuedAt(), accessClaims.getExpiresAt()).toSeconds());
        assertEquals(180, Duration.between(refreshClaims.getIssuedAt(), refreshClaims.getExpiresAt()).toSeconds());
        assertEquals("REFRESH", refreshClaims.getClaimAsString("scope"));
        assertEquals("access", accessClaims.getClaimAsString("typ"));
        assertEquals("refresh", refreshClaims.getClaimAsString("typ"));
        assertEquals("https://caprigestor.local", accessClaims.getIssuer().toString());
        assertEquals("caprigestor-api", accessClaims.getAudience().getFirst());
    }

    @Test
    void shouldRejectNonPositiveConfiguredDurations() {
        assertThrows(IllegalArgumentException.class, () -> new JwtService(jwtEncoder, 0, 180, "https://caprigestor.local", "caprigestor-api", "caprigestor-current"));
        assertThrows(IllegalArgumentException.class, () -> new JwtService(jwtEncoder, 90, 0, "https://caprigestor.local", "caprigestor-api", "caprigestor-current"));
    }

    private Jwt encodedToken() {
        Instant now = Instant.now();
        return Jwt.withTokenValue("encoded-token")
                .header("alg", "RS256")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(1))
                .subject("test@example.com")
                .build();
    }

    private User user() {
        Role role = new Role();
        role.setAuthority("ROLE_OPERATOR");
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test user");
        user.addRole(role);
        return user;
    }
}
