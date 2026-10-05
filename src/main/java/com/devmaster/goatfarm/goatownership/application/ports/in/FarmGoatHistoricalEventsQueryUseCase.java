package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalEventsSnapshot;

import java.util.Optional;

/** Queries historical operational events for one farm registry relationship. */
public interface FarmGoatHistoricalEventsQueryUseCase {

    Optional<FarmGoatHistoricalEventsSnapshot> findHistoricalEvents(long farmId, GoatId goatId);
}
