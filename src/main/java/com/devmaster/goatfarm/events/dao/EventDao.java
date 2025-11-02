package com.devmaster.goatfarm.events.dao;

import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.model.entity.Event;
import com.devmaster.goatfarm.events.model.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class EventDao {

    @Autowired
    private EventRepository eventRepository;

    public Event saveEvent(Event event) {
        return eventRepository.save(event);
    }

    public Optional<Event> findEventById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> findEventsByGoatNumRegistro(String goatNumRegistration) {
        return eventRepository.findEventsByGoatNumRegistro(goatNumRegistration);
    }

    public Page<Event> findEventsByGoatWithFilters(String registrationNumber,
                                                   EventType eventType,
                                                   LocalDate startDate,
                                                   LocalDate endDate,
                                                   Pageable pageable) {
        return eventRepository.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate, pageable);
    }

    public void deleteEventById(Long id) {
        eventRepository.deleteById(id);
    }

    @Transactional
    public void deleteEventsFromOtherUsers(Long adminId) {
        eventRepository.deleteEventsFromOtherUsers(adminId);
    }
}
