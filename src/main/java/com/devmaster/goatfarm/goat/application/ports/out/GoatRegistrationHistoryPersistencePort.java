package com.devmaster.goatfarm.goat.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.domain.GoatRegistrationHistory;

import java.util.List;

/** Persistence boundary for the audited registration-rectification history. */
public interface GoatRegistrationHistoryPersistencePort {

    GoatRegistrationHistory save(GoatRegistrationHistory history);

    List<GoatRegistrationHistory> findByFarmIdAndGoatId(Long farmId, GoatId goatId);
}
