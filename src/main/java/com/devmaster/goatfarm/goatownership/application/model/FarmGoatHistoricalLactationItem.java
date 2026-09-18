package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.milk.enums.LactationStatus;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Technology-neutral application read model representing one historical lactation.
 */
public record FarmGoatHistoricalLactationItem(
        Long id,
        GoatId goatId,
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
    public FarmGoatHistoricalLactationItem {
        Objects.requireNonNull(goatId, "goatId must not be null");
        Objects.requireNonNull(farmId, "farmId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        Objects.requireNonNull(startDate, "startDate must not be null");
    }
}
