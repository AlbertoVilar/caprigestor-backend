package com.devmaster.goatfarm.goatownership.api.mapper;

import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalEventDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalEventsResponseDTO;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalEventsSnapshot;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalOperationalEventItem;
import org.springframework.stereotype.Component;

/** Maps immutable-provenance event facts to the registry transport contract. */
@Component
public class FarmGoatHistoricalEventsApiMapper {

    public FarmGoatHistoricalEventsResponseDTO toResponse(FarmGoatHistoricalEventsSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new FarmGoatHistoricalEventsResponseDTO(
                snapshot.goatId().value(),
                snapshot.events().stream().map(this::toEventDto).toList()
        );
    }

    private FarmGoatHistoricalEventDTO toEventDto(HistoricalOperationalEventItem item) {
        return new FarmGoatHistoricalEventDTO(
                item.id(), item.recordingFarmId(), item.eventType(), item.date(), item.description(),
                item.location(), item.veterinarian(), item.outcome()
        );
    }
}
