package com.devmaster.goatfarm.goatownership.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalOperationalEventItem;

import java.util.List;

/** Boundary owned by goatownership for immutable-provenance event reads. */
public interface FarmGoatHistoricalEventsQueryPort {

    List<HistoricalOperationalEventItem> findHistoricalEvents(GoatId goatId, long requestingFarmId);
}
