package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.goat.domain.GoatId;

import java.time.LocalDate;

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

    /**
     * Requires the expected farm to own the Goat for the complete civil day
     * represented by the supplied date. The sole partial-day exception is the
     * first and only open canonical ownership period when it begins during
     * that civil day and its entry type is {@code BIRTH}, {@code MANUAL_IMPORT},
     * or {@code ABCC_IMPORT}. A day split by an ownership transfer remains
     * ambiguous and is rejected for both farms. The concrete ownership
     * calendar zone is a domain policy of the ownership module and is
     * intentionally not supplied by callers.
     */
    void requireUnambiguousOwnershipOnDate(GoatId goatId, long expectedFarmId, LocalDate date);
}
