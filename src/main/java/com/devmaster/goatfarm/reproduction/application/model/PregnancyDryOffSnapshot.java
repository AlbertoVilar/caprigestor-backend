package com.devmaster.goatfarm.reproduction.application.model;

import java.time.LocalDate;

/** Minimal framework-free pregnancy read model for farm-wide dry-off alerts. */
public record PregnancyDryOffSnapshot(
        Long id,
        Long farmId,
        Long goatTechnicalId,
        String goatId,
        String status,
        LocalDate breedingDate,
        LocalDate confirmDate,
        LocalDate startDate,
        LocalDate closedAt
) {
    public boolean activeAsOf(LocalDate referenceDate) {
        return "ACTIVE".equalsIgnoreCase(status)
                && (closedAt == null || closedAt.isAfter(referenceDate));
    }
    public String goatKey() {
        return goatTechnicalId != null ? String.valueOf(goatTechnicalId) : goatId;
    }
}
