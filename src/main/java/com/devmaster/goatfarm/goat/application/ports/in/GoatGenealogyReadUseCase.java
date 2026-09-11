package com.devmaster.goatfarm.goat.application.ports.in;

import com.devmaster.goatfarm.goat.application.model.GoatGenealogySnapshot;
import java.util.Optional;

/**
 * Read boundary for local genealogy.
 *
 * <p>The current public genealogy route is an explicit RG route. It must not
 * infer a technical GoatId merely because a registration is numeric; future
 * technical routes will use a distinct, versioned contract.</p>
 */
public interface GoatGenealogyReadUseCase {

    /**
     * Loads the local family graph through technical parent references while
     * keeping registration values available for display and ABCC integration.
     */
    Optional<GoatGenealogySnapshot> findGenealogyByRegistrationNumberAndFarmId(
            String registrationNumber,
            Long farmId
    );
}
