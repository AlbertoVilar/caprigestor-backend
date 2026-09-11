package com.devmaster.goatfarm.events.business.bo;

import com.devmaster.goatfarm.events.enums.EventType;

import java.time.LocalDate;

/**
 * Stable event contract exposed by the application port. No JPA entity crosses
 * into infrastructure messaging.
 */
public record EventPublication(
        Long eventId,
        Long goatTechnicalId,
        String goatRegistrationNumber,
        String goatName,
        EventType eventType,
        LocalDate date,
        String description,
        String location,
        String veterinarian,
        String outcome,
        Long farmId,
        String publishedAt,
        String publishedBy
) {
}
