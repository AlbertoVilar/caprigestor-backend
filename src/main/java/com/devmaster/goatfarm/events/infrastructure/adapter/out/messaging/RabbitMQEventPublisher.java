package com.devmaster.goatfarm.events.infrastructure.adapter.out.messaging;

import com.devmaster.goatfarm.events.application.ports.out.EventPublisher;
import com.devmaster.goatfarm.events.business.bo.EventPublication;
import com.devmaster.goatfarm.events.infrastructure.adapter.messaging.dto.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Adapter que publica eventos no RabbitMQ implementando o port de saída.
 */
@Component
@ConditionalOnProperty(value = "caprigestor.messaging.enabled", havingValue = "true", matchIfMissing = false)
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
    public void publishEvent(EventPublication event) {
        if (event == null) {
            log.warn("Ignorando publicação: evento ou cabra nulos");
            return;
        }

        EventMessage message = EventMessage.builder()
                .eventId(event.eventId())
                .goatRegistrationNumber(event.goatRegistrationNumber())
                .goatName(event.goatName())
                .eventType(event.eventType())
                .date(event.date())
                .description(event.description())
                .location(event.location())
                .veterinarian(event.veterinarian())
                .outcome(event.outcome())
                .farmId(event.farmId())
                .publishedAt(event.publishedAt())
                .publishedBy(event.publishedBy())
                .build();

        try {
            rabbitTemplate.convertAndSend(eventsExchange.getName(), routingKey, message);
            log.info("Evento publicado no RabbitMQ: id={}, goat={}, type={}, exchange={}, routingKey={}",
                    event.eventId(), event.goatRegistrationNumber(), event.eventType(), eventsExchange.getName(), routingKey);
        } catch (Exception ex) {
            // Não falhar a transação de API se o RabbitMQ estiver indisponível
            log.warn("Falha ao publicar evento no RabbitMQ (continuando sem mensageria): id={}, goat={}, type={}, exchange={}, routingKey={}, erro={}",
                    event.eventId(), event.goatRegistrationNumber(), event.eventType(), eventsExchange.getName(), routingKey, ex.getMessage());
        }
    }
}
