package com.devmaster.goatfarm.goat.domain;

import java.util.Optional;

/**
 * Explicit route token for structural GoatId lookups.
 *
 * <p>Legacy v1 paths still carry the RG directly.  When a client has a
 * technical identity it must use {@code technical-{id}} so a numeric RG can
 * never be mistaken for a GoatId.</p>
 */
public final class GoatRouteIdentifier {

    public static final String TECHNICAL_PREFIX = "technical-";

    private GoatRouteIdentifier() {
    }

    public static Optional<GoatId> technicalId(String token) {
        if (token == null || !token.startsWith(TECHNICAL_PREFIX)) {
            return Optional.empty();
        }

        String value = token.substring(TECHNICAL_PREFIX.length());
        if (!value.matches("\\d+")) {
            return Optional.empty();
        }

        try {
            return Optional.of(new GoatId(Long.parseLong(value)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
