package com.devmaster.goatfarm.reproduction.business.reproductionservice;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.farm.application.ports.in.FarmRegistrationQueryUseCase;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.application.routing.GoatReferenceResolver;
import com.devmaster.goatfarm.reproduction.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.business.mapper.ReproductionBusinessMapper;
import com.devmaster.goatfarm.reproduction.application.model.PregnancyDiagnosisAlertSnapshot;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReproductionQueryPendingAlertsTest {

    @Mock
    private PregnancyPersistencePort pregnancyPersistencePort;

    @Mock
    private ReproductiveEventPersistencePort reproductiveEventPersistencePort;

    @Mock
    private GoatGenderValidator goatGenderValidator;

    @Mock
    private GoatPersistencePort goatPersistencePort;

    @Mock
    private GoatReferenceResolver goatReferenceResolver;

    @Mock
    private FarmRegistrationQueryUseCase farmRegistrationQueryUseCase;

    @Mock
    private GoatManagementUseCase goatManagementUseCase;

    @Mock
    private ReproductionBusinessMapper reproductionBusinessMapper;

    private LegacyReproductionTestFacade reproductionBusiness;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-02-08T10:00:00Z"), ZoneOffset.UTC);
        reproductionBusiness = new LegacyReproductionTestFacade(
                pregnancyPersistencePort,
                reproductiveEventPersistencePort,
                goatPersistencePort,
                goatReferenceResolver,
                farmRegistrationQueryUseCase,
                goatManagementUseCase,
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

        PregnancyDiagnosisAlertSnapshot projection = new PregnancyDiagnosisAlertSnapshot(null, "GOAT-001", coverageDate, null, null);

        PageQuery pageQuery = new PageQuery(0, 20, List.of());
        when(reproductiveEventPersistencePort.findPendingPregnancyDiagnosisAlerts(
                farmId,
                referenceDate,
                60,
                pageQuery
        )).thenReturn(new PageResult<>(List.of(projection), 1, 0, 20));

        var result = reproductionBusiness.getPendingPregnancyDiagnosisAlerts(
                farmId,
                referenceDate,
                pageQuery
        );

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).getEligibleDate()).isEqualTo(coverageDate.plusDays(60));
        assertThat(result.content().get(0).getDaysOverdue()).isEqualTo(20);
    }
}
