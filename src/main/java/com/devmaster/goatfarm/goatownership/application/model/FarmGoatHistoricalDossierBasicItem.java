package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;

import java.time.LocalDate;
import java.util.Set;

/**
 * Neutral application model representing a goat's basic zootechnical identity
 * within a farm registry context.
 */
public record FarmGoatHistoricalDossierBasicItem(
        GoatId goatId,
        String registrationNumber,
        String name,
        GoatStatus globalStatus,
        Gender gender,
        GoatBreed breed,
        String color,
        LocalDate birthDate,
        Category category,
        String tod,
        String toe,
        String fatherName,
        String fatherRegistrationNumber,
        String motherName,
        String motherRegistrationNumber,
        Long creatorFarmId,
        String creatorNameSnapshot,
        Set<FarmGoatRegistryRole> roles,
        FarmGoatRegistryDisposition disposition,
        Long currentOwnerFarmId
) {
}
