package com.devmaster.goatfarm.events.business.eventbusiness;

import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.dao.EventDao;
import com.devmaster.goatfarm.events.enuns.EventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EventBusiness {

    @Autowired
    private EventDao eventDao;

    public EventResponseVO createEvent(EventRequestVO requestVO, String goatRegistrationNumber) {
        return eventDao.createEvent(requestVO,goatRegistrationNumber);
    }

    public EventResponseVO updateEvent(Long id, EventRequestVO requestVO, String goatRegistrationNumber) {
        return eventDao.updateGoatEvent(id, requestVO,goatRegistrationNumber);
    }

    public List<EventResponseVO> findEventByGoat(String goatNumRegistration) {
        return eventDao.findEventByGoat(goatNumRegistration);
    }

    public Page<EventResponseVO> findEventsByGoatWithFilters(String registrationNumber,
                                                             EventType eventType,
                                                             LocalDate startDate,
                                                             LocalDate endDate,
                                                             Pageable pageable) {

        return eventDao.findEventsByGoatWithFilters(registrationNumber, eventType, startDate, endDate, pageable);
    }

    public void deleteEventById(Long id) {
        eventDao.deleteEventById(id);
    }
}
