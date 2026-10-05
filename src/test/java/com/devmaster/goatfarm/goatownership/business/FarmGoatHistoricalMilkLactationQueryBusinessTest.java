package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.application.model.*;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatRegistryQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalMilkLactationQueryPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipQueryPort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmGoatHistoricalMilkLactationQueryBusinessTest {

    private static final ZoneId DOMAIN_ZONE = ZoneId.of("America/Sao_Paulo");

    @Mock
    private FarmGoatRegistryQueryUseCase registryQueryUseCase;
    @Mock
    private GoatOwnershipQueryPort goatOwnershipQueryPort;
    @Mock
    private FarmGoatHistoricalMilkLactationQueryPort queryPort;

    private FarmGoatHistoricalMilkLactationQueryBusiness business;

    private static final Long FARM_A = 1L;
    private static final Long FARM_B = 2L;
    private static final Long FARM_C = 3L;
    private static final GoatId GOAT_ID = GoatId.of(100L);
    private static final String REG_NO = "RG-100";

    private FarmGoatRegistryItem defaultRegistryItem;

    @BeforeEach
    void setUp() {
        business = new FarmGoatHistoricalMilkLactationQueryBusiness(
                registryQueryUseCase,
                goatOwnershipQueryPort,
                queryPort
        );

        defaultRegistryItem = new FarmGoatRegistryItem(
                GOAT_ID, REG_NO, "Serena", GoatStatus.ATIVO, FARM_A, "Capril A",
                Set.of(FarmGoatRegistryRole.CURRENT_OWNER), FarmGoatRegistryDisposition.CURRENT, FARM_A
        );
    }

    private Instant toInstant(LocalDate date) {
        return date.atStartOfDay(DOMAIN_ZONE).toInstant();
    }

    private GoatOwnershipPeriod closedPeriod(long farmId, LocalDate start, LocalDate end) {
        return GoatOwnershipPeriod.rehydrate(
                1L, GOAT_ID, farmId,
                toInstant(start), toInstant(end),
                OwnershipEntryType.PURCHASE, OwnershipExitType.EXTERNAL_SALE, "test"
        );
    }

    private GoatOwnershipPeriod openPeriod(long farmId, LocalDate start) {
        return GoatOwnershipPeriod.rehydrate(
                1L, GOAT_ID, farmId,
                toInstant(start), null,
                OwnershipEntryType.PURCHASE, null, "test"
        );
    }

    private FarmGoatHistoricalLactationItem lactation(Long id, Long farmId, LocalDate start, LocalDate end) {
        return new FarmGoatHistoricalLactationItem(
                id, GOAT_ID, farmId, LactationStatus.ACTIVE,
                start, end, null, null, null, null, true
        );
    }

    private FarmGoatHistoricalMilkProductionItem production(Long id, Long farmId, Long lactationId, LocalDate date, MilkingShift shift) {
        return new FarmGoatHistoricalMilkProductionItem(
                id, GOAT_ID, lactationId, farmId, date, shift,
                new BigDecimal("2.50"), MilkProductionStatus.ACTIVE,
                null, null, null, false, null, null, null
        );
    }

    @Test
    @DisplayName("Throws when farmId is invalid")
    void invalidFarmId_throwsException() {
        assertThatThrownBy(() -> business.findHistoricalMilkLactation(0L, GOAT_ID))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> business.findHistoricalMilkLactation(-1L, GOAT_ID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Throws when goatId is null")
    void nullGoatId_throwsException() {
        assertThatThrownBy(() -> business.findHistoricalMilkLactation(FARM_A, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Unrelated farm receives Optional.empty()")
    void unrelatedFarm_returnsEmpty() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_C, GOAT_ID)).thenReturn(Optional.empty());

        Optional<FarmGoatHistoricalMilkLactationSnapshot> result =
                business.findHistoricalMilkLactation(FARM_C, GOAT_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Case A: Lactation completely inside ownership period -> VISIBLE")
    void caseA_lactationCompletelyInsideOwnershipPeriod_visible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 1));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).containsExactly(lac);
    }

    @Test
    @DisplayName("Case B: Lactation starts before ownership period and ends inside it -> VISIBLE")
    void caseB_lactationStartsBeforeAndEndsInside_visible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 4, 1));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).containsExactly(lac);
    }

    @Test
    @DisplayName("Case C: Lactation starts inside ownership period and ends after it -> VISIBLE")
    void caseC_lactationStartsInsideAndEndsAfter_visible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 7, 1));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).containsExactly(lac);
    }

    @Test
    @DisplayName("Case D: Lactation encompasses entire ownership period -> VISIBLE")
    void caseD_lactationEncompassesPeriod_visible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 7, 1));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).containsExactly(lac);
    }

    @Test
    @DisplayName("Case E: Lactation strictly before ownership period -> NOT VISIBLE")
    void caseE_lactationStrictlyBeforePeriod_notVisible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).isEmpty();
    }

    @Test
    @DisplayName("Case F: Lactation strictly after ownership period -> NOT VISIBLE")
    void caseF_lactationStrictlyAfterPeriod_notVisible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 6, 2), LocalDate.of(2026, 7, 1));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).isEmpty();
    }

    @Test
    @DisplayName("Required A: Ownership ends on same civil date lactation starts -> Condition 3 FALSE (not visible)")
    void ownershipEndsOnSameCivilDateLactationStarts_condition3False_notVisible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        // Lactation starts on 2026-06-01 (same date ownership ends) and originated on FARM_B
        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 1));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).isEmpty();
    }

    @Test
    @DisplayName("Required B: Ownership starts on same civil date closed lactation ends -> Condition 3 FALSE (not visible)")
    void ownershipStartsOnSameCivilDateClosedLactationEnds_condition3False_notVisible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        // Lactation ends on 2026-03-01 (same date ownership starts) and originated on FARM_B
        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).isEmpty();
    }

    @Test
    @DisplayName("Required C: Ownership starts on lactation start date, but lactation continues beyond -> Condition 3 TRUE (visible)")
    void ownershipStartsOnLactationStartDate_lactationContinuesBeyond_condition3True_visible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        // Lactation starts on 2026-03-01 and ends on 2026-05-01 (unambiguous continuation after boundary)
        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 5, 1));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).containsExactly(lac);
    }

    @Test
    @DisplayName("Required D: Ownership ends on lactation end date, but lactation started before -> Condition 3 TRUE (visible)")
    void ownershipEndsOnLactationEndDate_lactationStartedBefore_condition3True_visible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        // Lactation started on 2026-05-01 and ends on 2026-06-01 (unambiguous overlap before end boundary)
        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 6, 1));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).containsExactly(lac);
    }

    @Test
    @DisplayName("Required E: Active lactation + open/current ownership -> Condition 3 TRUE (visible)")
    void activeLactation_openOwnership_timelinesOverlap_condition3True_visible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                openPeriod(FARM_A, LocalDate.of(2026, 3, 1))
        ));

        // Active lactation (endDate == null) started on 2026-04-01 during open period
        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 4, 1), null);
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).containsExactly(lac);
    }

    @Test
    @DisplayName("Required F: Closed ownership ending before lactation starts -> Condition 3 FALSE (not visible)")
    void closedOwnershipEndingBeforeLactationStarts_condition3False_notVisible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        // Ownership ended on 2026-06-01, lactation starts on 2026-06-02
        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 6, 2), LocalDate.of(2026, 7, 1));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).isEmpty();
    }

    @Test
    @DisplayName("Required G: Ownership beginning after closed lactation ends -> Condition 3 FALSE (not visible)")
    void ownershipBeginningAfterClosedLactationEnds_condition3False_notVisible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        // Lactation ended on 2026-02-28, ownership begins on 2026-03-01
        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).isEmpty();
    }

    @Test
    @DisplayName("Required H: Source farm with ambiguous transfer-day overlap but lactation.farmId == source -> Condition 1 TRUE (visible)")
    void sourceFarmAmbiguousTransferDayOverlap_lactationOriginatedOnSourceFarm_condition1True_visible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        // Ownership ends on 2026-06-01, lactation starts on 2026-06-01 (ambiguous transfer day)
        // But lactation originated on FARM_A (farmId == FARM_A)
        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_A, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 7, 1));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).containsExactly(lac);
    }

    @Test
    @DisplayName("Required I: Target farm with ambiguous transfer-day overlap but its own MilkProduction exists -> Condition 2 TRUE (visible)")
    void targetFarmAmbiguousTransferDayOverlap_milkProductionExistsOnTargetFarm_condition2True_visible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 12, 1))
        ));

        // Ownership starts on 2026-06-01, closed lactation ends on 2026-06-01, originated on FARM_B (ambiguous transfer day)
        FarmGoatHistoricalLactationItem lac = lactation(99L, FARM_B, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        // But FARM_A has its own recorded milk production referencing lactation 99L
        FarmGoatHistoricalMilkProductionItem prod = production(1001L, FARM_A, 99L, LocalDate.of(2026, 6, 1), MilkingShift.MORNING);
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of(prod));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).containsExactly(lac);
        assertThat(snapshot.get().milkProductions()).containsExactly(prod);
    }

    @Test
    @DisplayName("Open lactation (endDate == null) overlapping closed period -> VISIBLE")
    void openLactation_overlappingClosedPeriod_visible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 5, 1), null);
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).containsExactly(lac);
    }

    @Test
    @DisplayName("Open lactation (endDate == null) starting after closed period -> NOT VISIBLE")
    void openLactation_startingAfterClosedPeriod_notVisible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 6, 2), null);
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).isEmpty();
    }

    @Test
    @DisplayName("Open period (endedAt == null) overlapping closed lactation -> VISIBLE")
    void openPeriod_overlappingClosedLactation_visible() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                openPeriod(FARM_A, LocalDate.of(2026, 3, 1))
        ));

        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_B, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 1));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).containsExactly(lac);
    }

    @Test
    @DisplayName("Condition 1: Lactation originated on farmId is visible even without ownership period overlap")
    void condition1_originatedOnFarm_visibleWithoutOverlap() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of());
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        // Lactation originated on FARM_A, but dated before the ownership period
        FarmGoatHistoricalLactationItem lac = lactation(10L, FARM_A, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).containsExactly(lac);
    }

    @Test
    @DisplayName("Condition 2: Lactation referenced by farmId milk production is visible even without ownership overlap")
    void condition2_referencedByProduction_visibleWithoutOverlap() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 1))
        ));

        // Lactation originated on FARM_B and strictly before FARM_A ownership period
        FarmGoatHistoricalLactationItem lac = lactation(99L, FARM_B, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 2, 28));
        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(lac));

        // But FARM_A has a production referencing lactation 99L
        FarmGoatHistoricalMilkProductionItem prod = production(1001L, FARM_A, 99L, LocalDate.of(2026, 3, 15), MilkingShift.MORNING);
        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of(prod));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        assertThat(snapshot.get().lactations()).containsExactly(lac);
        assertThat(snapshot.get().milkProductions()).containsExactly(prod);
    }

    @Test
    @DisplayName("Deterministic sorting for lactations (startDate DESC, id DESC) and milk productions (date DESC, shift ASC, id DESC)")
    void deterministicSorting_appliedProperly() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItem));
        when(goatOwnershipQueryPort.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                closedPeriod(FARM_A, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31))
        ));

        FarmGoatHistoricalLactationItem l1 = lactation(10L, FARM_A, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30));
        FarmGoatHistoricalLactationItem l2 = lactation(5L, FARM_A, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));
        FarmGoatHistoricalLactationItem l3 = lactation(12L, FARM_A, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31));

        when(queryPort.findLactationsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(l1, l2, l3));

        FarmGoatHistoricalMilkProductionItem p1 = production(1L, FARM_A, 10L, LocalDate.of(2026, 4, 2), MilkingShift.MORNING);
        FarmGoatHistoricalMilkProductionItem p2 = production(2L, FARM_A, 12L, LocalDate.of(2026, 4, 5), MilkingShift.AFTERNOON);
        FarmGoatHistoricalMilkProductionItem p3 = production(3L, FARM_A, 12L, LocalDate.of(2026, 4, 5), MilkingShift.MORNING);
        FarmGoatHistoricalMilkProductionItem p4 = production(4L, FARM_A, 12L, LocalDate.of(2026, 4, 5), MilkingShift.MORNING);

        when(queryPort.findMilkProductionsByGoatAndFarm(FARM_A, GOAT_ID, REG_NO)).thenReturn(List.of(p1, p2, p3, p4));

        Optional<FarmGoatHistoricalMilkLactationSnapshot> snapshot = business.findHistoricalMilkLactation(FARM_A, GOAT_ID);

        assertThat(snapshot).isPresent();
        // Lactation sorting: startDate DESC, id DESC
        // l3 (2026-05-01, id 12), l2 (2026-05-01, id 5), l1 (2026-04-01, id 10)
        assertThat(snapshot.get().lactations()).containsExactly(l3, l2, l1);

        // Production sorting: date DESC, shift ASC, id DESC
        // 2026-04-05 MORNING (p4 id 4, p3 id 3), 2026-04-05 AFTERNOON (p2 id 2), 2026-04-02 MORNING (p1 id 1)
        assertThat(snapshot.get().milkProductions()).containsExactly(p4, p3, p2, p1);
    }
}