package com.devmaster.goatfarm.goatownership.api.dto;

import com.devmaster.goatfarm.reproduction.enums.BreedingType;

import java.time.LocalDate;

/**
 * Minimal neutral context for a foreign coverage event that originated a pregnancy.
 */
public record FarmGoatHistoricalForeignCoverageContextDTO(
        Long coverageEventId,
        Long originFarmId,
        LocalDate coverageDate,
        BreedingType breedingType,
        String breederRef
) {
}
