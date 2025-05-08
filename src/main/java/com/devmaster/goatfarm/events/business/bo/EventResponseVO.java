package com.devmaster.goatfarm.events.business.bo;

import com.devmaster.goatfarm.events.enuns.EventType;

import java.time.LocalDate;

public record EventResponseVO(
        Long eventId,
        String goatId,
        String goatName,
        EventType eventType,
        LocalDate date,
        String description,
        String location,
        String veterinarian,
        String outcome
) {}
