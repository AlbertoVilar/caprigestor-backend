package com.devmaster.goatfarm.reproduction.business.reproductionservice;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.reproduction.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.business.mapper.ReproductionBusinessMapper;
import com.devmaster.goatfarm.reproduction.persistence.projection.PregnancyDiagnosisAlertProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReproductionBusinessPendingAlertsTest {

    @Mock
    private PregnancyPersistencePort pregnancyPersistencePort;

    @Mock
    private ReproductiveEventPersistencePort reproductiveEventPersistencePort;

    @Mock
    private GoatGenderValidator goatGenderValidator;

    @Mock
    private ReproductionBusinessMapper reproductionBusinessMapper;

    private ReproductionBusiness reproductionBusiness;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-08T10:00:00Z"), ZoneOffset.UTC);
        reproductionBusiness = new ReproductionBusiness(
                pregnancyPersistencePort,
                reproductiveEventPersistencePort,
                goatGenderValidator,
                reproductionBusinessMapper,
                clock
        );
    }

    @Test
    void getPendingPregnancyDiagnosisAlerts_shouldCalculateDaysOverdueAndEligibleDate() {
        Long farmId = 1L;
        LocalDate referenceDate = LocalDate.of(2026, 2, 8);
        LocalDate coverageDate = LocalDate.of(2025, 11, 20);

        PregnancyDiagnosisAlertProjection projection = new PregnancyDiagnosisAlertProjection() {
            @Override
            public String getGoatId() {
                return "GOAT-001";
            }

            @Override
            public LocalDate getLastCoverageDate() {
                return coverageDate;
            }

            @Override
            public LocalDate getLastCheckDate() {
                return null;
            }

            @Override
            public LocalDate getEligibleDate() {
                return null;
            }
        };

        when(reproductiveEventPersistencePort.findPendingPregnancyDiagnosisAlerts(
                farmId,
                referenceDate,
                60,
                PageRequest.of(0, 20)
        )).thenReturn(new PageImpl<>(List.of(projection), PageRequest.of(0, 20), 1));

        var result = reproductionBusiness.getPendingPregnancyDiagnosisAlerts(
                farmId,
                referenceDate,
                PageRequest.of(0, 20)
        );

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getEligibleDate()).isEqualTo(coverageDate.plusDays(60));
        assertThat(result.getContent().get(0).getDaysOverdue()).isEqualTo(20);
    }
}
