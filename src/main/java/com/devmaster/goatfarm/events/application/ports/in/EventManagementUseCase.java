package com.devmaster.goatfarm.events.application.ports.in;

import com.devmaster.goatfarm.events.application.ports.out.EventPage;
import com.devmaster.goatfarm.events.application.ports.out.EventPageQuery;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.enums.EventType;

import java.time.LocalDate;
import java.util.List;

/** Inbound application contract for farm-scoped operational events. */
public interface EventManagementUseCase {

    EventResponseVO createEvent(Long farmId, String registrationNumber, EventRequestVO request);

    EventResponseVO updateEvent(Long farmId, String registrationNumber, Long eventId, EventRequestVO request);

    EventResponseVO findEventById(Long farmId, String registrationNumber, Long eventId);

    List<EventResponseVO> findEventsByGoat(Long farmId, String registrationNumber);

    EventPage<EventResponseVO> findEventsWithFilters(
            Long farmId,
            String registrationNumber,
            EventType eventType,
            LocalDate startDate,
            LocalDate endDate,
            EventPageQuery pageQuery
    );

    void deleteEvent(Long farmId, String registrationNumber, Long eventId);

    /** Administrative maintenance operation, invoked only by the authority workflow. */
    void deleteEventsFromOtherUsers(Long adminId);
}
