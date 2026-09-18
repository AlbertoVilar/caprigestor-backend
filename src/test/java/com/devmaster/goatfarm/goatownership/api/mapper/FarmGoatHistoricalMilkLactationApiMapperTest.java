package com.devmaster.goatfarm.goatownership.api.mapper;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalMilkLactationResponseDTO;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalLactationItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalMilkLactationSnapshot;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalMilkProductionItem;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FarmGoatHistoricalMilkLactationApiMapperTest {

    private final FarmGoatHistoricalMilkLactationApiMapper mapper = new FarmGoatHistoricalMilkLactationApiMapper();

    @Test
    @DisplayName("Maps complete snapshot to response DTO preserving all fields")
    void toResponse_mapsAllFields() {
        GoatId goatId = GoatId.of(100L);
        FarmGoatHistoricalLactationItem lac = new FarmGoatHistoricalLactationItem(
                10L,
                goatId,
                1L,
                LactationStatus.ACTIVE,
                LocalDate.of(2025, 1, 1),
                null,
                LocalDate.of(2025, 2, 1),
                LocalDate.of(2025, 5, 1),
                90,
                60,
                true
        );

        FarmGoatHistoricalMilkProductionItem prod = new FarmGoatHistoricalMilkProductionItem(
                200L,
                goatId,
                10L,
                1L,
                LocalDate.of(2025, 1, 5),
                MilkingShift.MORNING,
                new BigDecimal("3.25"),
                MilkProductionStatus.ACTIVE,
                "Morning test",
                null,
                null,
                false,
                null,
                null,
                null
        );

        FarmGoatHistoricalMilkLactationSnapshot snapshot = new FarmGoatHistoricalMilkLactationSnapshot(
                goatId,
                List.of(lac),
                List.of(prod)
        );

        FarmGoatHistoricalMilkLactationResponseDTO response = mapper.toResponse(snapshot);

        assertThat(response).isNotNull();
        assertThat(response.goatId()).isEqualTo(100L);

        assertThat(response.lactations()).hasSize(1);
        var lacDto = response.lactations().get(0);
        assertThat(lacDto.id()).isEqualTo(10L);
        assertThat(lacDto.goatId()).isEqualTo(100L);
        assertThat(lacDto.farmId()).isEqualTo(1L);
        assertThat(lacDto.status()).isEqualTo(LactationStatus.ACTIVE);
        assertThat(lacDto.startDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(lacDto.endDate()).isNull();
        assertThat(lacDto.pregnancyStartDate()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(lacDto.dryStartDate()).isEqualTo(LocalDate.of(2025, 5, 1));
        assertThat(lacDto.dryAtPregnancyDays()).isEqualTo(90);
        assertThat(lacDto.restDays()).isEqualTo(60);
        assertThat(lacDto.active()).isTrue();

        assertThat(response.milkProductions()).hasSize(1);
        var prodDto = response.milkProductions().get(0);
        assertThat(prodDto.id()).isEqualTo(200L);
        assertThat(prodDto.goatId()).isEqualTo(100L);
        assertThat(prodDto.lactationId()).isEqualTo(10L);
        assertThat(prodDto.farmId()).isEqualTo(1L);
        assertThat(prodDto.date()).isEqualTo(LocalDate.of(2025, 1, 5));
        assertThat(prodDto.shift()).isEqualTo(MilkingShift.MORNING);
        assertThat(prodDto.volumeLiters()).isEqualByComparingTo(new BigDecimal("3.25"));
        assertThat(prodDto.status()).isEqualTo(MilkProductionStatus.ACTIVE);
        assertThat(prodDto.notes()).isEqualTo("Morning test");
        assertThat(prodDto.recordedDuringMilkWithdrawal()).isFalse();
    }

    @Test
    @DisplayName("Returns null when snapshot is null")
    void toResponse_nullSnapshot_returnsNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }
}