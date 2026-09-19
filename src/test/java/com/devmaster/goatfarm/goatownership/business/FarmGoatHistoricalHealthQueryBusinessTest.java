package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.application.model.*;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatRegistryQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.out.FarmGoatHistoricalHealthQueryPort;
import com.devmaster.goatfarm.health.domain.enums.AdministrationRoute;
import com.devmaster.goatfarm.health.domain.enums.DoseUnit;
import com.devmaster.goatfarm.health.domain.enums.HealthEventStatus;
import com.devmaster.goatfarm.health.domain.enums.HealthEventType;
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
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmGoatHistoricalHealthQueryBusinessTest {

    @Mock
    private FarmGoatRegistryQueryUseCase registryQueryUseCase;

    @Mock
    private FarmGoatHistoricalHealthQueryPort queryPort;

    private FarmGoatHistoricalHealthQueryBusiness business;

    private static final Long FARM_A = 1L;
    private static final Long FARM_B = 2L;
    private static final Long FARM_UNRELATED = 99L;
    private static final GoatId GOAT_ID = GoatId.of(100L);
    private static final GoatId OTHER_GOAT_ID = GoatId.of(200L);
    private static final String REG_NO = "RG-100";

    private FarmGoatRegistryItem defaultRegistryItemA;
    private FarmGoatRegistryItem defaultRegistryItemB;

    @BeforeEach
    void setUp() {
        business = new FarmGoatHistoricalHealthQueryBusiness(registryQueryUseCase, queryPort);

        defaultRegistryItemA = new FarmGoatRegistryItem(
                GOAT_ID, REG_NO, "Estrela", GoatStatus.ATIVO, FARM_A, "Capril A",
                Set.of(FarmGoatRegistryRole.FORMER_OWNER), FarmGoatRegistryDisposition.TRANSFERRED, FARM_A
        );

        defaultRegistryItemB = new FarmGoatRegistryItem(
                GOAT_ID, REG_NO, "Estrela", GoatStatus.ATIVO, FARM_B, "Capril B",
                Set.of(FarmGoatRegistryRole.CURRENT_OWNER), FarmGoatRegistryDisposition.CURRENT, FARM_B
        );
    }

    private HistoricalHealthEventItem event(Long id, GoatId goatId, Long farmId, HealthEventType type,
                                           HealthEventStatus status, LocalDate scheduledDate,
                                           LocalDateTime performedAt, Integer withdrawalMilkDays) {
        return new HistoricalHealthEventItem(
                id,
                goatId,
                farmId,
                type,
                status,
                "Evento " + type + " #" + id,
                "Descricao " + id,
                scheduledDate,
                performedAt,
                "Dr. Veterinario",
                "Notas clinicas",
                "Produto X",
                "Principio Ativo Y",
                new BigDecimal("5.0"),
                DoseUnit.ML,
                AdministrationRoute.IM,
                "LOTE-123",
                withdrawalMilkDays,
                withdrawalMilkDays != null ? withdrawalMilkDays * 2 : null
        );
    }

    @Test
    @DisplayName("Current farm owner sees its own health history with all statuses preserved")
    void currentFarmOwnerSeesOwnHealthHistory() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_B, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemB));

        HistoricalHealthEventItem e1 = event(1L, GOAT_ID, FARM_B, HealthEventType.VACINA,
                HealthEventStatus.REALIZADO, LocalDate.of(2026, 4, 1),
                LocalDateTime.of(2026, 4, 1, 10, 0), 3);
        HistoricalHealthEventItem e2 = event(2L, GOAT_ID, FARM_B, HealthEventType.VERMIFUGACAO,
                HealthEventStatus.AGENDADO, LocalDate.of(2026, 4, 15), null, null);
        HistoricalHealthEventItem e3 = event(3L, GOAT_ID, FARM_B, HealthEventType.MEDICACAO,
                HealthEventStatus.CANCELADO, LocalDate.of(2026, 3, 20), null, null);

        when(queryPort.findHistoricalHealthEvents(GOAT_ID, FARM_B)).thenReturn(List.of(e1, e2, e3));

        Optional<FarmGoatHistoricalHealthSnapshot> result = business.findHistoricalHealth(FARM_B, GOAT_ID);

        assertThat(result).isPresent();
        FarmGoatHistoricalHealthSnapshot snapshot = result.get();
        assertThat(snapshot.goatId()).isEqualTo(GOAT_ID);
        assertThat(snapshot.events()).hasSize(3);

        // Deterministic ordering: scheduledDate DESC, id DESC
        assertThat(snapshot.events().get(0).id()).isEqualTo(2L); // 2026-04-15
        assertThat(snapshot.events().get(1).id()).isEqualTo(1L); // 2026-04-01
        assertThat(snapshot.events().get(2).id()).isEqualTo(3L); // 2026-03-20

        // Statuses are preserved without synthesis
        assertThat(snapshot.events().get(0).status()).isEqualTo(HealthEventStatus.AGENDADO);
        assertThat(snapshot.events().get(1).status()).isEqualTo(HealthEventStatus.REALIZADO);
        assertThat(snapshot.events().get(2).status()).isEqualTo(HealthEventStatus.CANCELADO);
    }

    @Test
    @DisplayName("Former farm owner sees its own historical health events after transfer")
    void formerFarmOwnerSeesOwnHistoricalHealthEvents() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        HistoricalHealthEventItem eA1 = event(10L, GOAT_ID, FARM_A, HealthEventType.VACINA,
                HealthEventStatus.REALIZADO, LocalDate.of(2026, 1, 15),
                LocalDateTime.of(2026, 1, 15, 9, 0), 5);
        HistoricalHealthEventItem eA2 = event(11L, GOAT_ID, FARM_A, HealthEventType.DOENCA,
                HealthEventStatus.REALIZADO, LocalDate.of(2026, 2, 1),
                LocalDateTime.of(2026, 2, 1, 14, 0), 0);

        when(queryPort.findHistoricalHealthEvents(GOAT_ID, FARM_A)).thenReturn(List.of(eA1, eA2));

        Optional<FarmGoatHistoricalHealthSnapshot> result = business.findHistoricalHealth(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().events()).extracting(HistoricalHealthEventItem::id).containsExactly(11L, 10L);
    }

    @Test
    @DisplayName("Farm A does not see Farm B records after transfer (no leakage A <- B)")
    void farmADoesNotSeeFarmBRecordsAfterTransfer() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        HistoricalHealthEventItem eA = event(10L, GOAT_ID, FARM_A, HealthEventType.VACINA,
                HealthEventStatus.REALIZADO, LocalDate.of(2026, 1, 15), null, null);
        HistoricalHealthEventItem eB = event(20L, GOAT_ID, FARM_B, HealthEventType.MEDICACAO,
                HealthEventStatus.REALIZADO, LocalDate.of(2026, 5, 1), null, null);

        // QueryPort should return only FARM_A events, but business also defends against foreign farmId
        when(queryPort.findHistoricalHealthEvents(GOAT_ID, FARM_A)).thenReturn(List.of(eA, eB));

        Optional<FarmGoatHistoricalHealthSnapshot> result = business.findHistoricalHealth(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().events()).hasSize(1);
        assertThat(result.get().events().get(0).id()).isEqualTo(10L);
        assertThat(result.get().events().get(0).farmId()).isEqualTo(FARM_A);
    }

    @Test
    @DisplayName("Farm B does not see Farm A records before transfer (no leakage B <- A)")
    void farmBDoesNotSeeFarmARecordsBeforeTransfer() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_B, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemB));

        HistoricalHealthEventItem eA = event(10L, GOAT_ID, FARM_A, HealthEventType.VACINA,
                HealthEventStatus.REALIZADO, LocalDate.of(2026, 1, 15), null, null);
        HistoricalHealthEventItem eB = event(20L, GOAT_ID, FARM_B, HealthEventType.MEDICACAO,
                HealthEventStatus.REALIZADO, LocalDate.of(2026, 5, 1), null, null);

        when(queryPort.findHistoricalHealthEvents(GOAT_ID, FARM_B)).thenReturn(List.of(eA, eB));

        Optional<FarmGoatHistoricalHealthSnapshot> result = business.findHistoricalHealth(FARM_B, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().events()).hasSize(1);
        assertThat(result.get().events().get(0).id()).isEqualTo(20L);
        assertThat(result.get().events().get(0).farmId()).isEqualTo(FARM_B);
    }

    @Test
    @DisplayName("Registry-unrelated farm returns Optional.empty()")
    void registryUnrelatedFarmReturnsEmpty() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_UNRELATED, GOAT_ID)).thenReturn(Optional.empty());

        Optional<FarmGoatHistoricalHealthSnapshot> result = business.findHistoricalHealth(FARM_UNRELATED, GOAT_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Valid historical member with no health events returns empty snapshot")
    void validMemberWithNoHealthEventsReturnsEmptySnapshot() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));
        when(queryPort.findHistoricalHealthEvents(GOAT_ID, FARM_A)).thenReturn(List.of());

        Optional<FarmGoatHistoricalHealthSnapshot> result = business.findHistoricalHealth(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().goatId()).isEqualTo(GOAT_ID);
        assertThat(result.get().events()).isEmpty();
    }

    @Test
    @DisplayName("Deterministic ordering: scheduledDate DESC, then id DESC")
    void deterministicOrderingByScheduledDateAndId() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        LocalDate sameDate = LocalDate.of(2026, 2, 1);
        HistoricalHealthEventItem e1 = event(1L, GOAT_ID, FARM_A, HealthEventType.VACINA,
                HealthEventStatus.REALIZADO, sameDate, null, null);
        HistoricalHealthEventItem e2 = event(5L, GOAT_ID, FARM_A, HealthEventType.VERMIFUGACAO,
                HealthEventStatus.REALIZADO, sameDate, null, null);
        HistoricalHealthEventItem e3 = event(3L, GOAT_ID, FARM_A, HealthEventType.PROCEDIMENTO,
                HealthEventStatus.REALIZADO, sameDate.plusDays(5), null, null);
        HistoricalHealthEventItem e4 = event(2L, GOAT_ID, FARM_A, HealthEventType.MEDICACAO,
                HealthEventStatus.REALIZADO, sameDate.minusDays(5), null, null);

        when(queryPort.findHistoricalHealthEvents(GOAT_ID, FARM_A)).thenReturn(List.of(e1, e2, e3, e4));

        Optional<FarmGoatHistoricalHealthSnapshot> result = business.findHistoricalHealth(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        // Expected order: e3 (plusDays 5), e2 (sameDate id=5), e1 (sameDate id=1), e4 (minusDays 5)
        assertThat(result.get().events()).extracting(HistoricalHealthEventItem::id)
                .containsExactly(3L, 5L, 1L, 2L);
    }

    @Test
    @DisplayName("GoatId structural identity preserved and withdrawal calculations correct")
    void goatIdStructuralIdentityAndWithdrawalCalculations() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID)).thenReturn(Optional.of(defaultRegistryItemA));

        LocalDateTime performedAt = LocalDateTime.of(2026, 3, 1, 8, 30);
        HistoricalHealthEventItem e = event(100L, GOAT_ID, FARM_A, HealthEventType.MEDICACAO,
                HealthEventStatus.REALIZADO, LocalDate.of(2026, 3, 1), performedAt, 7);

        when(queryPort.findHistoricalHealthEvents(GOAT_ID, FARM_A)).thenReturn(List.of(e));

        Optional<FarmGoatHistoricalHealthSnapshot> result = business.findHistoricalHealth(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        HistoricalHealthEventItem item = result.get().events().get(0);
        assertThat(item.goatId()).isEqualTo(GOAT_ID);
        assertThat(item.withdrawalMilkDays()).isEqualTo(7);
        assertThat(item.withdrawalMeatDays()).isEqualTo(14);
        assertThat(item.milkWithdrawalEndDate()).isEqualTo(LocalDate.of(2026, 3, 8));
        assertThat(item.meatWithdrawalEndDate()).isEqualTo(LocalDate.of(2026, 3, 15));
    }

    @Test
    @DisplayName("Fail-closed: events returned with different GoatId are filtered out")
    void excludesEventsWithDifferentGoatIdEvenIfSameFarm() {
        when(registryQueryUseCase.findForFarmAndGoat(FARM_A, GOAT_ID))
                .thenReturn(Optional.of(defaultRegistryItemA));

        HistoricalHealthEventItem validEvent = event(1L, GOAT_ID, FARM_A, HealthEventType.VACINA,
                HealthEventStatus.REALIZADO, LocalDate.of(2026, 3, 1), null, null);
        HistoricalHealthEventItem wrongGoatEvent = event(2L, OTHER_GOAT_ID, FARM_A, HealthEventType.MEDICACAO,
                HealthEventStatus.REALIZADO, LocalDate.of(2026, 3, 2), null, null);

        when(queryPort.findHistoricalHealthEvents(GOAT_ID, FARM_A))
                .thenReturn(List.of(validEvent, wrongGoatEvent));

        Optional<FarmGoatHistoricalHealthSnapshot> result = business.findHistoricalHealth(FARM_A, GOAT_ID);

        assertThat(result).isPresent();
        assertThat(result.get().events()).hasSize(1);
        assertThat(result.get().events().get(0).id()).isEqualTo(1L);
        assertThat(result.get().events().get(0).goatId()).isEqualTo(GOAT_ID);
    }

    @Test
    @DisplayName("Invalid farmId throws IllegalArgumentException")
    void invalidFarmIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> business.findHistoricalHealth(0L, GOAT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("farmId must be positive");

        assertThatThrownBy(() -> business.findHistoricalHealth(-1L, GOAT_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("farmId must be positive");
    }

    @Test
    @DisplayName("Null GoatId throws IllegalArgumentException")
    void nullGoatIdThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> business.findHistoricalHealth(FARM_A, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("goatId must not be null");
    }
}
