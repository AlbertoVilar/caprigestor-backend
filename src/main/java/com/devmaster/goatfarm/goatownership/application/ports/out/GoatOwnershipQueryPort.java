package com.devmaster.goatfarm.goatownership.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GoatOwnershipQueryPort {
    Optional<Long> findCurrentOwnerFarmId(GoatId goatId);

    boolean isOwnedByFarmAt(GoatId goatId, Long farmId, Instant instant);

    List<GoatOwnershipPeriod> findOwnershipHistory(GoatId goatId);
}
