package com.devmaster.goatfarm.goatownership.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalLactationItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalMilkProductionItem;

import java.util.List;

/**
 * Outbound port to query historical lactation and milk production records for a goat.
 * Implemented by the Milk module persistence adapter.
 */
public interface FarmGoatHistoricalMilkLactationQueryPort {

    /**
     * Loads all historical lactations for a goat by its technical id or registration number.
     */
    List<FarmGoatHistoricalLactationItem> findLactationsByGoat(GoatId goatId, String registrationNumber);

    /**
     * Loads all historical milk production records for a goat strictly attributed to the given farm.
     */
    List<FarmGoatHistoricalMilkProductionItem> findMilkProductionsByGoatAndFarm(long farmId, GoatId goatId, String registrationNumber);
}