package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.reproduction.enums.BreedingType;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Technology-neutral read model representing one historical reproductive event fact.
 */
public record HistoricalReproductionEventItem(
        Long id,
        GoatId goatId,
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
    public HistoricalReproductionEventItem {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(goatId, "goatId must not be null");
        Objects.requireNonNull(farmId, "farmId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(eventDate, "eventDate must not be null");
    }
}
