package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.goat.domain.GoatId;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Technology-neutral historical operational event fact. Its farm attribution
 * is the immutable recording-farm provenance persisted with the event.
 */
public record HistoricalOperationalEventItem(
        Long id,
        GoatId goatId,
        Long recordingFarmId,
        EventType eventType,
        LocalDate date,
        String description,
        String location,
        String veterinarian,
        String outcome
) {
    public HistoricalOperationalEventItem {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(goatId, "goatId must not be null");
        Objects.requireNonNull(recordingFarmId, "recordingFarmId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(date, "date must not be null");
    }
}
