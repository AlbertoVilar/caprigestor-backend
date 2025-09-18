package com.devmaster.goatfarm.events.facade.dto;

import com.devmaster.goatfarm.events.enuns.EventType;
import java.time.LocalDate;

/**
 * DTO de resposta do EventFacade para encapsular dados do evento
 * sem expor detalhes internos dos VOs.
 */
public class EventFacadeResponseDTO {

    private Long id;
    private EventType eventType;
    private LocalDate eventDate;
    private String description;
    private String goatRegistrationNumber;
    private String goatName;

    public EventFacadeResponseDTO() {
    }

    public EventFacadeResponseDTO(Long id, EventType eventType, LocalDate eventDate, 
                                 String description, String goatRegistrationNumber, String goatName) {
        this.id = id;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.description = description;
        this.goatRegistrationNumber = goatRegistrationNumber;
        this.goatName = goatName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGoatRegistrationNumber() {
        return goatRegistrationNumber;
    }

    public void setGoatRegistrationNumber(String goatRegistrationNumber) {
        this.goatRegistrationNumber = goatRegistrationNumber;
    }

    public String getGoatName() {
        return goatName;
    }

    public void setGoatName(String goatName) {
        this.goatName = goatName;
    }
}