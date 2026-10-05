package com.devmaster.goatfarm.goatownership.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalReproductionEventItem;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalReproductionProcessCandidate;

import java.util.List;

/**
 * Outbound port to query historical candidate pregnancies and reproductive events for a goat.
 * Implemented by the Reproduction module persistence adapter.
 */
public interface FarmGoatHistoricalReproductionQueryPort {

    List<HistoricalReproductionProcessCandidate> findCandidateProcessesByGoat(GoatId goatId, String registrationNumber);

    List<HistoricalReproductionEventItem> findCandidateEventsByGoat(GoatId goatId, String registrationNumber);
}
