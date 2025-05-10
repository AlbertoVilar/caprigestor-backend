package com.devmaster.goatfarm.events.facade;

import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.business.eventbusiness.EventBusiness;
import com.devmaster.goatfarm.events.enuns.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventFacade {

    @Autowired
    private EventBusiness eventBusiness;

    public EventResponseVO createEvent(EventRequestVO requestVO, String goatRegistrationNumber) {
        return eventBusiness.createEvent(requestVO,goatRegistrationNumber);
    }

    public EventResponseVO updateEvent(Long id, EventRequestVO requestVO, String goatRegistrationNumber) {
        return eventBusiness.updateEvent(id, requestVO,goatRegistrationNumber);
    }

    public List<EventResponseVO> findEventByGoat(String goatNumRegistration) {
        return eventBusiness.findEventByGoat(goatNumRegistration);
    }

    public List<EventResponseVO> findEventsByGoatWithFilters(String registrationNumber,
                                                             EventType eventType,
                                                             LocalDate startDate,
                                                             LocalDate endDate) {

        return eventBusiness.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate);
    }

    public void deleteEventById(Long id) {
        eventBusiness.deleteEventById(id);
    }
}


