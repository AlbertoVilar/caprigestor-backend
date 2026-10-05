package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.application.model.*;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatRegistryQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalReproductionQueryPort;
import com.devmaster.goatfarm.reproduction.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmGoatHistoricalReproductionQueryBusinessTest {

    @Mock
    private FarmGoatRegistryQueryUseCase registryQueryUseCase;

    @Mock
    private FarmGoatHistoricalReproductionQueryPort queryPort;

    private FarmGoatHistoricalReproductionQueryBusiness business;

    private static final Long FARM_A = 1L;
    private static final Long FARM_B = 2L;
    private static final GoatId GOAT_ID = GoatId.of(100L);
    private static final String REG_NO = "RG-100";

    private FarmGoatRegistryItem defaultRegistryItemA;
    private FarmGoatRegistryItem defaultRegistryItemB;

    @BeforeEach
    void setUp() {
        business = new FarmGoatHistoricalReproductionQueryBusiness(registryQueryUseCase, queryPort);

        defaultRegistryItemA = new FarmGoatRegistryItem(
                GOAT_ID, REG_NO, "Estrela", GoatStatus.ATIVO, FARM_A, "Capril A",
                Set.of(FarmGoatRegistryRole.FORMER_OWNER), FarmGoatRegistryDisposition.TRANSFERRED, FARM_A
        );

        defaultRegistryItemB = new FarmGoatRegistryItem(
                GOAT_ID, REG_NO, "Estrela", GoatStatus.ATIVO, FARM_B, "Capril B",
                Set.of(FarmGoatRegistryRole.CURRENT_OWNER), FarmGoatRegistryDisposition.CURRENT, FARM_B
        );
    }

    private HistoricalReproductionEventItem event(Long id, Long farmId, ReproductiveEventType type, LocalDate date) {
        return new HistoricalReproductionEventItem(
                id, GOAT_ID, farmId, type, date,
                BreedingType.NATURAL, "BOD-01", null, null, null, null, null, "Nota evento " + id
        );
    }

    private HistoricalReproductionEventItem closureEvent(Long id, Long farmId, Long pregnancyId, LocalDate date) {
        return new HistoricalReproductionEventItem(
                id, GOAT_ID, farmId, ReproductiveEventType.PREGNANCY_CLOSE, date,
                null, null, pregnancyId, null, null, null, null, "Parto registrado"
        );
    }

    private HistoricalReproductionProcessCandidate process(Long id, Long farmId, PregnancyStatus status,
                                                           LocalDate breedingDate, LocalDate confirmDate,
                                                           LocalDate closedAt, PregnancyCloseReason closeReason,
                                                           Long coverageEventId) {
        LocalDate expectedDue = breedingDate != null ? breedingDate.plusDays(150) : null;
        return new HistoricalReproductionProcessCandidate(
                id, GOAT_ID, farmId, status, breedingDate, confirmDate, expectedDue,
                closedAt, closeReason, coverageEventId, "Process notes " + id
        );
    }

    @Test
    @DisplayName("1. Registry membership absent -> Optional.empty")
    void test01_registryMembershipAbsent_returnsEmpty() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.empty());

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_A, GOAT_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Invalid arguments throw IllegalArgumentException")
    void test_invalidArguments_throws() {
        assertThatThrownBy(() -> business.findHistoricalReproduction(0L, GOAT_ID))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> business.findHistoricalReproduction(FARM_A, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("2. Farm A standalone coverage visible to A")
    void test02_standaloneCoverage_visibleToFarmA() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        HistoricalReproductionEventItem covA = event(1L, FARM_A, ReproductiveEventType.COVERAGE, LocalDate.of(2026, 1, 10));
        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(covA));
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(Collections.emptyList());

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().events()).hasSize(1);
        assertThat(result.get().events().get(0).id()).isEqualTo(1L);
        assertThat(result.get().events().get(0).eventType()).isEqualTo(ReproductiveEventType.COVERAGE);
        assertThat(result.get().processes()).isEmpty();
    }

    @Test
    @DisplayName("3. Farm B event not visible to A")
    void test03_farmBEvent_notVisibleToFarmA() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        HistoricalReproductionEventItem covA = event(1L, FARM_A, ReproductiveEventType.COVERAGE, LocalDate.of(2026, 1, 10));
        HistoricalReproductionEventItem checkB = event(2L, FARM_B, ReproductiveEventType.PREGNANCY_CHECK, LocalDate.of(2026, 3, 15));

        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(covA, checkB));
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(Collections.emptyList());

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().events()).hasSize(1);
        assertThat(result.get().events().get(0).id()).isEqualTo(1L);
        assertThat(result.get().events().get(0).farmId()).isEqualTo(FARM_A);
    }

    @Test
    @DisplayName("4. A coverage linked to B pregnancy does NOT expose B pregnancy to A")
    void test04_coverageLinkedToForeignPregnancy_doesNotExposePregnancyToFormerOwner() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        HistoricalReproductionEventItem covA = event(1L, FARM_A, ReproductiveEventType.COVERAGE, LocalDate.of(2026, 1, 10));
        HistoricalReproductionProcessCandidate pregB = process(
                10L, FARM_B, PregnancyStatus.ACTIVE, LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 15), null, null, 1L
        );

        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(covA));
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(pregB));

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().events()).hasSize(1);
        assertThat(result.get().events().get(0).id()).isEqualTo(1L);
        // B's pregnancy must NOT be exposed to A
        assertThat(result.get().processes()).isEmpty();
    }

    @Test
    @DisplayName("5. B-owned pregnancy linked to A coverage returns only approved minimal foreign coverage context to B")
    void test05_bOwnedPregnancyLinkedToACoverage_returnsMinimalForeignCoverageContext() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_B, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemB));

        HistoricalReproductionEventItem covA = event(1L, FARM_A, ReproductiveEventType.COVERAGE, LocalDate.of(2026, 1, 10));
        HistoricalReproductionProcessCandidate pregB = process(
                10L, FARM_B, PregnancyStatus.ACTIVE, LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 15), null, null, 1L
        );

        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(covA));
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(pregB));

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_B, GOAT_ID);

        assertThat(result).isPresent();
        // A's coverage event must NOT appear in B's event stream
        assertThat(result.get().events()).isEmpty();

        // B's pregnancy is visible and carries minimal foreign coverage context
        assertThat(result.get().processes()).hasSize(1);
        HistoricalReproductionProcessItem p = result.get().processes().get(0);
        assertThat(p.pregnancyId()).isEqualTo(10L);
        assertThat(p.processOriginFarmId()).isEqualTo(FARM_B);
        assertThat(p.coverageEventId()).isEqualTo(1L);
        assertThat(p.foreignCoverageContext()).isNotNull();
        assertThat(p.foreignCoverageContext().coverageEventId()).isEqualTo(1L);
        assertThat(p.foreignCoverageContext().originFarmId()).isEqualTo(FARM_A);
        assertThat(p.foreignCoverageContext().coverageDate()).isEqualTo(LocalDate.of(2026, 1, 10));
        assertThat(p.foreignCoverageContext().breedingType()).isEqualTo(BreedingType.NATURAL);
        assertThat(p.foreignCoverageContext().breederRef()).isEqualTo("BOD-01");
    }

    @Test
    @DisplayName("6. A-owned Pregnancy visible to A")
    void test06_aOwnedPregnancy_visibleToFarmA() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        HistoricalReproductionProcessCandidate pregA = process(
                10L, FARM_A, PregnancyStatus.ACTIVE, LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 15), null, null, null
        );

        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(Collections.emptyList());
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(pregA));

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().processes()).hasSize(1);
        HistoricalReproductionProcessItem p = result.get().processes().get(0);
        assertThat(p.pregnancyId()).isEqualTo(10L);
        assertThat(p.processOriginFarmId()).isEqualTo(FARM_A);
        assertThat(p.status()).isEqualTo(PregnancyStatus.ACTIVE);
        assertThat(p.confirmDate()).isEqualTo(LocalDate.of(2026, 3, 15));
        assertThat(p.foreignCoverageContext()).isNull();
    }

    @Test
    @DisplayName("7. A Pregnancy later closed by B: A does NOT receive B closedAt/closeReason/status transition")
    void test07_aPregnancyClosedByB_formerOwnerDoesNotReceiveClosedAtCloseReasonOrStatus() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        // Pregnancy created under Farm A, but closed in DB with closedAt = 2026-06-01, closeReason = BIRTH
        HistoricalReproductionProcessCandidate pregA = process(
                10L, FARM_A, PregnancyStatus.CLOSED, LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 15), LocalDate.of(2026, 6, 1), PregnancyCloseReason.BIRTH, null
        );
        // Closure event recorded by Farm B
        HistoricalReproductionEventItem closeB = closureEvent(20L, FARM_B, 10L, LocalDate.of(2026, 6, 1));

        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(closeB));
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(pregA));

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        // B's closure event must NOT be in A's event stream
        assertThat(result.get().events()).isEmpty();

        assertThat(result.get().processes()).hasSize(1);
        HistoricalReproductionProcessItem p = result.get().processes().get(0);
        assertThat(p.pregnancyId()).isEqualTo(10L);
        assertThat(p.processOriginFarmId()).isEqualTo(FARM_A);
        // Critical: closedAt, closeReason, and status must NOT leak Farm B's transition to Farm A
        assertThat(p.closedAt()).isNull();
        assertThat(p.closeReason()).isNull();
        assertThat(p.status()).isNull(); // Unexposed/unknown, never fabricated ACTIVE
    }

    @Test
    @DisplayName("8. Foreign pregnancy with B-owned closure event: B receives only minimal process context + its own closure")
    void test08_foreignPregnancyWithOwnClosure_farmBReceivesMinimalProcessContextAndOwnClosure() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_B, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemB));

        // Pregnancy created under Farm A with private confirmDate and expectedDueDate
        HistoricalReproductionProcessCandidate pregA = process(
                10L, FARM_A, PregnancyStatus.CLOSED, LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 15), LocalDate.of(2026, 6, 1), PregnancyCloseReason.BIRTH, 5L
        );
        // Closure event recorded by Farm B
        HistoricalReproductionEventItem closeB = closureEvent(20L, FARM_B, 10L, LocalDate.of(2026, 6, 1));

        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(closeB));
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(pregA));

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_B, GOAT_ID);

        assertThat(result).isPresent();
        // B sees its own closure event
        assertThat(result.get().events()).hasSize(1);
        assertThat(result.get().events().get(0).id()).isEqualTo(20L);

        // B sees minimal foreign process context
        assertThat(result.get().processes()).hasSize(1);
        HistoricalReproductionProcessItem p = result.get().processes().get(0);
        assertThat(p.pregnancyId()).isEqualTo(10L);
        assertThat(p.processOriginFarmId()).isEqualTo(FARM_A);
        assertThat(p.breedingDate()).isEqualTo(LocalDate.of(2026, 1, 10));
        // Critical: A's private confirmation and expected due dates are redacted!
        assertThat(p.confirmDate()).isNull();
        assertThat(p.expectedDueDate()).isNull();
        // B sees its own closure facts
        assertThat(p.status()).isEqualTo(PregnancyStatus.CLOSED);
        assertThat(p.closedAt()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(p.closeReason()).isEqualTo(PregnancyCloseReason.BIRTH);
    }

    @Test
    @DisplayName("9. Valid unique closure correlation exposes closeReason/closedAt")
    void test09_validUniqueClosureCorrelation_exposesClosureFields() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        HistoricalReproductionProcessCandidate pregA = process(
                10L, FARM_A, PregnancyStatus.CLOSED, LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 15), LocalDate.of(2026, 6, 1), PregnancyCloseReason.BIRTH, null
        );
        HistoricalReproductionEventItem closeA = closureEvent(20L, FARM_A, 10L, LocalDate.of(2026, 6, 1));

        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(closeA));
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(pregA));

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().processes()).hasSize(1);
        HistoricalReproductionProcessItem p = result.get().processes().get(0);
        assertThat(p.closedAt()).isEqualTo(LocalDate.of(2026, 6, 1));
        assertThat(p.closeReason()).isEqualTo(PregnancyCloseReason.BIRTH);
        assertThat(p.status()).isEqualTo(PregnancyStatus.CLOSED);
    }

    @Test
    @DisplayName("10. Missing closure event fails closed")
    void test10_missingClosureEvent_failsClosed() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        // Pregnancy is CLOSED in DB, but NO closure event exists
        HistoricalReproductionProcessCandidate pregA = process(
                10L, FARM_A, PregnancyStatus.CLOSED, LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 15), LocalDate.of(2026, 6, 1), PregnancyCloseReason.BIRTH, null
        );

        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(Collections.emptyList());
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(pregA));

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().processes()).hasSize(1);
        HistoricalReproductionProcessItem p = result.get().processes().get(0);
        // Fails closed: closedAt, closeReason, and status are null
        assertThat(p.closedAt()).isNull();
        assertThat(p.closeReason()).isNull();
        assertThat(p.status()).isNull();
    }

    @Test
    @DisplayName("11. Multiple matching closure events fail closed")
    void test11_multipleMatchingClosureEvents_failsClosed() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        HistoricalReproductionProcessCandidate pregA = process(
                10L, FARM_A, PregnancyStatus.CLOSED, LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 15), LocalDate.of(2026, 6, 1), PregnancyCloseReason.BIRTH, null
        );
        HistoricalReproductionEventItem close1 = closureEvent(20L, FARM_A, 10L, LocalDate.of(2026, 6, 1));
        HistoricalReproductionEventItem close2 = closureEvent(21L, FARM_A, 10L, LocalDate.of(2026, 6, 1));

        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(close1, close2));
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(pregA));

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().processes()).hasSize(1);
        HistoricalReproductionProcessItem p = result.get().processes().get(0);
        // Ambiguous -> fails closed
        assertThat(p.closedAt()).isNull();
        assertThat(p.closeReason()).isNull();
        assertThat(p.status()).isNull();
    }

    @Test
    @DisplayName("12. Closure event date mismatch fails closed")
    void test12_closureEventDateMismatch_failsClosed() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        HistoricalReproductionProcessCandidate pregA = process(
                10L, FARM_A, PregnancyStatus.CLOSED, LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 15), LocalDate.of(2026, 6, 1), PregnancyCloseReason.BIRTH, null
        );
        // Event date is 2026-06-02 (mismatch with closedAt 2026-06-01)
        HistoricalReproductionEventItem closeA = closureEvent(20L, FARM_A, 10L, LocalDate.of(2026, 6, 2));

        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(closeA));
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(pregA));

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().processes()).hasSize(1);
        HistoricalReproductionProcessItem p = result.get().processes().get(0);
        assertThat(p.closedAt()).isNull();
        assertThat(p.closeReason()).isNull();
        assertThat(p.status()).isNull();
    }

    @Test
    @DisplayName("13. Standalone NEGATIVE check remains independent event")
    void test13_standaloneNegativeCheck_remainsIndependentEvent() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        HistoricalReproductionEventItem negCheck = new HistoricalReproductionEventItem(
                30L, GOAT_ID, FARM_A, ReproductiveEventType.PREGNANCY_CHECK, LocalDate.of(2026, 3, 1),
                null, null, null, null, null, null, PregnancyCheckResult.NEGATIVE, "Diagnóstico negativo"
        );

        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(negCheck));
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(Collections.emptyList());

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().events()).hasSize(1);
        assertThat(result.get().events().get(0).id()).isEqualTo(30L);
        assertThat(result.get().events().get(0).checkResult()).isEqualTo(PregnancyCheckResult.NEGATIVE);
        assertThat(result.get().events().get(0).pregnancyId()).isNull();
        assertThat(result.get().processes()).isEmpty();
    }

    @Test
    @DisplayName("14. POSITIVE check with pregnancyId NULL remains independent event")
    void test14_positiveCheckWithNullPregnancyId_remainsIndependentEvent() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        HistoricalReproductionEventItem posCheck = new HistoricalReproductionEventItem(
                31L, GOAT_ID, FARM_A, ReproductiveEventType.PREGNANCY_CHECK, LocalDate.of(2026, 3, 1),
                null, null, null, null, null, null, PregnancyCheckResult.POSITIVE, "Diagnóstico positivo"
        );

        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(posCheck));
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(Collections.emptyList());

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().events()).hasSize(1);
        assertThat(result.get().events().get(0).id()).isEqualTo(31L);
        assertThat(result.get().events().get(0).checkResult()).isEqualTo(PregnancyCheckResult.POSITIVE);
        assertThat(result.get().events().get(0).pregnancyId()).isNull();
    }

    @Test
    @DisplayName("15. Coverage correction linked through relatedEventId")
    void test15_coverageCorrectionLinkedThroughRelatedEventId() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        HistoricalReproductionEventItem cov = new HistoricalReproductionEventItem(
                1L, GOAT_ID, FARM_A, ReproductiveEventType.COVERAGE, LocalDate.of(2026, 1, 10),
                BreedingType.NATURAL, "BOD-01", null, null, null, null, null, "Cobertura inicial"
        );
        HistoricalReproductionEventItem corr = new HistoricalReproductionEventItem(
                2L, GOAT_ID, FARM_A, ReproductiveEventType.COVERAGE_CORRECTION, LocalDate.of(2026, 1, 12),
                null, null, null, 1L, LocalDate.of(2026, 1, 11), null, null, "Correção de data"
        );

        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(cov, corr));
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(Collections.emptyList());

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().events()).hasSize(2);
        // Both facts preserved independently
        assertThat(result.get().events()).extracting(HistoricalReproductionEventItem::id).containsExactly(2L, 1L);
        HistoricalReproductionEventItem corrEvent = result.get().events().get(0);
        assertThat(corrEvent.eventType()).isEqualTo(ReproductiveEventType.COVERAGE_CORRECTION);
        assertThat(corrEvent.relatedEventId()).isEqualTo(1L);
        assertThat(corrEvent.correctedEventDate()).isEqualTo(LocalDate.of(2026, 1, 11));
    }

    @Test
    @DisplayName("16. Deterministic ordering: processes by breedingDate DESC nulls last, id DESC; events by eventDate DESC, id DESC")
    void test16_deterministicOrdering() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        HistoricalReproductionEventItem e1 = event(1L, FARM_A, ReproductiveEventType.COVERAGE, LocalDate.of(2025, 5, 1));
        HistoricalReproductionEventItem e2 = event(2L, FARM_A, ReproductiveEventType.COVERAGE, LocalDate.of(2026, 1, 1));
        HistoricalReproductionEventItem e3 = event(3L, FARM_A, ReproductiveEventType.WEANING, LocalDate.of(2026, 1, 1));

        HistoricalReproductionProcessCandidate p1 = process(1L, FARM_A, PregnancyStatus.ACTIVE, LocalDate.of(2025, 5, 1), null, null, null, null);
        HistoricalReproductionProcessCandidate p2 = process(2L, FARM_A, PregnancyStatus.ACTIVE, LocalDate.of(2026, 1, 1), null, null, null, null);
        HistoricalReproductionProcessCandidate p3 = process(3L, FARM_A, PregnancyStatus.ACTIVE, null, null, null, null, null);

        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(e1, e2, e3));
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(p1, p2, p3));

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        // Events ordered by date DESC, then id DESC: e3 (2026-01-01, id 3), e2 (2026-01-01, id 2), e1 (2025-05-01, id 1)
        assertThat(result.get().events()).extracting(HistoricalReproductionEventItem::id).containsExactly(3L, 2L, 1L);

        // Processes ordered by breedingDate DESC nulls last, then id DESC: p2 (2026-01-01), p1 (2025-05-01), p3 (null)
        assertThat(result.get().processes()).extracting(HistoricalReproductionProcessItem::pregnancyId).containsExactly(2L, 1L, 3L);
    }

    @Test
    @DisplayName("17. No synthetic process state: status is null when terminal transition is unknown/unexposed")
    void test17_noSyntheticProcessState_statusIsNullWhenTransitionUnexposed() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        // Former owner owns pregnancy, but it was closed after transfer without an unambiguous closure event for Farm A
        HistoricalReproductionProcessCandidate pregA = process(
                10L, FARM_A, PregnancyStatus.CLOSED, LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 3, 15), LocalDate.of(2026, 6, 1), PregnancyCloseReason.BIRTH, null
        );

        when(queryPort.findCandidateEventsByGoat(GOAT_ID, REG_NO)).thenReturn(Collections.emptyList());
        when(queryPort.findCandidateProcessesByGoat(GOAT_ID, REG_NO)).thenReturn(List.of(pregA));

        Optional<FarmGoatHistoricalReproductionSnapshot> result = business.findHistoricalReproduction(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        HistoricalReproductionProcessItem p = result.get().processes().get(0);
        assertThat(p.status()).isNull();
        assertThat(p.closedAt()).isNull();
        assertThat(p.closeReason()).isNull();
    }
}
