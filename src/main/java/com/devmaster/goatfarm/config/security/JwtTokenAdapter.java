package com.devmaster.goatfarm.config.security;

import com.devmaster.goatfarm.authority.application.ports.out.AuthTokenPort;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.business.bo.IssuedRefreshToken;
import com.devmaster.goatfarm.authority.business.bo.RefreshTokenClaims;
import com.devmaster.goatfarm.config.exceptions.custom.UnauthorizedException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Infrastructure adapter keeping JWT encoding/decoding behind AuthTokenPort.
 */
@Component
public class JwtTokenAdapter implements AuthTokenPort {

    private final JwtService jwtService;
    private final JwtDecoder refreshJwtDecoder;

    public JwtTokenAdapter(JwtService jwtService,
                           @Qualifier("refreshJwtDecoder") JwtDecoder refreshJwtDecoder) {
        this.jwtService = jwtService;
        this.refreshJwtDecoder = refreshJwtDecoder;
    }

    @Override
    public String issueAccessToken(AuthenticatedPrincipal principal) {
        return jwtService.generateToken(principal);
    }

    @Override
    public IssuedRefreshToken issueRefreshToken(AuthenticatedPrincipal principal, UUID familyId) {
        JwtService.IssuedRefreshToken issued = jwtService.issueRefreshToken(principal, familyId);
        return new IssuedRefreshToken(issued.token(), issued.tokenId(), issued.familyId(), issued.issuedAt(), issued.expiresAt());
    }

    @Override
    public long accessTokenDurationSeconds() {
        return jwtService.getAccessTokenDurationSeconds();
    }

    @Override
    public RefreshTokenClaims decodeRefreshToken(String rawToken) {
        try {
            Jwt jwt = refreshJwtDecoder.decode(rawToken);
            return new RefreshTokenClaims(
                    jwt.getSubject(),
                    jwt.getId(),
                    jwt.getClaimAsString("familyId"),
                    jwt.getClaimAsString("userId"),
                    jwt.getClaimAsString("typ"),
                    jwt.getClaimAsString("scope")
            );
        } catch (RuntimeException exception) {
            throw new UnauthorizedException("Token inválido ou expirado");
        }
    }
}
