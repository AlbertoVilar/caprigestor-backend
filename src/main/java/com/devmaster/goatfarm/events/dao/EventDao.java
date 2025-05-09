package com.devmaster.goatfarm.events.dao;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.converter.EventEntityConverter;
import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.model.entity.Event;
import com.devmaster.goatfarm.events.model.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventDao {

    @Autowired
    public EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<EventResponseVO> findEventByGoat(String goatNumRegistration) {
        if (goatNumRegistration == null || goatNumRegistration.trim().isEmpty()) {
            throw new IllegalArgumentException("O número de registro da cabra não pode ser nulo ou vazio.");
        }

        List<Event> events = eventRepository.findEventsByGoatNumRegistro(goatNumRegistration);
        if (events.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum evento encontrado para a cabra com número de registro: " + goatNumRegistration);
        }
        return events.stream().map(EventEntityConverter::toResponseVO).toList();
    }

    public List<EventResponseVO> findEventsByGoatWithFilters(String registrationNumber,
                                                             EventType eventType,
                                                             LocalDate startDate,
                                                             LocalDate endDate) {
        if (registrationNumber.isEmpty()) {
            throw new IllegalArgumentException("O número de registro da cabra não pode ser nulo ou vazio");
        }

        List<Event> events = eventRepository.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate);
        if (events.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum evento encontrado para a cabra com os filtros fornecidos.");
        }
        return events.stream().map(EventEntityConverter::toResponseVO).toList();
    }
}
