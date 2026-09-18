package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Technology-neutral read model representing one historical pregnancy/reproductive process.
 * Sanitized to expose only fields authorized for the requesting farm.
 */
public record HistoricalReproductionProcessItem(
        Long pregnancyId,
        GoatId goatId,
        Long processOriginFarmId,
        LocalDate breedingDate,
        LocalDate confirmDate,
        LocalDate expectedDueDate,
        Long coverageEventId,
        PregnancyStatus status,
        LocalDate closedAt,
        PregnancyCloseReason closeReason,
        MinimalForeignCoverageContext foreignCoverageContext
) {
    public HistoricalReproductionProcessItem {
        Objects.requireNonNull(pregnancyId, "pregnancyId must not be null");
        Objects.requireNonNull(goatId, "goatId must not be null");
        Objects.requireNonNull(processOriginFarmId, "processOriginFarmId must not be null");
    }
}
