package com.devmaster.goatfarm.events.converter;

import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.model.entity.Event;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.springframework.stereotype.Component;

@Component
public class EventEntityConverter {

    public static Event toEntity(EventRequestVO requestVO, Goat goat) {
        return new Event(
                null,
                goat,
                requestVO.eventType(),
                requestVO.date(),
                requestVO.description(),
                requestVO.location(),
                requestVO.veterinarian(),
                requestVO.outcome()
        );
    }

    public static EventResponseVO toResponseVO(Event event) {
        return new EventResponseVO(
                event.getId(),
                event.getGoat().getRegistrationNumber(),
                event.getGoat().getName(),
                event.getEventType(),
                event.getDate(),
                event.getDescription(),
                event.getLocation(),
                event.getVeterinarian(),
                event.getOutcome()
        );
    }
}
