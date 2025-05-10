package com.devmaster.goatfarm.events.business.eventbusiness;

import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.dao.EventDao;
import com.devmaster.goatfarm.events.enuns.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventBusiness {

    @Autowired
    private EventDao eventDao;

    public List<EventResponseVO> findEventByGoat(String goatNumRegistration) {
        return eventDao.findEventByGoat(goatNumRegistration);
    }

    public List<EventResponseVO> findEventsByGoatWithFilters(String registrationNumber,
                                                             EventType eventType,
                                                             LocalDate startDate,
                                                             LocalDate endDate) {

        return eventDao.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate);
    }

    public EventResponseVO createEvent(EventRequestVO requestVO, String goatRegistrationNumber) {
        return eventDao.createEvent(requestVO,goatRegistrationNumber);
    }
}
