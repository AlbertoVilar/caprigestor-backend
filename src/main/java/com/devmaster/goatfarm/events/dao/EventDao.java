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
import java.util.Objects;

@Service
public class EventDao {

    @Autowired
    public EventRepository eventRepository;

    @Autowired
    public GoatRepository goatRepository;

    //CREATE
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

    //UPDATE
    @Transactional
    public EventResponseVO updateGoatEvent(Long id, EventRequestVO requestVO, String goatRegistrationNumber) {

        // Validação dos parâmetros
        if (!StringUtils.hasText(goatRegistrationNumber)) {
            throw new ResourceNotFoundException("O número de registro da cabra não pode ser nulo ou vazio.");
        }

        if (Objects.isNull(id)) {
            throw new ResourceNotFoundException("O ID do evento não pode ser nulo.");
        }

        if (Objects.isNull(requestVO)) {
            throw new IllegalArgumentException("Evento não pode ser nulo.");
        }

        // Busca da cabra associada ao evento
        Goat goat = goatRepository.findById(goatRegistrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado: " + goatRegistrationNumber));

        // Busca do evento que será atualizado
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + id));

        // (Opcional, mas recomendável) Validar se o evento pertence à cabra
        if (!event.getGoat().getRegistrationNumber().equals(goatRegistrationNumber)) {
            throw new ResourceNotFoundException("Este evento não pertence à cabra de registro: " + goatRegistrationNumber);
        }
        // Atualiza os dados do evento
        EventEntityConverter.toUpdateEvent(event,  requestVO);

        // Persiste as alterações
        eventRepository.save(event);

        // Retorna o resultado como VO
        return EventEntityConverter.toResponseVO(event);
    }


    //FIND BY GOAT REGISTRATION
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

    //FIND GOAT EVENT WITH FILTERS
    @Transactional(readOnly = true)
    public List<EventResponseVO> findEventsByGoatWithFilters(String registrationNumber,
                                                             EventType eventType,
                                                             LocalDate startDate,
                                                             LocalDate endDate) {
        if (registrationNumber.isEmpty()) {
            throw new ResourceNotFoundException("O número de registro da cabra não pode ser nulo ou vazio");
        }
        List<Event> events = eventRepository.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate);
        if (events.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum evento encontrado para a cabra com os filtros fornecidos.");
        }
        return events.stream().map(EventEntityConverter::toResponseVO).toList();
    }

    //DELETE
    @Transactional
    public void deleteEventById(Long id) {


        if (Objects.isNull(id)) {
            throw new ResourceNotFoundException("O ID do evento não pode ser nulo");
        }
        if(!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Nenhum evento encontrado com número de registro: " + id);
        }
        eventRepository.deleteById(id);
    }

}
