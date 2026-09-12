package com.devmaster.goatfarm.events.domain;

import com.devmaster.goatfarm.goat.domain.GoatId;

/**
 * Domain-level reference used when creating an operational event.
 *
 * <p>This is deliberately local to the events bounded context. It carries the
 * technical relationship and the business snapshots needed by the event
 * aggregate without making the domain depend on a Goat application port.</p>
 */
public record GoatEventReference(
        GoatId id,
        Long farmId,
        String registrationNumber,
        String name
) {
}
