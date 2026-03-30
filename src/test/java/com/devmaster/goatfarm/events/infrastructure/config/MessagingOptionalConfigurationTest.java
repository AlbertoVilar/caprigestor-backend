package com.devmaster.goatfarm.events.infrastructure.config;

import com.devmaster.goatfarm.events.application.ports.out.EventPublisher;
import com.devmaster.goatfarm.events.infrastructure.adapter.in.messaging.EventConsumer;
import com.devmaster.goatfarm.events.infrastructure.adapter.out.messaging.NoOpEventPublisher;
import com.devmaster.goatfarm.events.infrastructure.adapter.out.messaging.RabbitMQEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class MessagingOptionalConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("caprigestor.messaging.enabled=false")
            .withUserConfiguration(
                    RabbitMQConfig.class,
                    RabbitMQEventPublisher.class,
                    NoOpEventPublisher.class,
                    EventConsumer.class
            );

    @Test
    void shouldLoadNoOpPublisherWhenMessagingIsDisabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(EventPublisher.class);
            assertThat(context).hasSingleBean(NoOpEventPublisher.class);
            assertThat(context).doesNotHaveBean(RabbitMQEventPublisher.class);
            assertThat(context).doesNotHaveBean(EventConsumer.class);
        });
    }
}
