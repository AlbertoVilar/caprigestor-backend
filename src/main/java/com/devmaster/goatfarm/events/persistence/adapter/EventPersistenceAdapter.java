package com.devmaster.goatfarm.events.persistence.adapter;

import com.devmaster.goatfarm.events.application.ports.out.EventPersistencePort;
import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.events.persistence.entity.Event;
import com.devmaster.goatfarm.events.persistence.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Adaptador de persistência para eventos
 * Implementa a porta de saída EventPersistencePort usando Spring Data JPA
 */
@Component
public class EventPersistenceAdapter implements EventPersistencePort {

    private final EventRepository eventRepository;

    public EventPersistenceAdapter(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public Event save(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    public List<Event> findByGoatRegistrationNumber(String goatRegistrationNumber) {
        return eventRepository.findEventsByGoatRegistrationNumber(goatRegistrationNumber);
    }

    @Override
    public Page<Event> findWithFilters(String registrationNumber,
                                      EventType eventType,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      Pageable pageable) {
        return eventRepository.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate, pageable);
    }

    @Override
    public void deleteById(Long id) {
        eventRepository.deleteById(id);
    }

    @Override
    public void deleteEventsFromOtherUsers(Long adminId) {
        eventRepository.deleteEventsFromOtherUsers(adminId);
    }

    @Override
    public boolean existsById(Long id) {
        return eventRepository.existsById(id);
    }
}