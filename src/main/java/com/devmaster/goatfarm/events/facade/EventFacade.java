package com.devmaster.goatfarm.events.facade;

import com.devmaster.goatfarm.events.api.dto.EventRequestDTO;
import com.devmaster.goatfarm.events.api.dto.EventResponseDTO;
import com.devmaster.goatfarm.events.business.eventbusiness.EventBusiness;
import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.mapper.EventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventFacade {

    @Autowired
    private EventBusiness eventBusiness;

    @Autowired
    private EventMapper eventMapper;

    public EventResponseDTO createEvent(Long farmId, String goatId, EventRequestDTO requestDTO) {
        return eventMapper.toResponseDTO(eventBusiness.createEvent(farmId, goatId, eventMapper.toRequestVO(requestDTO)));
    }

    public EventResponseDTO updateEvent(Long farmId, String goatId, Long eventId, EventRequestDTO requestDTO) {
        return eventMapper.toResponseDTO(eventBusiness.updateEvent(farmId, goatId, eventId, eventMapper.toRequestVO(requestDTO)));
    }

    public EventResponseDTO findEventById(Long farmId, String goatId, Long eventId) {
        return eventMapper.toResponseDTO(eventBusiness.findEventById(farmId, goatId, eventId));
    }

    public Page<EventResponseDTO> findAllEventsByGoatAndFarm(Long farmId, String goatId, Pageable pageable) {
        return eventBusiness.findAllEventsByGoatAndFarm(farmId, goatId, pageable).map(eventMapper::toResponseDTO);
    }

    public Page<EventResponseDTO> findEventsByGoatWithFilters(Long farmId, String goatId, EventType eventType, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return eventBusiness.findEventsByGoatWithFilters(farmId, goatId, eventType, startDate, endDate, pageable).map(eventMapper::toResponseDTO);
    }

    public void deleteEvent(Long farmId, String goatId, Long eventId) {
        eventBusiness.deleteEvent(farmId, goatId, eventId);
    }
}
