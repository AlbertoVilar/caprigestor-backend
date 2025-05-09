package com.devmaster.goatfarm.events.facade;

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

    public List<EventResponseVO> findEventByGoat(String goatNumRegistration) {
        return eventBusiness.findEventByGoat(goatNumRegistration);
    }

    public List<EventResponseVO> findEventsByGoatWithFilters(String registrationNumber,
                                                             EventType eventType,
                                                             LocalDate startDate,
                                                             LocalDate endDate) {

        return eventBusiness.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate);
    }
}

