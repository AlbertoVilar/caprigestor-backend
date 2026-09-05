package com.devmaster.goatfarm.reproduction.business.reproductionservice;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.reproduction.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.business.mapper.ReproductionBusinessMapper;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.persistence.entity.Pregnancy;
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
class ReproductionBusinessBirthAlertsTest {

    @Mock
    private PregnancyPersistencePort pregnancyPersistencePort;

    @Mock
    private ReproductiveEventPersistencePort reproductiveEventPersistencePort;

    @Mock
    private GoatGenderValidator goatGenderValidator;

    @Mock
    private GoatPersistencePort goatPersistencePort;

    @Mock
    private GoatFarmPersistencePort goatFarmPersistencePort;

    @Mock
    private GoatManagementUseCase goatManagementUseCase;

    @Mock
    private ReproductionBusinessMapper reproductionBusinessMapper;

    private ReproductionBusiness reproductionBusiness;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-07-03T10:00:00Z"), ZoneOffset.UTC);
        reproductionBusiness = new ReproductionBusiness(
                pregnancyPersistencePort,
                reproductiveEventPersistencePort,
                goatPersistencePort,
                goatFarmPersistencePort,
                goatManagementUseCase,
                goatGenderValidator,
                reproductionBusinessMapper,
                clock
        );
    }

    @Test
    void getPendingBirthAlerts_shouldReturnActivePregnanciesDueTodayOrEarlier() {
        Long farmId = 14L;
        LocalDate referenceDate = LocalDate.of(2026, 7, 3);
        PageRequest pageable = PageRequest.of(0, 20);
        Pregnancy pregnancy = Pregnancy.builder()
                .id(27L)
                .farmId(farmId)
                .goatId("1615325001")
                .status(PregnancyStatus.ACTIVE)
                .expectedDueDate(LocalDate.of(2026, 7, 1))
                .build();

        when(pregnancyPersistencePort.findActiveWithDueDateOnOrBefore(farmId, referenceDate, pageable))
                .thenReturn(new PageImpl<>(List.of(pregnancy), pageable, 1));

        var result = reproductionBusiness.getPendingBirthAlerts(farmId, referenceDate, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).singleElement().satisfies(alert -> {
            assertThat(alert.getPregnancyId()).isEqualTo(27L);
            assertThat(alert.getGoatId()).isEqualTo("1615325001");
            assertThat(alert.getExpectedDueDate()).isEqualTo(LocalDate.of(2026, 7, 1));
            assertThat(alert.getDaysOverdue()).isEqualTo(2);
        });
    }
}
