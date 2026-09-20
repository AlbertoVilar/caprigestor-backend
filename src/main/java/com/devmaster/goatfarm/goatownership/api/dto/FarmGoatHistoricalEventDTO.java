package com.devmaster.goatfarm.goatownership.api.dto;

import com.devmaster.goatfarm.events.enums.EventType;

import java.time.LocalDate;

/** Transport-only representation of a historical operational event fact. */
public record FarmGoatHistoricalEventDTO(
        Long id,
        Long recordingFarmId,
        EventType eventType,
        LocalDate date,
        String description,
        String location,
        String veterinarian,
        String outcome
) {
}
