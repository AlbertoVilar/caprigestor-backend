package com.devmaster.goatfarm.events.business.eventbusiness;

import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.dao.EventDao;
import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.mapper.EventMapper;
import com.devmaster.goatfarm.events.model.entity.Event;
import com.devmaster.goatfarm.goat.dao.GoatDAO;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventBusiness {

    private final EventDao eventDao;
    private final GoatDAO goatDAO;
    private final OwnershipService ownershipService;
    private final EventMapper eventMapper;

    public EventBusiness(EventDao eventDao, GoatDAO goatDAO, OwnershipService ownershipService, EventMapper eventMapper) {
        this.eventDao = eventDao;
        this.goatDAO = goatDAO;
        this.ownershipService = ownershipService;
        this.eventMapper = eventMapper;
    }

    @Transactional
    public void deleteEventsFromOtherUsers(Long adminId) {
        eventDao.deleteEventsFromOtherUsers(adminId);
    }

    @Transactional
    public EventResponseVO createEvent(Long farmId, String goatId, EventRequestVO requestVO) {
        ownershipService.verifyGoatOwnership(farmId, goatId);

        Goat goat = goatDAO.findByIdAndFarmId(goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada na fazenda especificada."));

        Event event = eventMapper.toEntity(requestVO);
        event.setGoat(goat);
        event = eventDao.saveEvent(event);
        return eventMapper.toResponseVO(event);
    }

    @Transactional
    public EventResponseVO updateEvent(Long farmId, String goatId, Long eventId, EventRequestVO requestVO) {
        ownershipService.verifyGoatOwnership(farmId, goatId);

        Event event = eventDao.findByIdAndGoatRegistrationNumberAndGoatFarmId(eventId, goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado para a cabra e fazenda especificadas."));

        eventMapper.updateEvent(event, requestVO);
        Event updatedEvent = eventDao.saveEvent(event);
        return eventMapper.toResponseVO(updatedEvent);
    }

    @Transactional(readOnly = true)
    public EventResponseVO findEventById(Long farmId, String goatId, Long eventId) {
        ownershipService.verifyGoatOwnership(farmId, goatId);
        Event event = eventDao.findByIdAndGoatRegistrationNumberAndGoatFarmId(eventId, goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado para a cabra e fazenda especificadas."));
        return eventMapper.toResponseVO(event);
    }

    @Transactional(readOnly = true)
    public Page<EventResponseVO> findAllEventsByGoatAndFarm(Long farmId, String goatId, Pageable pageable) {
        ownershipService.verifyGoatOwnership(farmId, goatId);
        return eventDao.findAllByGoatRegistrationNumberAndGoatFarmId(goatId, farmId, pageable).map(eventMapper::toResponseVO);
    }

    @Transactional(readOnly = true)
    public Page<EventResponseVO> findEventsByGoatWithFilters(Long farmId, String goatId, EventType eventType, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        ownershipService.verifyGoatOwnership(farmId, goatId);
        return eventDao.findEventsByGoatWithFilters(goatId, eventType, startDate, endDate, pageable).map(eventMapper::toResponseVO);
    }

    @Transactional
    public void deleteEvent(Long farmId, String goatId, Long eventId) {
        ownershipService.verifyGoatOwnership(farmId, goatId);
        Event event = eventDao.findByIdAndGoatRegistrationNumberAndGoatFarmId(eventId, goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento não encontrado para a cabra e fazenda especificadas."));
        eventDao.deleteEventById(eventId);
    }
}
