package com.devmaster.goatfarm.goatownership.api.dto;

import com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;

import java.time.LocalDate;

/**
 * Historical pregnancy process DTO exposed in the farm goat registry historical dossier.
 */
public record FarmGoatHistoricalReproductionProcessDTO(
        Long pregnancyId,
        Long processOriginFarmId,
        LocalDate breedingDate,
        LocalDate confirmDate,
        LocalDate expectedDueDate,
        Long coverageEventId,
        PregnancyStatus status,
        LocalDate closedAt,
        PregnancyCloseReason closeReason,
        FarmGoatHistoricalForeignCoverageContextDTO foreignCoverageContext
) {
}
