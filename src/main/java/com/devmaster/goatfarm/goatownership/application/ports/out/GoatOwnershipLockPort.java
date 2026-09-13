package com.devmaster.goatfarm.goatownership.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.GoatOwnershipLockState;

import java.util.Optional;

/**
 * Acquires the ownership lock for a Goat. Callers must already be inside an
 * active transaction; this port does not create a transaction boundary.
 */
public interface GoatOwnershipLockPort {
    Optional<GoatOwnershipLockState> lockGoatOwnership(GoatId goatId);
}
