package com.devmaster.goatfarm.goat.application.ports.out;

import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatStatus;

import java.util.Optional;

/**
 * Read-only projection required by cross-module goat validation.
 */
public interface GoatValidationQueryPort {

    Optional<GoatValidationSnapshot> findForValidation(String registrationNumber, Long farmId);

    record GoatValidationSnapshot(
            String registrationNumber,
            Gender gender,
            GoatStatus status
    ) {
    }
}
