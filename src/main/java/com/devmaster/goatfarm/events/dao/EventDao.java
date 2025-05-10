package com.devmaster.goatfarm.events.dao;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.converter.EventEntityConverter;
import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.model.entity.Event;
import com.devmaster.goatfarm.events.model.repository.EventRepository;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventDao {

    @Autowired
    public EventRepository eventRepository;

    @Autowired
    public GoatRepository goatRepository;

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


    @Transactional(readOnly = true)
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

    @Transactional
    public EventResponseVO createEvent(EventRequestVO requestVO, String goatRegistrationNumber) {

        if (!StringUtils.hasText(goatRegistrationNumber)) {
            throw new ResourceNotFoundException("O número de registro da cabra não pode ser nulo ou vazio");
        }

        if (requestVO == null) {
            throw new ResourceNotFoundException("Evento vazio.");
        }

        Goat goat = goatRepository.findById(goatRegistrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado: " + goatRegistrationNumber));

        Event event = eventRepository.save(EventEntityConverter.toEntity(requestVO, goat));
        return EventEntityConverter.toResponseVO(event);
    }

}
