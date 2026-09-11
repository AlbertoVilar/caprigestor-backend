package com.devmaster.goatfarm.goat.application.routing;

import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Resolves the public v1 goat token vocabulary at the application boundary.
 *
 * <p>Technical identifiers are explicit ({@code technical-<id>}); every other
 * token is deliberately treated as a registration number. This keeps the
 * compatibility policy in one place and prevents business modules from
 * depending on persistence details or duplicating token parsing.</p>
 */
@Component
public class GoatReferenceResolver {

    private final GoatReferenceQueryPort referenceQueryPort;

    public GoatReferenceResolver(GoatReferenceQueryPort referenceQueryPort) {
        this.referenceQueryPort = referenceQueryPort;
    }

    public Optional<GoatReference> resolve(String routeToken, Long farmId) {
        if (routeToken == null || farmId == null) {
            return Optional.empty();
        }

        return GoatRouteIdentifier.technicalId(routeToken)
                .flatMap(id -> referenceQueryPort.findReferenceByTechnicalIdAndFarmId(id, farmId))
                .or(() -> referenceQueryPort.findReferenceByRegistrationNumberAndFarmId(routeToken, farmId));
    }
}
