package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalMilkLactationSnapshot;

import java.util.Optional;

/**
 * Inbound port for querying historical lactation and milk production in a farm goat registry context.
 */
public interface FarmGoatHistoricalMilkLactationQueryUseCase {

    /**
     * Retrieves the historical lactation and milk production snapshot for the specified goat
     * if and only if the goat is part of the farm's registry universe.
     *
     * @param farmId the positive farm identifier
     * @param goatId the structural goat identifier
     * @return the snapshot if found and authorized by membership, empty otherwise
     */
    Optional<FarmGoatHistoricalMilkLactationSnapshot> findHistoricalMilkLactation(long farmId, GoatId goatId);
}
