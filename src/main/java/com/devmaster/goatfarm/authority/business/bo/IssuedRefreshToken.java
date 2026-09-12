package com.devmaster.goatfarm.authority.business.bo;

import java.time.Instant;
import java.util.UUID;

/** Technology-neutral refresh token data required by the Authority use case. */
public record IssuedRefreshToken(
        String token,
        UUID tokenId,
        UUID familyId,
        Instant issuedAt,
        Instant expiresAt
) {
}
