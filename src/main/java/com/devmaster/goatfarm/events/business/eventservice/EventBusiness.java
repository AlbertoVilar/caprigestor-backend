package com.devmaster.goatfarm.events.business.eventservice;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.events.application.ports.in.EventManagementUseCase;
import com.devmaster.goatfarm.events.application.ports.out.EventPersistencePort;
import com.devmaster.goatfarm.events.application.ports.out.EventPublisher;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.events.business.mapper.EventBusinessMapper;
import com.devmaster.goatfarm.events.persistence.entity.Event;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EventBusiness implements EventManagementUseCase {

    private final EventPersistencePort eventPersistencePort;
    private final GoatPersistencePort goatPersistencePort;
    private final OwnershipService ownershipService;
    private final EventBusinessMapper eventMapper;
    private final EventPublisher eventPublisher;

    public EventBusiness(EventPersistencePort eventPersistencePort,
                         GoatPersistencePort goatPersistencePort,
                         OwnershipService ownershipService,
                         EventBusinessMapper eventMapper,
                         EventPublisher eventPublisher) {
        this.eventPersistencePort = eventPersistencePort;
        this.goatPersistencePort = goatPersistencePort;
        this.ownershipService = ownershipService;
        this.eventMapper = eventMapper;
        this.eventPublisher = eventPublisher;
    }
    @Override
    public EventResponseVO createEvent(EventRequestVO requestVO, String goatRegistrationNumber) {
        Goat goat = goatPersistencePort.findByRegistrationNumber(goatRegistrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada: " + goatRegistrationNumber));

        verifyFarmOwnership(goat);

        Event event = eventMapper.toEntity(requestVO);
        event.setGoat(goat);
        event = eventPersistencePort.save(event);
        // Publicar evento de forma assíncrona
        eventPublisher.publishEvent(event);
        return eventMapper.toResponseVO(event);
    }

    @Override
    public EventResponseVO updateEvent(Long id, EventRequestVO requestVO, String goatRegistrationNumber) {
        Goat goat = goatPersistencePort.findByRegistrationNumber(goatRegistrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada: " + goatRegistrationNumber));

        Event event = eventPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + id));

        if (!Optional.ofNullable(event.getGoat())
                .map(Goat::getRegistrationNumber)
                .orElse("").equals(goatRegistrationNumber)) {
            throw new ResourceNotFoundException("Este evento não pertence à cabra de registro: " + goatRegistrationNumber);
        }

        verifyFarmOwnership(goat);

        eventMapper.updateEntity(event, requestVO);
        Event updatedEvent = eventPersistencePort.save(event);
        return eventMapper.toResponseVO(updatedEvent);
    }

    @Transactional(readOnly = true)
    public EventResponseVO findEventById(Long eventId) {
        Event event = eventPersistencePort.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + eventId));
        verifyFarmOwnership(event.getGoat());
        return eventMapper.toResponseVO(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponseVO> findEventsByGoat(String goatNumRegistration) {
        Goat goat = goatPersistencePort.findByRegistrationNumber(goatNumRegistration)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada: " + goatNumRegistration));

        verifyFarmOwnership(goat);

        List<Event> events = eventPersistencePort.findByGoatRegistrationNumber(goatNumRegistration);
        if (events.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum evento encontrado para a cabra com número de registro: " + goatNumRegistration);
        }
        return events.stream().map(eventMapper::toResponseVO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponseVO> findEventsWithFilters(String registrationNumber,
                                                      EventType eventType,
                                                      LocalDate startDate,
                                                      LocalDate endDate,
                                                      Pageable pageable) {
        Goat goat = goatPersistencePort.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada: " + registrationNumber));

        verifyFarmOwnership(goat);

        Page<Event> events = eventPersistencePort.findWithFilters(registrationNumber, eventType, startDate, endDate, pageable);
        return events.map(eventMapper::toResponseVO);
    }

    @Override
    public void deleteEvent(Long id) {
        Event event = eventPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + id));
        verifyFarmOwnership(event.getGoat());
        eventPersistencePort.deleteById(id);
    }

    @Override
    public void deleteEventsFromOtherUsers(Long adminId) {
        eventPersistencePort.deleteEventsFromOtherUsers(adminId);
    }

    private void verifyFarmOwnership(Goat goat) {
        ownershipService.verifyGoatOwnership(goat.getFarm().getId(), goat.getRegistrationNumber());
    }
}
