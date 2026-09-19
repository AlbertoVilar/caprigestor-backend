package com.devmaster.goatfarm.events.business.eventservice;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
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
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipGuardUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

/**
 * Farm-scoped event use cases. The legacy v1 route explicitly carries an RG;
 * the event relationship itself is always the immutable GoatId.
 */
@Service
@Transactional
public class EventBusiness implements EventManagementUseCase {

    private static final ZoneId OWNERSHIP_CALENDAR_ZONE = ZoneId.of("America/Sao_Paulo");

    private final EventPersistencePort eventPersistencePort;
    private final GoatReferenceQueryPort goatReferenceQueryPort;
    private final FarmAuthorizationUseCase ownershipService;
    private final EventPublisher eventPublisher;
    private final GoatOwnershipGuardUseCase goatOwnershipGuard;
    private final Clock clock;

    public EventBusiness(
            EventPersistencePort eventPersistencePort,
            GoatReferenceQueryPort goatReferenceQueryPort,
            FarmAuthorizationUseCase ownershipService,
            EventPublisher eventPublisher,
            GoatOwnershipGuardUseCase goatOwnershipGuard,
            Clock clock
    ) {
        this.eventPersistencePort = eventPersistencePort;
        this.goatReferenceQueryPort = goatReferenceQueryPort;
        this.ownershipService = ownershipService;
        this.eventPublisher = eventPublisher;
        this.goatOwnershipGuard = goatOwnershipGuard;
        this.clock = clock;
    }

    @Override
    public EventResponseVO createEvent(Long farmId, String registrationNumber, EventRequestVO request) {
        ownershipService.verifyFarmManagement(farmId);
        GoatReference goat = requireGoatForCreate(registrationNumber);
        requireRequestMatchesPath(request, registrationNumber);
        goatOwnershipGuard.requireCurrentFarm(goat.id(), farmId);
        requireProjectionMatchesFarm(goat, farmId);
        requireDateNotInFuture(request.date());
        goatOwnershipGuard.requireUnambiguousOwnershipOnDate(goat.id(), farmId, request.date());

        OperationalEvent saved = eventPersistencePort.save(OperationalEvent.create(
                toEventReference(goat), farmId, request.eventType(), request.date(), request.description(), request.location(),
                request.veterinarian(), request.outcome()));
        eventPublisher.publishEvent(toPublication(saved));
        return toResponse(saved);
    }

    @Override
    public EventResponseVO updateEvent(Long farmId, String registrationNumber, Long eventId, EventRequestVO request) {
        GoatReference goat = requireGoat(farmId, registrationNumber);
        ownershipService.verifyFarmOwnership(farmId);
        requireRequestMatchesPath(request, registrationNumber);
        goatOwnershipGuard.requireCurrentFarm(goat.id(), farmId);

        OperationalEvent existing = findEventByStructuralIdentity(eventId, goat);
        requireMutationProvenance(existing, farmId);
        requireDateNotInFuture(request.date());
        goatOwnershipGuard.requireUnambiguousOwnershipOnDate(goat.id(), existing.recordingFarmId(), request.date());
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
        return toResponse(findEventByStructuralIdentity(eventId, goat));
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
        goatOwnershipGuard.requireCurrentFarm(goat.id(), farmId);
        OperationalEvent existing = findEventByStructuralIdentity(eventId, goat);
        requireMutationProvenance(existing, farmId);
        eventPersistencePort.deleteById(eventId);
    }

    private GoatReference requireGoat(Long farmId, String registrationNumber) {
        return goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(registrationNumber, farmId)
                .orElseGet(() -> {
                    if (goatReferenceQueryPort.findReferenceByRegistrationNumber(registrationNumber).isPresent()) {
                        throw new AuthorizationDeniedException("Cabra não pertence à fazenda informada.");
                    }
                    throw new ResourceNotFoundException("Cabra não encontrada para a fazenda informada.");
                });
    }

    private GoatReference requireGoatForCreate(String registrationNumber) {
        return goatReferenceQueryPort.findReferenceByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada."));
    }

    private void requireProjectionMatchesFarm(GoatReference goat, Long farmId) {
        if (goat.farmId() == null || !goat.farmId().equals(farmId)) {
            throw new AuthorizationDeniedException(
                    "A projeção legada da cabra está divergente do contexto da fazenda informado.");
        }
    }

    private void requireDateNotInFuture(LocalDate date) {
        if (date.isAfter(LocalDate.now(clock.withZone(OWNERSHIP_CALENDAR_ZONE)))) {
            throw new InvalidArgumentException("date", "A data do evento não pode estar no futuro.");
        }
    }

    private GoatEventReference toEventReference(GoatReference goat) {
        return new GoatEventReference(goat.id(), goat.registrationNumber(), goat.name());
    }

    private OperationalEvent findEventByStructuralIdentity(Long eventId, GoatReference goat) {
        return eventPersistencePort.findByIdAndGoatId(eventId, goat.id())
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado para a cabra informada."));
    }

    private void requireMutationProvenance(OperationalEvent event, Long farmId) {
        if (event.recordingFarmId() == null || !event.recordingFarmId().equals(farmId)) {
            throw new AuthorizationDeniedException("A fazenda não possui autorização para alterar este evento.");
        }
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
                event.recordingFarmId(),
                OffsetDateTime.now().toString(),
                "system"
        );
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }
}
