package com.devmaster.goatfarm.goat.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.Gender;

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
        String name,
        Gender gender
) {

    /** Compatibility constructor for consumers that only need display data. */
    public GoatReference(GoatId id, Long farmId, String registrationNumber, String name) {
        this(id, farmId, registrationNumber, name, null);
    }
}
