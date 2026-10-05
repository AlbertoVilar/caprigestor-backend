package com.devmaster.goatfarm.goatownership.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;

import java.util.List;
import java.util.Optional;

public interface GoatOwnershipPeriodPersistencePort {
    GoatOwnershipPeriod save(GoatOwnershipPeriod period);

    /**
     * Persists an ownership handoff inside the caller's transaction. The
     * adapter must physically close the old open period before inserting the
     * new one, keeping the unique-open-period invariant safe.
     */
    GoatOwnershipPeriod handoff(GoatOwnershipPeriod closedSource, GoatOwnershipPeriod openedTarget);

    Optional<GoatOwnershipPeriod> findOpenByGoatId(GoatId goatId);

    List<GoatOwnershipPeriod> findByGoatIdOrderByStartedAt(GoatId goatId);
}
