package com.devmaster.goatfarm.events.api.dto;

import com.devmaster.goatfarm.events.enuns.EventType;

import java.time.LocalDate;

public record EventRequestDTO(
        String goatId,
        EventType eventType,
        LocalDate date,
        String description,
        String location,
        String veterinarian,
        String outcome
) {}
