package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.reproduction.enums.BreedingType;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Minimal neutral context for a foreign coverage event that originated a pregnancy.
 */
public record MinimalForeignCoverageContext(
        Long coverageEventId,
        Long originFarmId,
        LocalDate coverageDate,
        BreedingType breedingType,
        String breederRef
) {
    public MinimalForeignCoverageContext {
        Objects.requireNonNull(coverageEventId, "coverageEventId must not be null");
        Objects.requireNonNull(originFarmId, "originFarmId must not be null");
    }
}
