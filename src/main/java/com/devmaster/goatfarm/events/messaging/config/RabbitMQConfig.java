package com.devmaster.goatfarm.events.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuração do RabbitMQ para o módulo de eventos
 * Define exchanges, queues, bindings e conversores de mensagem
 */
@Configuration
@EnableRabbit
public class RabbitMQConfig {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQConfig.class);

    @Value("${caprigestor.rabbitmq.exchange:events-exchange}")
    private String exchange;

    @Value("${caprigestor.rabbitmq.queue:events-queue}")
    private String queue;

    @Value("${caprigestor.rabbitmq.routing-key:event.created}")
    private String routingKey;

    /**
     * Cria a fila de eventos (durable = persiste no disco)
     */
    @Bean
    public Queue eventsQueue() {
        return new Queue(queue, true);
    }

    /**
     * Cria o exchange do tipo Topic
     */
    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(exchange);
    }

    /**
     * Cria o binding entre queue e exchange
     */
    @Bean
    public Binding binding(Queue eventsQueue, TopicExchange eventsExchange) {
        return BindingBuilder
                .bind(eventsQueue)
                .to(eventsExchange)
                .with(routingKey);
    }

    /**
     * Conversor de mensagens para JSON
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Template do RabbitMQ com conversor JSON
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        // Habilitar publisher confirms/returns quando possível
        if (connectionFactory instanceof CachingConnectionFactory ccf) {
            ccf.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
            ccf.setPublisherReturns(true);
        }

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        // Garantir que mensagens não roteadas sejam retornadas e logadas
        template.setMandatory(true);
        template.setReturnsCallback(returned -> {
            try {
                String body = new String(returned.getMessage().getBody());
                log.warn("RabbitMQ returned message (unroutable): replyCode={}, replyText={}, exchange={}, routingKey={}, body={}",
                        returned.getReplyCode(), returned.getReplyText(), returned.getExchange(), returned.getRoutingKey(), body);
            } catch (Exception e) {
                log.warn("RabbitMQ returned message (unroutable) but failed to read body: exchange={}, routingKey={}, error={}",
                        returned.getExchange(), returned.getRoutingKey(), e.getMessage());
            }
        });
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("RabbitMQ publish confirmed: correlationData={}", correlationData);
            } else {
                log.warn("RabbitMQ publish NOT confirmed: correlationData={}, cause={}", correlationData, cause);
            }
        });
        return template;
    }
}