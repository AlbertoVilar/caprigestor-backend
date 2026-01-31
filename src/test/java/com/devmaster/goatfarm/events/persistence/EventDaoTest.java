package com.devmaster.goatfarm.events.persistence;

import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.events.persistence.entity.Event;
import com.devmaster.goatfarm.events.persistence.repository.EventRepository;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import com.devmaster.goatfarm.events.persistence.adapter.EventPersistenceAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class EventDaoTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventPersistenceAdapter eventPersistenceAdapter;

    @Test
    void whenSaveEvent_thenEventIsSaved() {
        Goat goat = new Goat();
        goat.setRegistrationNumber("R-123");

        Event event = new Event();
        event.setGoat(goat);
        event.setEventType(EventType.VACINACAO);
        event.setDate(LocalDate.now());
        event.setDescription("Test Event");

        Event persisted = new Event();
        persisted.setGoat(goat);
        persisted.setEventType(EventType.VACINACAO);
        persisted.setDate(event.getDate());
        persisted.setDescription(event.getDescription());
        persisted.setId(1L);

        when(eventRepository.save(event)).thenReturn(persisted);

        Event savedEvent = eventPersistenceAdapter.save(event);

        assertThat(savedEvent).isNotNull();
        assertThat(savedEvent.getId()).isEqualTo(1L);
        assertThat(savedEvent.getDescription()).isEqualTo("Test Event");
        assertThat(savedEvent.getGoat()).isNotNull();
        assertThat(savedEvent.getEventType()).isEqualTo(EventType.VACINACAO);

        verify(eventRepository).save(event);
    }

    @Test
    void whenFindByGoat_thenAdapterDelegatesToRepository() {
        Goat goat = new Goat();
        goat.setRegistrationNumber("R-123");

        Event e1 = new Event();
        e1.setId(10L);
        e1.setGoat(goat);
        e1.setEventType(EventType.VACINACAO);
        e1.setDate(LocalDate.now());

        when(eventRepository.findEventsByGoatRegistrationNumber("R-123")).thenReturn(List.of(e1));

        List<Event> events = eventPersistenceAdapter.findByGoatRegistrationNumber("R-123");

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getId()).isEqualTo(10L);
        verify(eventRepository).findEventsByGoatRegistrationNumber("R-123");
    }
}
