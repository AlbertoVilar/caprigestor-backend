package com.devmaster.goatfarm.events.business.eventservice;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.events.application.ports.in.EventManagementUseCase;
import com.devmaster.goatfarm.events.application.ports.out.EventPage;
import com.devmaster.goatfarm.events.application.ports.out.EventPageQuery;
import com.devmaster.goatfarm.events.application.ports.out.EventPersistencePort;
import com.devmaster.goatfarm.events.application.ports.out.EventPublisher;
import com.devmaster.goatfarm.events.business.bo.EventPublication;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.domain.GoatEventReference;
import com.devmaster.goatfarm.events.domain.OperationalEvent;
import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Farm-scoped event use cases. The legacy v1 route explicitly carries an RG;
 * the event relationship itself is always the immutable GoatId.
 */
@Service
@Transactional
public class EventBusiness implements EventManagementUseCase {

    private final EventPersistencePort eventPersistencePort;
    private final GoatReferenceQueryPort goatReferenceQueryPort;
    private final FarmAuthorizationUseCase ownershipService;
    private final EventPublisher eventPublisher;

    public EventBusiness(
            EventPersistencePort eventPersistencePort,
            GoatReferenceQueryPort goatReferenceQueryPort,
            FarmAuthorizationUseCase ownershipService,
            EventPublisher eventPublisher
    ) {
        this.eventPersistencePort = eventPersistencePort;
        this.goatReferenceQueryPort = goatReferenceQueryPort;
        this.ownershipService = ownershipService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public EventResponseVO createEvent(Long farmId, String registrationNumber, EventRequestVO request) {
        GoatReference goat = requireGoat(farmId, registrationNumber);
        ownershipService.verifyFarmManagement(farmId);
        requireRequestMatchesPath(request, registrationNumber);

        OperationalEvent saved = eventPersistencePort.save(OperationalEvent.create(
                toEventReference(goat), request.eventType(), request.date(), request.description(), request.location(),
                request.veterinarian(), request.outcome()));
        eventPublisher.publishEvent(toPublication(saved));
        return toResponse(saved);
    }

    @Override
    public EventResponseVO updateEvent(Long farmId, String registrationNumber, Long eventId, EventRequestVO request) {
        GoatReference goat = requireGoat(farmId, registrationNumber);
        ownershipService.verifyFarmOwnership(farmId);
        requireRequestMatchesPath(request, registrationNumber);

        OperationalEvent existing = findEvent(eventId, goat, farmId);
        OperationalEvent updated = eventPersistencePort.save(existing.revise(
                request.eventType(), request.date(), request.description(), request.location(),
                request.veterinarian(), request.outcome()));
        return toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponseVO findEventById(Long farmId, String registrationNumber, Long eventId) {
        GoatReference goat = requireGoat(farmId, registrationNumber);
        ownershipService.verifyFarmManagement(farmId);
        return toResponse(findEvent(eventId, goat, farmId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseVO> findEventsByGoat(Long farmId, String registrationNumber) {
        return findEventsWithFilters(farmId, registrationNumber, null, null, null,
                new EventPageQuery(0, Integer.MAX_VALUE, "date,DESC")).content();
    }

    @Override
    @Transactional(readOnly = true)
    public EventPage<EventResponseVO> findEventsWithFilters(
            Long farmId,
            String registrationNumber,
            EventType eventType,
            LocalDate startDate,
            LocalDate endDate,
            EventPageQuery pageQuery
    ) {
        GoatReference goat = requireGoat(farmId, registrationNumber);
        ownershipService.verifyFarmManagement(farmId);
        EventPage<OperationalEvent> events = eventPersistencePort.findByGoatIdWithFilters(
                goat.id(), farmId, eventType, startDate, endDate, pageQuery);
        return new EventPage<>(events.content().stream().map(this::toResponse).toList(),
                events.totalElements(), events.page(), events.size());
    }

    @Override
    public void deleteEvent(Long farmId, String registrationNumber, Long eventId) {
        GoatReference goat = requireGoat(farmId, registrationNumber);
        ownershipService.verifyFarmOwnership(farmId);
        findEvent(eventId, goat, farmId);
        eventPersistencePort.deleteById(eventId);
    }

    private GoatReference requireGoat(Long farmId, String registrationNumber) {
        return goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(registrationNumber, farmId)
                .orElseGet(() -> {
                    if (goatReferenceQueryPort.findReferenceByRegistrationNumber(registrationNumber).isPresent()) {
                        throw new AccessDeniedException("Cabra não pertence à fazenda informada.");
                    }
                    throw new ResourceNotFoundException("Cabra não encontrada para a fazenda informada.");
                });
    }

    private GoatEventReference toEventReference(GoatReference goat) {
        return new GoatEventReference(goat.id(), goat.farmId(), goat.registrationNumber(), goat.name());
    }

    private OperationalEvent findEvent(Long eventId, GoatReference goat, Long farmId) {
        return eventPersistencePort.findByIdAndGoatIdAndFarmId(eventId, goat.id(), farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado para a cabra informada."));
    }

    private void requireRequestMatchesPath(EventRequestVO request, String registrationNumber) {
        if (request == null || !normalize(registrationNumber).equals(normalize(request.goatId()))) {
            throw new InvalidArgumentException("goatId", "O animal informado no corpo deve coincidir com o animal da URL.");
        }
    }

    private EventResponseVO toResponse(OperationalEvent event) {
        return new EventResponseVO(
                event.id(),
                event.goatId().value(),
                event.goatRegistrationNumber(),
                event.goatName(),
                event.eventType(),
                event.date(),
                event.description(),
                event.location(),
                event.veterinarian(),
                event.outcome()
        );
    }

    private EventPublication toPublication(OperationalEvent event) {
        return new EventPublication(
                event.id(),
                event.goatId().value(),
                event.goatRegistrationNumber(),
                event.goatName(),
                event.eventType(),
                event.date(),
                event.description(),
                event.location(),
                event.veterinarian(),
                event.outcome(),
                event.farmId(),
                OffsetDateTime.now().toString(),
                "system"
        );
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }
}
