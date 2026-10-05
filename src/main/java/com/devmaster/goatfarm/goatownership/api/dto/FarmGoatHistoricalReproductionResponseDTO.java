package com.devmaster.goatfarm.goatownership.api.dto;

import java.util.List;

/**
 * Historical reproduction response DTO for the farm goat registry dossier.
 */
public record FarmGoatHistoricalReproductionResponseDTO(
        Long goatId,
        List<FarmGoatHistoricalReproductionProcessDTO> processes,
        List<FarmGoatHistoricalReproductionEventDTO> events
) {
    public FarmGoatHistoricalReproductionResponseDTO {
        processes = processes == null ? List.of() : List.copyOf(processes);
        events = events == null ? List.of() : List.copyOf(events);
    }
}
