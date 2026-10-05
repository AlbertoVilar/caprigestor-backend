package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalReproductionSnapshot;

import java.util.Optional;

/**
 * Inbound port to query the historical reproduction dossier of a goat for an authorized farm.
 */
public interface FarmGoatHistoricalReproductionQueryUseCase {

    Optional<FarmGoatHistoricalReproductionSnapshot> findHistoricalReproduction(long farmId, GoatId goatId);
}
