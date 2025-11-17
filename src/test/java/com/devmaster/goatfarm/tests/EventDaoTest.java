package com.devmaster.goatfarm.tests;

import com.devmaster.goatfarm.events.model.entity.Event;
import com.devmaster.goatfarm.events.model.repository.EventRepository;
import com.devmaster.goatfarm.infrastructure.adapters.out.persistence.EventPersistenceAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(EventPersistenceAdapter.class)
public class EventDaoTest {

    @Autowired
    private EventPersistenceAdapter eventPersistenceAdapter;

    @Autowired
    private EventRepository eventRepository;

    @Test
    public void whenSaveEvent_thenEventIsSaved() {
        Event event = new Event();
        event.setDescription("Test Event");

        Event savedEvent = eventPersistenceAdapter.save(event);

        assertThat(savedEvent).isNotNull();
        assertThat(savedEvent.getId()).isNotNull();
        assertThat(savedEvent.getDescription()).isEqualTo("Test Event");

        // Sanidade: garantir que está persistido via repository também
        assertThat(eventRepository.findById(savedEvent.getId())).isPresent();
    }
}
