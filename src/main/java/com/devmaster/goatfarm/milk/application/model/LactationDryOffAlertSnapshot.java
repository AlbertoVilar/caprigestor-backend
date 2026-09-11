package com.devmaster.goatfarm.milk.application.model;

import java.time.LocalDate;

/** Framework-free read model for dry-off alerts. */
public record LactationDryOffAlertSnapshot(
        Long lactationId,
        Long goatTechnicalId,
        String goatId,
        Integer dryAtPregnancyDays,
        LocalDate startDatePregnancy,
        LocalDate breedingDate,
        LocalDate confirmDate,
        LocalDate dryOffDate
) {
}
