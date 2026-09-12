package com.devmaster.goatfarm.events.domain;

import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.goat.domain.GoatId;

import java.time.LocalDate;

/**
 * Framework-free event record. GoatId is the structural link; RG and name are
 * business snapshots retained for compatibility, auditability and messaging.
 */
public record OperationalEvent(
        Long id,
        GoatId goatId,
        Long farmId,
        String goatRegistrationNumber,
        String goatName,
        EventType eventType,
        LocalDate date,
        String description,
        String location,
        String veterinarian,
        String outcome
) {
    public static OperationalEvent create(
            GoatEventReference goat,
            EventType eventType,
            LocalDate date,
            String description,
            String location,
            String veterinarian,
            String outcome
    ) {
        return new OperationalEvent(null, goat.id(), goat.farmId(), goat.registrationNumber(), goat.name(),
                eventType, date, description, location, veterinarian, outcome);
    }

    public OperationalEvent revise(
            EventType eventType,
            LocalDate date,
            String description,
            String location,
            String veterinarian,
            String outcome
    ) {
        return new OperationalEvent(id, goatId, farmId, goatRegistrationNumber, goatName,
                eventType, date, description, location, veterinarian, outcome);
    }
}
