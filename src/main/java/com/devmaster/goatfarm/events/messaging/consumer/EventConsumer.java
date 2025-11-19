package com.devmaster.goatfarm.events.messaging.consumer;

import com.devmaster.goatfarm.events.messaging.dto.EventMessage;
import com.devmaster.goatfarm.events.enuns.EventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer de mensagens de eventos do RabbitMQ
 * Processa eventos publicados de forma assíncrona
 */
@Slf4j
@Component
public class EventConsumer {

    @RabbitListener(queues = "${caprigestor.rabbitmq.queue}")
    public void handleEvent(EventMessage message) {
        log.info("========================================");
        log.info("EVENT RECEIVED FROM QUEUE");
        log.info("========================================");
        log.info("Event ID: {}", message.getEventId());
        log.info("Event Type: {}", message.getEventType());
        log.info("Goat: {} ({})", message.getGoatName(), message.getGoatRegistrationNumber());
        log.info("Date: {}", message.getDate());
        log.info("Location: {}", message.getLocation());
        log.info("Veterinarian: {}", message.getVeterinarian());
        log.info("Farm ID: {}", message.getFarmId());
        log.info("Published At: {}", message.getPublishedAt());
        log.info("========================================");
        
        // Processar baseado no tipo de evento
        processEventByType(message);
        
        log.info("Event processed successfully: eventId={}", message.getEventId());
    }

    private void processEventByType(EventMessage message) {
        switch (message.getEventType()) {
            case COBERTURA:
                handleCoverageEvent(message);
                break;
            case PARTO:
                handleBirthEvent(message);
                break;
            case VACINACAO:
                handleVaccinationEvent(message);
                break;
            case PESAGEM:
                handleWeighingEvent(message);
                break;
            case SAUDE:
                handleHealthEvent(message);
                break;
            case MORTE:
                handleDeathEvent(message);
                break;
            case TRANSFERENCIA:
                handleTransferEvent(message);
                break;
            case MUDANCA_PROPRIETARIO:
                handleOwnerChangeEvent(message);
                break;
            case OUTRO:
                handleOtherEvent(message);
                break;
            default:
                log.warn("Unknown event type: {}", message.getEventType());
        }
    }

    private void handleCoverageEvent(EventMessage message) {
        log.info("Processing COVERAGE event for goat: {}", message.getGoatRegistrationNumber());
        // TODO: Implementar lógica de cobertura
        // - Calcular data prevista de parto (150 dias)
        // - Agendar lembrete de confirmação de prenhez
        // - Atualizar status da fêmea
    }

    private void handleBirthEvent(EventMessage message) {
        log.info("Processing BIRTH event for goat: {}", message.getGoatRegistrationNumber());
        // TODO: Implementar lógica de parto
        // - Enviar notificação para fazendeiro
        // - Atualizar estatísticas da fazenda
        // - Verificar genealogia
        // - Registrar crias
    }

    private void handleVaccinationEvent(EventMessage message) {
        log.info("Processing VACCINATION event for goat: {}", message.getGoatRegistrationNumber());
        // TODO: Implementar lógica de vacinação
        // - Agendar próxima dose
        // - Enviar lembrete
        // - Atualizar cartão de vacinação
    }

    private void handleWeighingEvent(EventMessage message) {
        log.info("Processing WEIGHING event for goat: {}", message.getGoatRegistrationNumber());
        // TODO: Implementar lógica de pesagem
        // - Calcular ganho de peso
        // - Gerar gráfico de evolução
        // - Alertar se peso abaixo do esperado
    }

    private void handleHealthEvent(EventMessage message) {
        log.info("Processing HEALTH event for goat: {}", message.getGoatRegistrationNumber());
        // TODO: Implementar lógica de saúde
        // - Registrar tratamento
        // - Agendar retorno
        // - Alertar veterinário se necessário
    }

    private void handleDeathEvent(EventMessage message) {
        log.info("Processing DEATH event for goat: {}", message.getGoatRegistrationNumber());
        // TODO: Implementar lógica de morte
        // - Atualizar status do animal
        // - Gerar relatório
        // - Notificar proprietário
    }

    private void handleTransferEvent(EventMessage message) {
        log.info("Processing TRANSFER event for goat: {}", message.getGoatRegistrationNumber());
        // TODO: Implementar lógica de transferência
        // - Atualizar localização
        // - Registrar histórico
    }

    private void handleOwnerChangeEvent(EventMessage message) {
        log.info("Processing OWNER_CHANGE event for goat: {}", message.getGoatRegistrationNumber());
        // TODO: Implementar lógica de mudança de proprietário
        // - Atualizar proprietário
        // - Registrar histórico de propriedade
    }

    private void handleOtherEvent(EventMessage message) {
        log.info("Processing OTHER event for goat: {}", message.getGoatRegistrationNumber());
        // TODO: Implementar lógica genérica
    }
}