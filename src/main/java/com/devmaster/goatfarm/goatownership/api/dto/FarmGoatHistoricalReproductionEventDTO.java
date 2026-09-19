package com.devmaster.goatfarm.goatownership.api.dto;

import com.devmaster.goatfarm.reproduction.enums.BreedingType;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;

import java.time.LocalDate;

/**
 * Historical reproductive event DTO exposed in the farm goat registry historical dossier.
 */
public record FarmGoatHistoricalReproductionEventDTO(
        Long id,
        Long farmId,
        ReproductiveEventType eventType,
        LocalDate eventDate,
        BreedingType breedingType,
        String breederRef,
        Long pregnancyId,
        Long relatedEventId,
        LocalDate correctedEventDate,
        LocalDate checkScheduledDate,
        PregnancyCheckResult checkResult,
        String notes
) {
}
