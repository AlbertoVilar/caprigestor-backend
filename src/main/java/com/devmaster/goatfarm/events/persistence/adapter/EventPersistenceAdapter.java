package com.devmaster.goatfarm.events.persistence.adapter;

import com.devmaster.goatfarm.events.application.ports.out.EventPage;
import com.devmaster.goatfarm.events.application.ports.out.EventPageQuery;
import com.devmaster.goatfarm.events.application.ports.out.EventPersistencePort;
import com.devmaster.goatfarm.events.domain.OperationalEvent;
import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.events.persistence.entity.Event;
import com.devmaster.goatfarm.events.persistence.repository.EventRepository;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/** JPA adapter that keeps the Event core independent from persistence types. */
@Component
public class EventPersistenceAdapter implements EventPersistencePort {

    private final EventRepository eventRepository;
    private final GoatRepository goatRepository;

    public EventPersistenceAdapter(EventRepository eventRepository, GoatRepository goatRepository) {
        this.eventRepository = eventRepository;
        this.goatRepository = goatRepository;
    }

    @Override
    public OperationalEvent save(OperationalEvent event) {
        GoatEntity goat = goatRepository.findByTechnicalId(event.goatId().value())
                .orElseThrow(() -> new IllegalStateException("Referência técnica da cabra não encontrada."));
        boolean isNew = event.id() == null;
        Event entity = isNew
                ? new Event()
                : eventRepository.findById(event.id())
                        .orElseThrow(() -> new IllegalStateException("Evento não encontrado para atualização."));

        if (isNew) {
            if (event.recordingFarmId() == null) {
                throw new IllegalStateException("Novo evento deve ter fazenda de registro.");
            }
            entity.setRecordingFarmId(event.recordingFarmId());
        } else if (!Objects.equals(entity.getRecordingFarmId(), event.recordingFarmId())) {
            throw new IllegalStateException("A fazenda de registro do evento não pode ser alterada.");
        }

        if (entity.getGoat() == null) {
            // Existing historical rows created before the technical shadow was
            // adopted are repaired at the application boundary. For a normal
            // update, keep the event's original RG snapshot untouched.
            entity.setGoat(goat);
        } else if (entity.getGoat().getTechnicalId() == null
                || event.goatId().value() != entity.getGoat().getTechnicalId()) {
            throw new IllegalStateException("O evento não pode mudar de animal.");
        }
        entity.setEventType(event.eventType());
        entity.setDate(event.date());
        entity.setDescription(event.description());
        entity.setLocation(event.location());
        entity.setVeterinarian(event.veterinarian());
        entity.setOutcome(event.outcome());

        return toDomain(eventRepository.save(entity));
    }

    @Override
    public Optional<OperationalEvent> findByIdAndGoatId(Long eventId, GoatId goatId) {
        if (goatId == null) {
            return Optional.empty();
        }
        return eventRepository.findByIdAndGoatTechnicalId(eventId, goatId.value())
                .map(this::toDomain);
    }

    @Override
    public EventPage<OperationalEvent> findByGoatIdWithFilters(
            GoatId goatId,
            Long farmId,
            EventType eventType,
            LocalDate startDate,
            LocalDate endDate,
            EventPageQuery pageQuery
    ) {
        Page<Event> page = eventRepository.findAllByGoatTechnicalIdAndFarmIdWithFilters(
                goatId.value(), farmId, eventType, startDate, endDate, toPageable(pageQuery));
        return new EventPage<>(page.getContent().stream().map(this::toDomain).toList(),
                page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @Override
    public void deleteById(Long id) {
        eventRepository.deleteById(id);
    }

    private OperationalEvent toDomain(Event entity) {
        GoatEntity goat = entity.getGoat();
        if (goat == null || goat.getTechnicalId() == null) {
            throw new IllegalStateException("Evento sem referência técnica de cabra.");
        }
        return new OperationalEvent(
                entity.getId(),
                GoatId.of(goat.getTechnicalId()),
                entity.getRecordingFarmId(),
                entity.getGoatRegistrationNumber(),
                goat.getName(),
                entity.getEventType(),
                entity.getDate(),
                entity.getDescription(),
                entity.getLocation(),
                entity.getVeterinarian(),
                entity.getOutcome()
        );
    }

    private Pageable toPageable(EventPageQuery query) {
        String property = "date";
        Sort.Direction direction = Sort.Direction.DESC;
        if (query.sort() != null && !query.sort().isBlank()) {
            String[] parts = query.sort().split(",", 2);
            property = switch (parts[0]) {
                case "id", "date", "eventType" -> parts[0];
                default -> "date";
            };
            if (parts.length == 2 && "ASC".equalsIgnoreCase(parts[1])) {
                direction = Sort.Direction.ASC;
            }
        }
        return PageRequest.of(query.page(), query.size(), Sort.by(direction, property));
    }
}
