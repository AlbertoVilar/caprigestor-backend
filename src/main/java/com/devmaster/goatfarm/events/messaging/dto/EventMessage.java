package com.devmaster.goatfarm.events.messaging.dto;

import com.devmaster.goatfarm.events.enuns.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO para mensagens de eventos publicadas no RabbitMQ
 * Representa um evento serializado para a fila de mensagens
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long eventId;
    private String goatRegistrationNumber;
    private String goatName;
    private EventType eventType;
    private LocalDate date;
    private String description;
    private String location;
    private String veterinarian;
    private String outcome;
    private Long farmId;

    // Metadados de auditoria
    private String publishedAt;
    private String publishedBy;
}