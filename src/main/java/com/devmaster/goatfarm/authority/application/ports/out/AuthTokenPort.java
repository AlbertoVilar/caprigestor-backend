package com.devmaster.goatfarm.authority.application.ports.out;

import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.business.bo.IssuedRefreshToken;
import com.devmaster.goatfarm.authority.business.bo.RefreshTokenClaims;

import java.util.UUID;

/** Application boundary for issuing and reading authentication tokens. */
public interface AuthTokenPort {

    String issueAccessToken(AuthenticatedPrincipal principal);

    IssuedRefreshToken issueRefreshToken(AuthenticatedPrincipal principal, UUID familyId);

    long accessTokenDurationSeconds();

    RefreshTokenClaims decodeRefreshToken(String rawToken);
}
