package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenAdapterTest {

    @Test
    void mapsOnlyRequiredValidatedRefreshClaimsToApplicationModel() {
        JwtService jwtService = mock(JwtService.class);
        JwtDecoder decoder = mock(JwtDecoder.class);
        UUID tokenId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("refresh")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(600))
                .subject("user@example.com")
                .claim("jti", tokenId.toString())
                .claim("familyId", familyId.toString())
                .claim("userId", 12L)
                .claim("typ", "refresh")
                .claim("scope", "REFRESH")
                .build();
        when(decoder.decode("refresh")).thenReturn(jwt);

        var claims = new JwtTokenAdapter(jwtService, decoder).decodeRefreshToken("refresh");

        assertThat(claims.subject()).isEqualTo("user@example.com");
        assertThat(claims.tokenId()).isEqualTo(tokenId.toString());
        assertThat(claims.familyId()).isEqualTo(familyId.toString());
        assertThat(claims.userId()).isEqualTo("12");
        assertThat(claims.type()).isEqualTo("refresh");
        assertThat(claims.scope()).isEqualTo("REFRESH");
    }

    @Test
    void translatesDecoderFailureToApplicationUnauthorizedException() {
        JwtService jwtService = mock(JwtService.class);
        JwtDecoder decoder = mock(JwtDecoder.class);
        when(decoder.decode("invalid")).thenThrow(new IllegalArgumentException("invalid token"));

        assertThrows(UnauthorizedException.class,
                () -> new JwtTokenAdapter(jwtService, decoder).decodeRefreshToken("invalid"));
    }
}
