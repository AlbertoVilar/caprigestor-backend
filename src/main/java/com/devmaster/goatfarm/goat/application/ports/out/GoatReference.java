package com.devmaster.goatfarm.goat.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;

/**
 * Minimal cross-module reference to a local animal.
 *
 * <p>The technical id is the structural relation. Registration and name are
 * snapshots supplied to consumers that still need a human/business identity.</p>
 */
public record GoatReference(
        GoatId id,
        Long farmId,
        String registrationNumber,
        String name
) {
}
