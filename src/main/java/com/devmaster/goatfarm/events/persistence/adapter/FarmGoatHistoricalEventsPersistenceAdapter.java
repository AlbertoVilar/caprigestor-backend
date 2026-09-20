package com.devmaster.goatfarm.events.persistence.adapter;

import com.devmaster.goatfarm.events.persistence.entity.Event;
import com.devmaster.goatfarm.events.persistence.repository.EventRepository;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalOperationalEventItem;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalEventsQueryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/** Event-owned adapter for immutable recording-provenance historical reads. */
@Component
public class FarmGoatHistoricalEventsPersistenceAdapter implements FarmGoatHistoricalEventsQueryPort {

    private final EventRepository eventRepository;

    public FarmGoatHistoricalEventsPersistenceAdapter(EventRepository eventRepository) {
        this.eventRepository = Objects.requireNonNull(eventRepository, "eventRepository must not be null");
    }

    @Override
    public List<HistoricalOperationalEventItem> findHistoricalEvents(GoatId goatId, long requestingFarmId) {
        if (goatId == null || requestingFarmId <= 0) {
            return List.of();
        }

        return eventRepository.findHistoricalEventsByGoatTechnicalIdAndRecordingFarmId(goatId.value(), requestingFarmId)
                .stream()
                .map(this::toItem)
                .toList();
    }

    private HistoricalOperationalEventItem toItem(Event event) {
        return new HistoricalOperationalEventItem(
                event.getId(),
                GoatId.of(event.getGoatTechnicalId()),
                event.getRecordingFarmId(),
                event.getEventType(),
                event.getDate(),
                event.getDescription(),
                event.getLocation(),
                event.getVeterinarian(),
                event.getOutcome()
        );
    }
}
