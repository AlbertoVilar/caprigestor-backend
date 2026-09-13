package com.devmaster.goatfarm.reproduction.persistence.adapter;

import com.devmaster.goatfarm.goat.application.ports.out.GoatReference;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEventEntity;
import com.devmaster.goatfarm.reproduction.persistence.repository.ReproductiveEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReproductiveEventPersistenceAdapterTest {

    @Mock
    private ReproductiveEventRepository repository;

    @Mock
    private GoatReferenceQueryPort goatReferenceQueryPort;

    @Test
    void latestEffectiveCoverage_shouldFallbackToLegacyWhenTechnicalResultIsEmpty() {
        Long farmId = 7L;
        String registration = "RG-007";
        LocalDate referenceDate = LocalDate.of(2026, 8, 1);
        ReproductiveEventEntity legacy = ReproductiveEventEntity.builder().id(41L).farmId(farmId)
                .goatId(registration).eventType(ReproductiveEventType.COVERAGE)
                .eventDate(referenceDate.minusDays(30)).build();
        when(goatReferenceQueryPort.findReferenceByRegistrationNumberAndFarmId(registration, farmId))
                .thenReturn(Optional.of(new GoatReference(new GoatId(99L), farmId, registration, "Doe")));
        when(repository.findLatestEffectiveCoverageOnOrBeforeByTechnicalId(farmId, 99L, referenceDate))
                .thenReturn(Optional.empty());
        when(repository.findLatestEffectiveCoverageOnOrBefore(farmId, registration, referenceDate))
                .thenReturn(Optional.of(legacy));

        var result = new ReproductiveEventPersistenceAdapter(repository, goatReferenceQueryPort)
                .findLatestEffectiveCoverageByFarmIdAndGoatIdOnOrBefore(farmId, registration, referenceDate);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getId()).isEqualTo(41L);
        verify(repository).findLatestEffectiveCoverageOnOrBefore(farmId, registration, referenceDate);
    }
}
