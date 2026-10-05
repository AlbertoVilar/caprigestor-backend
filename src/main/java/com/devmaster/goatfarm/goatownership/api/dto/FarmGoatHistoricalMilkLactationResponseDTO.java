package com.devmaster.goatfarm.goatownership.api.dto;

import java.util.List;

/**
 * Historical milk and lactation response DTO for the farm goat registry dossier.
 */
public record FarmGoatHistoricalMilkLactationResponseDTO(
        Long goatId,
        List<FarmGoatHistoricalLactationDTO> lactations,
        List<FarmGoatHistoricalMilkProductionDTO> milkProductions
) {
    public FarmGoatHistoricalMilkLactationResponseDTO {
        lactations = lactations == null ? List.of() : List.copyOf(lactations);
        milkProductions = milkProductions == null ? List.of() : List.copyOf(milkProductions);
    }
}
