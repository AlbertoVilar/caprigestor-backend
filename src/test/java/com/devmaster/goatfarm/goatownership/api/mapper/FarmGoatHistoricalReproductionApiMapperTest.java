package com.devmaster.goatfarm.goatownership.api.mapper;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalReproductionResponseDTO;
import com.devmaster.goatfarm.goatownership.application.model.*;
import com.devmaster.goatfarm.reproduction.enums.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FarmGoatHistoricalReproductionApiMapperTest {

    private final FarmGoatHistoricalReproductionApiMapper mapper = new FarmGoatHistoricalReproductionApiMapper();

    @Test
    @DisplayName("Maps complete reproduction snapshot to response DTO preserving all fields")
    void toResponse_mapsAllFields() {
        GoatId goatId = GoatId.of(100L);

        MinimalForeignCoverageContext foreignContext = new MinimalForeignCoverageContext(
                55L,
                2L,
                LocalDate.of(2026, 1, 10),
                BreedingType.NATURAL,
                "BOD-01"
        );

        HistoricalReproductionProcessItem process = new HistoricalReproductionProcessItem(
                10L,
                goatId,
                1L,
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 15),
                LocalDate.of(2026, 6, 9),
                55L,
                PregnancyStatus.CLOSED,
                LocalDate.of(2026, 6, 9),
                PregnancyCloseReason.BIRTH,
                foreignContext
        );

        HistoricalReproductionEventItem event = new HistoricalReproductionEventItem(
                201L,
                goatId,
                1L,
                ReproductiveEventType.PREGNANCY_CLOSE,
                LocalDate.of(2026, 6, 9),
                null,
                null,
                10L,
                null,
                null,
                null,
                null,
                "Parto duplo"
        );

        FarmGoatHistoricalReproductionSnapshot snapshot = new FarmGoatHistoricalReproductionSnapshot(
                goatId,
                List.of(process),
                List.of(event)
        );

        FarmGoatHistoricalReproductionResponseDTO response = mapper.toResponse(snapshot);

        assertThat(response).isNotNull();
        assertThat(response.goatId()).isEqualTo(100L);

        assertThat(response.processes()).hasSize(1);
        var procDto = response.processes().get(0);
        assertThat(procDto.pregnancyId()).isEqualTo(10L);
        assertThat(procDto.processOriginFarmId()).isEqualTo(1L);
        assertThat(procDto.breedingDate()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(procDto.confirmDate()).isEqualTo(LocalDate.of(2026, 3, 15));
        assertThat(procDto.expectedDueDate()).isEqualTo(LocalDate.of(2026, 6, 9));
        assertThat(procDto.coverageEventId()).isEqualTo(55L);
        assertThat(procDto.status()).isEqualTo(PregnancyStatus.CLOSED);
        assertThat(procDto.closedAt()).isEqualTo(LocalDate.of(2026, 6, 9));
        assertThat(procDto.closeReason()).isEqualTo(PregnancyCloseReason.BIRTH);

        assertThat(procDto.foreignCoverageContext()).isNotNull();
        assertThat(procDto.foreignCoverageContext().coverageEventId()).isEqualTo(55L);
        assertThat(procDto.foreignCoverageContext().originFarmId()).isEqualTo(2L);
        assertThat(procDto.foreignCoverageContext().coverageDate()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(procDto.foreignCoverageContext().breedingType()).isEqualTo(BreedingType.NATURAL);
        assertThat(procDto.foreignCoverageContext().breederRef()).isEqualTo("BOD-01");

        assertThat(response.events()).hasSize(1);
        var eventDto = response.events().get(0);
        assertThat(eventDto.id()).isEqualTo(201L);
        assertThat(eventDto.farmId()).isEqualTo(1L);
        assertThat(eventDto.eventType()).isEqualTo(ReproductiveEventType.PREGNANCY_CLOSE);
        assertThat(eventDto.eventDate()).isEqualTo(LocalDate.of(2026, 6, 9));
        assertThat(eventDto.pregnancyId()).isEqualTo(10L);
        assertThat(eventDto.notes()).isEqualTo("Parto duplo");
    }

    @Test
    @DisplayName("Maps standalone COVERAGE event preserving breedingType and breederRef")
    void toEventDto_standaloneCoverage() {
        var item = new HistoricalReproductionEventItem(
                1L, GoatId.of(10L), 2L,
                ReproductiveEventType.COVERAGE,
                LocalDate.of(2026, 2, 1),
                BreedingType.AI,
                "SEMEN-99",
                null, null, null, null, null,
                "IA executada"
        );

        var dto = mapper.toEventDto(item);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.farmId()).isEqualTo(2L);
        assertThat(dto.eventType()).isEqualTo(ReproductiveEventType.COVERAGE);
        assertThat(dto.breedingType()).isEqualTo(BreedingType.AI);
        assertThat(dto.breederRef()).isEqualTo("SEMEN-99");
        assertThat(dto.pregnancyId()).isNull();
    }

    @Test
    @DisplayName("Maps COVERAGE_CORRECTION preserving relatedEventId and correctedEventDate")
    void toEventDto_coverageCorrection() {
        var item = new HistoricalReproductionEventItem(
                2L, GoatId.of(10L), 2L,
                ReproductiveEventType.COVERAGE_CORRECTION,
                LocalDate.of(2026, 2, 3),
                null, null, null,
                1L,
                LocalDate.of(2026, 2, 2),
                null, null,
                "Correção de data"
        );

        var dto = mapper.toEventDto(item);

        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.relatedEventId()).isEqualTo(1L);
        assertThat(dto.correctedEventDate()).isEqualTo(LocalDate.of(2026, 2, 2));
    }

    @Test
    @DisplayName("Maps standalone PREGNANCY_CHECK with NEGATIVE result and null pregnancyId")
    void toEventDto_negativePregnancyCheck() {
        var item = new HistoricalReproductionEventItem(
                3L, GoatId.of(10L), 2L,
                ReproductiveEventType.PREGNANCY_CHECK,
                LocalDate.of(2026, 3, 15),
                null, null, null, null, null,
                LocalDate.of(2026, 3, 15),
                PregnancyCheckResult.NEGATIVE,
                "Vazia"
        );

        var dto = mapper.toEventDto(item);

        assertThat(dto.id()).isEqualTo(3L);
        assertThat(dto.checkResult()).isEqualTo(PregnancyCheckResult.NEGATIVE);
        assertThat(dto.checkScheduledDate()).isEqualTo(LocalDate.of(2026, 3, 15));
        assertThat(dto.pregnancyId()).isNull();
    }

    @Test
    @DisplayName("Maps standalone PREGNANCY_CHECK with POSITIVE result and null pregnancyId")
    void toEventDto_positivePregnancyCheck() {
        var item = new HistoricalReproductionEventItem(
                4L, GoatId.of(10L), 2L,
                ReproductiveEventType.PREGNANCY_CHECK,
                LocalDate.of(2026, 3, 15),
                null, null, null, null, null,
                LocalDate.of(2026, 3, 15),
                PregnancyCheckResult.POSITIVE,
                "Positiva"
        );

        var dto = mapper.toEventDto(item);

        assertThat(dto.id()).isEqualTo(4L);
        assertThat(dto.checkResult()).isEqualTo(PregnancyCheckResult.POSITIVE);
        assertThat(dto.pregnancyId()).isNull();
    }

    @Test
    @DisplayName("Returns null when snapshot is null")
    void toResponse_nullSnapshot_returnsNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }
}
