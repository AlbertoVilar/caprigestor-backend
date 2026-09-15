package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.goat.domain.GoatId;

/**
 * Inbound ownership policies consumed by other application use cases.
 *
 * <p>The guard keeps ownership persistence details behind the ownership
 * module boundary while exposing only the business decisions required by
 * callers.</p>
 */
public interface GoatOwnershipGuardUseCase {

    void requireCurrentFarm(GoatId goatId, long expectedFarmId);

    void requireLastAssociatedFarm(GoatId goatId, long expectedFarmId);
}
