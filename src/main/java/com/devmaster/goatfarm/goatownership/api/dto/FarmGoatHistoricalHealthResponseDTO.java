package com.devmaster.goatfarm.goatownership.api.dto;

import java.util.List;

/** Historical health section returned by the farm registry dossier API. */
public record FarmGoatHistoricalHealthResponseDTO(
        Long goatId,
        List<FarmGoatHistoricalHealthEventDTO> events
) {
    public FarmGoatHistoricalHealthResponseDTO {
        events = events == null ? List.of() : List.copyOf(events);
    }
}
