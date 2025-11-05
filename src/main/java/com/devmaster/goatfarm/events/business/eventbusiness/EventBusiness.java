package com.devmaster.goatfarm.events.business.eventbusiness;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.dao.EventDao;
import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.mapper.EventMapper;
import com.devmaster.goatfarm.events.model.entity.Event;
import com.devmaster.goatfarm.goat.business.goatbusiness.GoatBusiness;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.config.security.OwnershipService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventBusiness {

    private final EventDao eventDao;
    private final GoatBusiness goatBusiness;
    private final OwnershipService ownershipService;
    private final EventMapper eventMapper;

    public EventBusiness(EventDao eventDao, GoatBusiness goatBusiness, OwnershipService ownershipService, EventMapper eventMapper) {
        this.eventDao = eventDao;
        this.goatBusiness = goatBusiness;
        this.ownershipService = ownershipService;
        this.eventMapper = eventMapper;
    }

    @Transactional
    public void deleteEventsFromOtherUsers(Long adminId) {
        eventDao.deleteEventsFromOtherUsers(adminId);
    }

    @Transactional
    public EventResponseVO createEvent(EventRequestVO requestVO, String goatRegistrationNumber) {
        Goat goat = goatBusiness.getEntityByRegistrationNumber(goatRegistrationNumber);

        verifyFarmOwnership(goat);

        Event event = eventMapper.toEntity(requestVO);
        event.setGoat(goat);
        event = eventDao.saveEvent(event);
        return eventMapper.toResponseVO(event);
    }

    @Transactional
    public EventResponseVO updateEvent(Long id, EventRequestVO requestVO, String goatRegistrationNumber) {
        Goat goat = goatBusiness.getEntityByRegistrationNumber(goatRegistrationNumber);

        Event event = eventDao.findEventById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + id));

        if (!event.getGoat().getRegistrationNumber().equals(goatRegistrationNumber)) {
            throw new ResourceNotFoundException("Este evento não pertence à cabra de registro: " + goatRegistrationNumber);
        }

        verifyFarmOwnership(goat);

        eventMapper.updateEvent(event, requestVO);
        Event updatedEvent = eventDao.saveEvent(event);
        return eventMapper.toResponseVO(updatedEvent);
    }

    @Transactional(readOnly = true)
    public List<EventResponseVO> findEventByGoat(String goatNumRegistration) {
        Goat goat = goatBusiness.getEntityByRegistrationNumber(goatNumRegistration);

        verifyFarmOwnership(goat);

        List<Event> events = eventDao.findEventsByGoatNumRegistro(goatNumRegistration);
        if (events.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum evento encontrado para a cabra com número de registro: " + goatNumRegistration);
        }
        return events.stream().map(eventMapper::toResponseVO).toList();
    }

    @Transactional(readOnly = true)
    public Page<EventResponseVO> findEventsByGoatWithFilters(String registrationNumber,
                                                             EventType eventType,
                                                             LocalDate startDate,
                                                             LocalDate endDate,
                                                             Pageable pageable) {
        Goat goat = goatBusiness.getEntityByRegistrationNumber(registrationNumber);

        verifyFarmOwnership(goat);

        Page<Event> page = eventDao.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate, pageable);
        if (page.isEmpty()) {
            throw new ResourceNotFoundException("Nenhum evento encontrado para a cabra com os filtros fornecidos.");
        }
        return page.map(eventMapper::toResponseVO);
    }

    @Transactional
    public void deleteEventById(Long id) {
        Event event = eventDao.findEventById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado: " + id));

        verifyFarmOwnership(event.getGoat());

        eventDao.deleteEventById(id);
    }

    private void verifyFarmOwnership(Goat goat) {
        ownershipService.verifyGoatOwnership(goat);
    }
}
