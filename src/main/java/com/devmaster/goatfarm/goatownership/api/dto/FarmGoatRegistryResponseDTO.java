package com.devmaster.goatfarm.goatownership.api.dto;

import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryDisposition;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryRole;

import java.util.Set;

public record FarmGoatRegistryResponseDTO(
        Long goatId,
        String registrationNumber,
        String name,
        GoatStatus globalStatus,
        Long creatorFarmId,
        String creatorNameSnapshot,
        Set<FarmGoatRegistryRole> roles,
        FarmGoatRegistryDisposition disposition,
        Long currentOwnerFarmId
) {
}
