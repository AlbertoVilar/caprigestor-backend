package com.devmaster.goatfarm.milk.persistence.adapter;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalLactationItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalMilkProductionItem;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.persistence.entity.LactationEntity;
import com.devmaster.goatfarm.milk.persistence.entity.MilkProductionEntity;
import com.devmaster.goatfarm.milk.persistence.repository.LactationRepository;
import com.devmaster.goatfarm.milk.persistence.repository.MilkProductionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmGoatHistoricalMilkLactationPersistenceAdapterTest {

    @Mock
    private LactationRepository lactationRepository;

    @Mock
    private MilkProductionRepository milkProductionRepository;

    private FarmGoatHistoricalMilkLactationPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new FarmGoatHistoricalMilkLactationPersistenceAdapter(lactationRepository, milkProductionRepository);
    }

    @Test
    @DisplayName("findLactationsByGoat maps entities correctly to technology-neutral domain items")
    void findLactationsByGoatMapsCorrectly() {
        GoatId goatId = new GoatId(42L);
        String regNumber = "BR-42";

        LactationEntity entity = LactationEntity.builder()
                .id(101L)
                .farmId(1L)
                .goatTechnicalId(42L)
                .goatId(regNumber)
                .status(LactationStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 15))
                .endDate(null)
                .pregnancyStartDate(LocalDate.of(2025, 8, 15))
                .dryStartDate(null)
                .dryAtPregnancyDays(60)
                .restDays(30)
                .build();

        when(lactationRepository.findByGoatTechnicalIdOrRg(42L, regNumber))
                .thenReturn(List.of(entity));

        List<FarmGoatHistoricalLactationItem> result = adapter.findLactationsByGoat(goatId, regNumber);

        assertThat(result).hasSize(1);
        FarmGoatHistoricalLactationItem item = result.get(0);
        assertThat(item.id()).isEqualTo(101L);
        assertThat(item.goatId()).isEqualTo(goatId);
        assertThat(item.farmId()).isEqualTo(1L);
        assertThat(item.status()).isEqualTo(LactationStatus.ACTIVE);
        assertThat(item.startDate()).isEqualTo(LocalDate.of(2026, 1, 15));
        assertThat(item.endDate()).isNull();
        assertThat(item.pregnancyStartDate()).isEqualTo(LocalDate.of(2025, 8, 15));
        assertThat(item.dryStartDate()).isNull();
        assertThat(item.dryAtPregnancyDays()).isEqualTo(60);
        assertThat(item.restDays()).isEqualTo(30);
        assertThat(item.active()).isTrue();

        verify(lactationRepository).findByGoatTechnicalIdOrRg(42L, regNumber);
    }

    @Test
    @DisplayName("findLactationsByGoat throws when goatId is null")
    void findLactationsByGoatThrowsOnNullGoatId() {
        assertThatThrownBy(() -> adapter.findLactationsByGoat(null, "BR-42"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("goatId must not be null");
    }

    @Test
    @DisplayName("findMilkProductionsByGoatAndFarm maps entities correctly to technology-neutral domain items")
    void findMilkProductionsByGoatAndFarmMapsCorrectly() {
        GoatId goatId = new GoatId(42L);
        long farmId = 1L;
        String regNumber = "BR-42";

        LactationEntity lactationEntity = LactationEntity.builder().id(200L).build();

        MilkProductionEntity entity = MilkProductionEntity.builder()
                .id(501L)
                .farmId(farmId)
                .goatTechnicalId(42L)
                .goatId(regNumber)
                .lactation(lactationEntity)
                .date(LocalDate.of(2026, 3, 10))
                .shift(MilkingShift.MORNING)
                .volumeLiters(new BigDecimal("3.50"))
                .status(MilkProductionStatus.ACTIVE)
                .notes("Morning milking")
                .canceledAt(LocalDateTime.of(2026, 3, 11, 8, 0))
                .canceledReason("Data entry correction")
                .recordedDuringMilkWithdrawal(true)
                .milkWithdrawalEventId(77L)
                .milkWithdrawalEndDate(LocalDate.of(2026, 3, 15))
                .milkWithdrawalSource("Mastitis Treatment")
                .build();

        when(milkProductionRepository.findHistoricalForDossier(farmId, 42L, regNumber))
                .thenReturn(List.of(entity));

        List<FarmGoatHistoricalMilkProductionItem> result = adapter.findMilkProductionsByGoatAndFarm(farmId, goatId, regNumber);

        assertThat(result).hasSize(1);
        FarmGoatHistoricalMilkProductionItem item = result.get(0);
        assertThat(item.id()).isEqualTo(501L);
        assertThat(item.goatId()).isEqualTo(goatId);
        assertThat(item.lactationId()).isEqualTo(200L);
        assertThat(item.farmId()).isEqualTo(farmId);
        assertThat(item.date()).isEqualTo(LocalDate.of(2026, 3, 10));
        assertThat(item.shift()).isEqualTo(MilkingShift.MORNING);
        assertThat(item.volumeLiters()).isEqualByComparingTo(new BigDecimal("3.50"));
        assertThat(item.status()).isEqualTo(MilkProductionStatus.ACTIVE);
        assertThat(item.notes()).isEqualTo("Morning milking");
        assertThat(item.canceledAt()).isEqualTo(LocalDateTime.of(2026, 3, 11, 8, 0));
        assertThat(item.canceledReason()).isEqualTo("Data entry correction");
        assertThat(item.recordedDuringMilkWithdrawal()).isTrue();
        assertThat(item.milkWithdrawalEventId()).isEqualTo(77L);
        assertThat(item.milkWithdrawalEndDate()).isEqualTo(LocalDate.of(2026, 3, 15));
        assertThat(item.milkWithdrawalSource()).isEqualTo("Mastitis Treatment");

        verify(milkProductionRepository).findHistoricalForDossier(farmId, 42L, regNumber);
    }

    @Test
    @DisplayName("findMilkProductionsByGoatAndFarm throws on invalid arguments")
    void findMilkProductionsByGoatAndFarmThrowsOnInvalidArgs() {
        GoatId goatId = new GoatId(42L);

        assertThatThrownBy(() -> adapter.findMilkProductionsByGoatAndFarm(0L, goatId, "BR-42"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("farmId must be positive");

        assertThatThrownBy(() -> adapter.findMilkProductionsByGoatAndFarm(-1L, goatId, "BR-42"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("farmId must be positive");

        assertThatThrownBy(() -> adapter.findMilkProductionsByGoatAndFarm(1L, null, "BR-42"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("goatId must not be null");
    }
}
