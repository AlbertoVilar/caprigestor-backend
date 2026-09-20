package com.devmaster.goatfarm.goatownership.api.dto;

import java.util.List;

/** Historical events section returned by the farm registry dossier API. */
public record FarmGoatHistoricalEventsResponseDTO(
        Long goatId,
        List<FarmGoatHistoricalEventDTO> events
) {
    public FarmGoatHistoricalEventsResponseDTO {
        events = events == null ? List.of() : List.copyOf(events);
    }
}
