package com.devmaster.goatfarm.goat.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;

import java.util.Optional;

/**
 * Cross-module read boundary for resolving a local animal explicitly by one
 * identity type. Callers must never infer an identity type from a string.
 */
public interface GoatReferenceQueryPort {

    Optional<GoatReference> findReferenceByRegistrationNumber(String registrationNumber);

    Optional<GoatReference> findReferenceByRegistrationNumberAndFarmId(
            String registrationNumber,
            Long farmId
    );

    Optional<GoatReference> findReferenceByTechnicalIdAndFarmId(
            GoatId goatId,
            Long farmId
    );
}
