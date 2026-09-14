package com.devmaster.goatfarm.goatownership.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;

/**
 * Transitional projection boundary for the legacy cabras.capril_id column.
 * Canonical ownership remains the open GoatOwnershipPeriod.
 */
public interface GoatCurrentOwnerProjectionPort {
    /**
     * Moves the projection only when it still points at the expected farm.
     *
     * @return true when exactly one Goat row was moved; false means drift or
     *         that the Goat row disappeared.
     */
    boolean moveFromTo(GoatId goatId, long expectedSourceFarmId, long targetFarmId);
}
