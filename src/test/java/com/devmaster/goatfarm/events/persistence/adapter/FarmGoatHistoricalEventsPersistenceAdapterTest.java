package com.devmaster.goatfarm.events.persistence.adapter;

import com.devmaster.goatfarm.events.enums.EventType;
import com.devmaster.goatfarm.events.persistence.entity.Event;
import com.devmaster.goatfarm.events.persistence.repository.EventRepository;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalOperationalEventItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmGoatHistoricalEventsPersistenceAdapterTest {
    private static final GoatId GOAT = GoatId.of(42L);
    @Mock EventRepository repository;
    private FarmGoatHistoricalEventsPersistenceAdapter adapter;

    @BeforeEach void setUp() { adapter = new FarmGoatHistoricalEventsPersistenceAdapter(repository); }

    @Test
    void usesExactTechnicalIdentityAndPersistedRecordingFarmProvenance() {
        Event event = new Event();
        event.setId(90L); event.setGoatTechnicalId(GOAT.value()); event.setRecordingFarmId(7L);
        event.setEventType(EventType.OUTRO); event.setDate(LocalDate.of(2026, 9, 18));
        event.setDescription("Check"); event.setLocation("Farm A"); event.setVeterinarian("Vet"); event.setOutcome("Ok");
        when(repository.findHistoricalEventsByGoatTechnicalIdAndRecordingFarmId(GOAT.value(), 7L)).thenReturn(List.of(event));

        List<HistoricalOperationalEventItem> result = adapter.findHistoricalEvents(GOAT, 7L);

        assertThat(result).singleElement().satisfies(item -> {
            assertThat(item.id()).isEqualTo(90L); assertThat(item.goatId()).isEqualTo(GOAT);
            assertThat(item.recordingFarmId()).isEqualTo(7L); assertThat(item.eventType()).isEqualTo(EventType.OUTRO);
        });
        verify(repository).findHistoricalEventsByGoatTechnicalIdAndRecordingFarmId(GOAT.value(), 7L);
    }

    @Test
    void rejectsAmbiguousQueryInputsWithoutRepositoryAccess() {
        assertThat(adapter.findHistoricalEvents(null, 7L)).isEmpty();
        assertThat(adapter.findHistoricalEvents(GOAT, 0L)).isEmpty();
        verifyNoInteractions(repository);
    }
}
