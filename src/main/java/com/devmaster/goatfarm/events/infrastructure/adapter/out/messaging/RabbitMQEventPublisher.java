package com.devmaster.goatfarm.events.infrastructure.adapter.out.messaging;

import com.devmaster.goatfarm.events.application.ports.out.EventPublisher;
import com.devmaster.goatfarm.events.infrastructure.adapter.messaging.dto.EventMessage;
import com.devmaster.goatfarm.events.persistence.entity.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Adapter que publica eventos no RabbitMQ implementando o port de saída.
 */
@Component
public class RabbitMQEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;
    private final TopicExchange eventsExchange;

    @Value("${caprigestor.rabbitmq.routing-key:event.created}")
    private String routingKey;

    public RabbitMQEventPublisher(RabbitTemplate rabbitTemplate, TopicExchange eventsExchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.eventsExchange = eventsExchange;
    }

    @Override
    public void publishEvent(Event event) {
        if (event == null || event.getGoat() == null) {
            log.warn("Ignorando publicação: evento ou cabra nulos");
            return;
        }

        var goat = event.getGoat();
        var farm = goat.getFarm();

        EventMessage message = EventMessage.builder()
                .eventId(event.getId())
                .goatRegistrationNumber(goat.getRegistrationNumber())
                .goatName(goat.getName())
                .eventType(event.getEventType())
                .date(event.getDate())
                .description(event.getDescription())
                .location(event.getLocation())
                .veterinarian(event.getVeterinarian())
                .outcome(event.getOutcome())
                .farmId(farm != null ? farm.getId() : null)
                .publishedAt(OffsetDateTime.now().toString())
                .publishedBy("system")
                .build();

        try {
            rabbitTemplate.convertAndSend(eventsExchange.getName(), routingKey, message);
            log.info("Evento publicado no RabbitMQ: id={}, goat={}, type={}, exchange={}, routingKey={}",
                    event.getId(), goat.getRegistrationNumber(), event.getEventType(), eventsExchange.getName(), routingKey);
        } catch (Exception ex) {
            // Não falhar a transação de API se o RabbitMQ estiver indisponível
            log.warn("Falha ao publicar evento no RabbitMQ (continuando sem mensageria): id={}, goat={}, type={}, exchange={}, routingKey={}, erro={}",
                    event.getId(), goat.getRegistrationNumber(), event.getEventType(), eventsExchange.getName(), routingKey, ex.getMessage());
        }
    }
}