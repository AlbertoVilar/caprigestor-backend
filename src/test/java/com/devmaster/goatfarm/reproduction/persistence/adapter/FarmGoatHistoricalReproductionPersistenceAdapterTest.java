package com.devmaster.goatfarm.reproduction.persistence.adapter;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalReproductionEventItem;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalReproductionProcessCandidate;
import com.devmaster.goatfarm.reproduction.enums.*;
import com.devmaster.goatfarm.reproduction.persistence.entity.PregnancyEntity;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEventEntity;
import com.devmaster.goatfarm.reproduction.persistence.repository.PregnancyRepository;
import com.devmaster.goatfarm.reproduction.persistence.repository.ReproductiveEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmGoatHistoricalReproductionPersistenceAdapterTest {

    @Mock
    private PregnancyRepository pregnancyRepository;

    @Mock
    private ReproductiveEventRepository reproductiveEventRepository;

    private FarmGoatHistoricalReproductionPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FarmGoatHistoricalReproductionPersistenceAdapter(
                pregnancyRepository,
                reproductiveEventRepository
        );
    }

    @Test
    @DisplayName("findCandidateProcessesByGoat maps entities correctly to technology-neutral models")
    void findCandidateProcessesByGoat_mapsCorrectly() {
        GoatId goatId = new GoatId(42L);
        String regNumber = "BR-42";

        PregnancyEntity entity = PregnancyEntity.builder()
                .id(101L)
                .farmId(1L)
                .goatTechnicalId(42L)
                .goatId(regNumber)
                .status(PregnancyStatus.ACTIVE)
                .breedingDate(LocalDate.of(2026, 1, 15))
                .confirmDate(LocalDate.of(2026, 3, 16))
                .expectedDueDate(LocalDate.of(2026, 6, 14))
                .closedAt(null)
                .closeReason(null)
                .coverageEventId(55L)
                .notes("Pregnancy note")
                .build();

        when(pregnancyRepository.findHistoricalCandidates(42L, regNumber))
                .thenReturn(List.of(entity));

        List<HistoricalReproductionProcessCandidate> result =
                adapter.findCandidateProcessesByGoat(goatId, regNumber);

        assertThat(result).hasSize(1);
        HistoricalReproductionProcessCandidate item = result.get(0);
        assertThat(item.id()).isEqualTo(101L);
        assertThat(item.goatId()).isEqualTo(goatId);
        assertThat(item.farmId()).isEqualTo(1L);
        assertThat(item.status()).isEqualTo(PregnancyStatus.ACTIVE);
        assertThat(item.breedingDate()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(item.confirmDate()).isEqualTo(LocalDate.of(2026, 3, 16));
        assertThat(item.expectedDueDate()).isEqualTo(LocalDate.of(2026, 6, 14));
        assertThat(item.closedAt()).isNull();
        assertThat(item.closeReason()).isNull();
        assertThat(item.coverageEventId()).isEqualTo(55L);
        assertThat(item.notes()).isEqualTo("Pregnancy note");

        verify(pregnancyRepository).findHistoricalCandidates(42L, regNumber);
    }

    @Test
    @DisplayName("findCandidateEventsByGoat maps entities correctly to technology-neutral models")
    void findCandidateEventsByGoat_mapsCorrectly() {
        GoatId goatId = new GoatId(42L);
        String regNumber = "BR-42";

        ReproductiveEventEntity entity = ReproductiveEventEntity.builder()
                .id(202L)
                .farmId(1L)
                .goatTechnicalId(42L)
                .goatId(regNumber)
                .eventType(ReproductiveEventType.COVERAGE)
                .eventDate(LocalDate.of(2026, 1, 15))
                .breedingType(BreedingType.NATURAL)
                .breederRef("BOD-01")
                .pregnancyId(null)
                .relatedEventId(null)
                .correctedEventDate(null)
                .checkScheduledDate(null)
                .checkResult(null)
                .notes("Coverage note")
                .build();

        when(reproductiveEventRepository.findHistoricalCandidates(42L, regNumber))
                .thenReturn(List.of(entity));

        List<HistoricalReproductionEventItem> result =
                adapter.findCandidateEventsByGoat(goatId, regNumber);

        assertThat(result).hasSize(1);
        HistoricalReproductionEventItem item = result.get(0);
        assertThat(item.id()).isEqualTo(202L);
        assertThat(item.goatId()).isEqualTo(goatId);
        assertThat(item.farmId()).isEqualTo(1L);
        assertThat(item.eventType()).isEqualTo(ReproductiveEventType.COVERAGE);
        assertThat(item.eventDate()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(item.breedingType()).isEqualTo(BreedingType.NATURAL);
        assertThat(item.breederRef()).isEqualTo("BOD-01");
        assertThat(item.pregnancyId()).isNull();
        assertThat(item.relatedEventId()).isNull();
        assertThat(item.correctedEventDate()).isNull();
        assertThat(item.checkScheduledDate()).isNull();
        assertThat(item.checkResult()).isNull();
        assertThat(item.notes()).isEqualTo("Coverage note");

        verify(reproductiveEventRepository).findHistoricalCandidates(42L, regNumber);
    }

    @Test
    @DisplayName("Null goatId throws IllegalArgumentException")
    void nullGoatId_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> adapter.findCandidateProcessesByGoat(null, "BR-42"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> adapter.findCandidateEventsByGoat(null, "BR-42"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
