package com.devmaster.goatfarm.authority.business.bo;

/**
 * Validated refresh-token metadata exposed to the application boundary.
 * Claim values remain plain strings so business validation can preserve the
 * existing malformed-claim behavior before matching a persisted session.
 */
public record RefreshTokenClaims(
        String subject,
        String tokenId,
        String familyId,
        String userId,
        String type,
        String scope
) {
}
