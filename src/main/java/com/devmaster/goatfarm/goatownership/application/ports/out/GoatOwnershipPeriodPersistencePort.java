package com.devmaster.goatfarm.goatownership.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;

import java.util.List;
import java.util.Optional;

public interface GoatOwnershipPeriodPersistencePort {
    GoatOwnershipPeriod save(GoatOwnershipPeriod period);

    Optional<GoatOwnershipPeriod> findOpenByGoatId(GoatId goatId);

    List<GoatOwnershipPeriod> findByGoatIdOrderByStartedAt(GoatId goatId);
}
