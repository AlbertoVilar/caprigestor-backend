package com.devmaster.goatfarm.events.persistence;

import com.devmaster.goatfarm.events.application.ports.out.EventPage;
import com.devmaster.goatfarm.events.application.ports.out.EventPageQuery;
import com.devmaster.goatfarm.events.domain.OperationalEvent;
import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.events.persistence.adapter.EventPersistenceAdapter;
import com.devmaster.goatfarm.events.persistence.entity.Event;
import com.devmaster.goatfarm.events.persistence.repository.EventRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventDaoTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private GoatRepository goatRepository;

    private EventPersistenceAdapter adapter;
    private GoatEntity goat;

    @BeforeEach
    void setUp() {
        adapter = new EventPersistenceAdapter(eventRepository, goatRepository);
        GoatFarm farm = new GoatFarm();
        farm.setId(7L);
        goat = new GoatEntity();
        goat.setTechnicalId(42L);
        goat.setRegistrationNumber("R-123");
        goat.setName("Matriz");
        goat.setFarm(farm);
    }

    @Test
    void saveWritesTechnicalRelationshipAndBusinessRegistrationSnapshot() {
        OperationalEvent event = OperationalEvent.create(
                new GoatReference(new GoatId(42L), 7L, "R-123", "Matriz"),
                EventType.VACINACAO, LocalDate.of(2026, 1, 1), "Test Event",
                "Farm", "Veterinarian", "Completed");
        when(goatRepository.findByTechnicalId(42L)).thenReturn(Optional.of(goat));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        OperationalEvent saved = adapter.save(event);

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        assertThat(captor.getValue().getGoat()).isSameAs(goat);
        assertThat(captor.getValue().getGoatRegistrationNumber()).isEqualTo("R-123");
        assertThat(saved.id()).isEqualTo(1L);
        assertThat(saved.goatId()).isEqualTo(new GoatId(42L));
        assertThat(saved.goatRegistrationNumber()).isEqualTo("R-123");
    }

    @Test
    void findByTechnicalGoatAndFarmReturnsOnlyScopedEvents() {
        Event entity = entity(10L, "R-123");
        when(eventRepository.findByIdAndGoatTechnicalIdAndFarmId(10L, 42L, 7L))
                .thenReturn(Optional.of(entity));

        Optional<OperationalEvent> result = adapter.findByIdAndGoatIdAndFarmId(10L, new GoatId(42L), 7L);

        assertThat(result).get().satisfies(event -> {
            assertThat(event.id()).isEqualTo(10L);
            assertThat(event.goatId()).isEqualTo(new GoatId(42L));
            assertThat(event.goatRegistrationNumber()).isEqualTo("R-123");
        });
        verify(eventRepository).findByIdAndGoatTechnicalIdAndFarmId(10L, 42L, 7L);
    }

    @Test
    void findPageMapsPersistenceEntitiesToApplicationRecords() {
        Event entity = entity(11L, "R-123");
        PageRequest expectedPageable = PageRequest.of(0, 12,
                Sort.by(Sort.Direction.DESC, "date"));
        when(eventRepository.findAllByGoatTechnicalIdAndFarmIdWithFilters(
                42L, 7L, EventType.VACINACAO, null, null, expectedPageable))
                .thenReturn(new PageImpl<>(List.of(entity), expectedPageable, 1));

        EventPage<OperationalEvent> result = adapter.findByGoatIdWithFilters(
                new GoatId(42L), 7L, EventType.VACINACAO, null, null,
                new EventPageQuery(0, 12, ""));

        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.content()).singleElement().satisfies(event -> {
            assertThat(event.eventType()).isEqualTo(EventType.VACINACAO);
            assertThat(event.goatName()).isEqualTo("Matriz");
        });
    }

    private Event entity(Long id, String registrationNumber) {
        Event entity = new Event();
        entity.setId(id);
        entity.setGoat(goat);
        entity.setGoatRegistrationNumber(registrationNumber);
        entity.setEventType(EventType.VACINACAO);
        entity.setDate(LocalDate.of(2026, 1, 1));
        entity.setDescription("Event");
        entity.setLocation("Farm");
        entity.setVeterinarian("Veterinarian");
        entity.setOutcome("Completed");
        return entity;
    }
}
