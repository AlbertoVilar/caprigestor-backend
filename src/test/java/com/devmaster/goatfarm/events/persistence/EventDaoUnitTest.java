package com.devmaster.goatfarm.events.persistence;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventDaoUnitTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private GoatRepository goatRepository;

    @Test
    void deleteUsesEventIdentityOnlyAfterScopedLookup() {
        EventPersistenceAdapter adapter = new EventPersistenceAdapter(eventRepository, goatRepository);
        GoatFarm farm = new GoatFarm();
        farm.setId(7L);
        GoatEntity goat = new GoatEntity();
        goat.setTechnicalId(42L);
        goat.setRegistrationNumber("R-123");
        goat.setName("Matriz");
        goat.setFarm(farm);
        Event event = new Event();
        event.setId(10L);
        event.setGoat(goat);
        event.setEventType(EventType.PESAGEM);
        event.setDate(LocalDate.of(2026, 1, 2));
        event.setDescription("Weight");
        when(eventRepository.findByIdAndGoatTechnicalIdAndFarmId(10L, 42L, 7L))
                .thenReturn(Optional.of(event));

        assertThat(adapter.findByIdAndGoatIdAndFarmId(10L, new GoatId(42L), 7L)).isPresent();
        adapter.deleteById(10L);

        verify(eventRepository).deleteById(10L);
    }

    @Test
    void savePreservesTheRegistrationSnapshotWhenGoatNameChanges() {
        EventPersistenceAdapter adapter = new EventPersistenceAdapter(eventRepository, goatRepository);
        GoatFarm farm = new GoatFarm();
        farm.setId(7L);
        GoatEntity goat = new GoatEntity();
        goat.setTechnicalId(42L);
        goat.setRegistrationNumber("R-123-CORRIGIDO");
        goat.setName("Nome atual");
        goat.setFarm(farm);
        OperationalEvent event = new OperationalEvent(1L, new GoatId(42L), 7L,
                "R-123-ANTIGO", "Nome antigo", EventType.OUTRO, LocalDate.of(2026, 1, 3),
                "History", "Farm", "Vet", "Done");
        Event persisted = new Event();
        persisted.setId(1L);
        persisted.setGoat(goat);
        persisted.setGoatRegistrationNumber("R-123-ANTIGO");
        persisted.setEventType(EventType.OUTRO);
        persisted.setDate(event.date());
        persisted.setDescription(event.description());
        persisted.setLocation(event.location());
        persisted.setVeterinarian(event.veterinarian());
        persisted.setOutcome(event.outcome());
        when(goatRepository.findByTechnicalId(42L)).thenReturn(Optional.of(goat));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(persisted));
        when(eventRepository.save(persisted)).thenReturn(persisted);

        OperationalEvent saved = adapter.save(event);

        assertThat(saved.goatId()).isEqualTo(new GoatId(42L));
        // A normal event update never rewrites the historical RG snapshot.
        assertThat(saved.goatRegistrationNumber()).isEqualTo("R-123-ANTIGO");
    }
}
