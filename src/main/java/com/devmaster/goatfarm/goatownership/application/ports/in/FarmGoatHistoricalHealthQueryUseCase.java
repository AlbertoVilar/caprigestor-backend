package com.devmaster.goatfarm.goatownership.application.ports.in;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalHealthSnapshot;

import java.util.Optional;

/**
 * Inbound use case for querying the historical health dossier of a goat
 * in the context of a specific farm registry.
 */
public interface FarmGoatHistoricalHealthQueryUseCase {

    /**
     * Retrieves the historical health snapshot for a goat in the context of a requesting farm.
     *
     * @param farmId the identifier of the requesting farm
     * @param goatId the structural identifier of the goat
     * @return the historical health snapshot if the goat is in the farm's registry, or empty if not related
     */
    Optional<FarmGoatHistoricalHealthSnapshot> findHistoricalHealth(long farmId, GoatId goatId);
}
