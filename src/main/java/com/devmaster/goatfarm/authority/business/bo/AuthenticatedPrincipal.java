package com.devmaster.goatfarm.authority.business.bo;

import java.util.Set;

/**
 * Minimal identity used by security/application boundaries.
 * It deliberately contains no persistence or framework types.
 */
public record AuthenticatedPrincipal(
        Long id,
        String email,
        String name,
        Set<String> authorities
) {

    public AuthenticatedPrincipal {
        authorities = authorities == null ? Set.of() : Set.copyOf(authorities);
    }

    public boolean hasAuthority(String authority) {
        return authorities.contains(authority);
    }
}
