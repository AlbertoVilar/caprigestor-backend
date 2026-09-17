package com.devmaster.goatfarm.goatownership.api.dto;

import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryDisposition;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryRole;

import java.time.LocalDate;
import java.util.Set;

/**
 * Dedicated response DTO for the farm goat historical dossier basic read endpoint.
 *
 * <p>Exposes only safe zootechnical identity, provenance and registry status.
 * Internal farm operational details (farmId, farmName, userName, exit notes,
 * current owner person name) are strictly omitted.</p>
 */
public record FarmGoatHistoricalDossierBasicResponseDTO(
        Long goatId,
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
