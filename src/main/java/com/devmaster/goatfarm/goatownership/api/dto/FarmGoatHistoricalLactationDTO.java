package com.devmaster.goatfarm.goatownership.api.dto;

import com.devmaster.goatfarm.milk.enums.LactationStatus;

import java.time.LocalDate;

/**
 * Historical lactation record exposed in the farm goat registry historical dossier.
 */
public record FarmGoatHistoricalLactationDTO(
        Long id,
        Long goatId,
        Long farmId,
        LactationStatus status,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate pregnancyStartDate,
        LocalDate dryStartDate,
        Integer dryAtPregnancyDays,
        Integer restDays,
        boolean active
) {
}
