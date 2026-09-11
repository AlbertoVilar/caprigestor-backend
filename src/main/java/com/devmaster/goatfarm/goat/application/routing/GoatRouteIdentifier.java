package com.devmaster.goatfarm.goat.application.routing;

import com.devmaster.goatfarm.goat.domain.GoatId;

import java.util.Optional;

/**
 * Parses the explicit technical identifier vocabulary used by internal API
 * routes.
 *
 * <p>Route-token parsing belongs to the application/API boundary rather than
 * to the domain model. Legacy v1 paths still carry the RG directly. When a
 * client has a technical identity it must use {@code technical-{id}} so a
 * numeric RG can never be mistaken for a GoatId.</p>
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
