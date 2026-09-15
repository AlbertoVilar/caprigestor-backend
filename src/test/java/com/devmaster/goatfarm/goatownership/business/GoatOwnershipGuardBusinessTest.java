package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipQueryPort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoatOwnershipGuardBusinessTest {

    private static final GoatId GOAT_ID = GoatId.of(7L);

    @Mock private GoatOwnershipQueryPort ownershipQuery;

    private GoatOwnershipGuardBusiness guard;

    @BeforeEach
    void setUp() {
        guard = new GoatOwnershipGuardBusiness(ownershipQuery);
    }

    @Test
    void currentFarmRequiresExactlyAnOpenPeriodInExpectedFarm() {
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                period(1L, 10L, "2026-01-01T00:00:00Z", null, OwnershipEntryType.MANUAL_IMPORT, null)));

        guard.requireCurrentFarm(GOAT_ID, 10L);

        assertThatThrownBy(() -> guard.requireCurrentFarm(GOAT_ID, 11L))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void currentFarmDeniesTerminalHistory() {
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                period(1L, 10L, "2026-01-01T00:00:00Z", "2026-02-01T00:00:00Z",
                        OwnershipEntryType.MANUAL_IMPORT, OwnershipExitType.DEATH)));

        assertThatThrownBy(() -> guard.requireCurrentFarm(GOAT_ID, 10L))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void lastAssociatedFarmAllowsClosedLastPeriodAndIsDeterministic() {
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                period(1L, 10L, "2026-01-01T00:00:00Z", "2026-02-01T00:00:00Z",
                        OwnershipEntryType.MANUAL_IMPORT, OwnershipExitType.TRANSFER_OUT),
                period(2L, 20L, "2026-02-01T00:00:00Z", "2026-03-01T00:00:00Z",
                        OwnershipEntryType.TRANSFER_IN, OwnershipExitType.DEATH)));

        guard.requireLastAssociatedFarm(GOAT_ID, 20L);

        assertThatThrownBy(() -> guard.requireLastAssociatedFarm(GOAT_ID, 10L))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void emptyOrInconsistentHistoryFailsClosed() {
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of());
        assertThatThrownBy(() -> guard.requireLastAssociatedFarm(GOAT_ID, 10L))
                .isInstanceOf(BusinessRuleException.class);

        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                period(1L, 10L, "2026-01-01T00:00:00Z", null, OwnershipEntryType.MANUAL_IMPORT, null),
                period(2L, 20L, "2026-01-15T00:00:00Z", null, OwnershipEntryType.MANUAL_IMPORT, null)));
        assertThatThrownBy(() -> guard.requireCurrentFarm(GOAT_ID, 10L))
                .isInstanceOf(BusinessRuleException.class);
        verify(ownershipQuery, org.mockito.Mockito.times(2)).findOwnershipHistory(GOAT_ID);
    }

    @Test
    void mixedGoatHistoryFailsClosedAtTheOwnershipBoundary() {
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                period(1L, 10L, "2026-01-01T00:00:00Z", null, OwnershipEntryType.MANUAL_IMPORT, null),
                GoatOwnershipPeriod.rehydrate(2L, GoatId.of(20L), 20L,
                        Instant.parse("2026-02-01T00:00:00Z"), null,
                        OwnershipEntryType.MANUAL_IMPORT, null, "TEST")));

        assertThatThrownBy(() -> guard.requireCurrentFarm(GOAT_ID, 10L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("outro GoatId");
    }

    @Test
    void unambiguousDateRequiresOnePeriodToCoverTheCompleteCivilDay() {
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        Instant dayStart = LocalDate.of(2026, 9, 9).atStartOfDay(zone).toInstant();
        Instant nextDayStart = LocalDate.of(2026, 9, 10).atStartOfDay(zone).toInstant();
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                period(1L, 10L, dayStart.minus(2, ChronoUnit.DAYS), nextDayStart,
                        OwnershipEntryType.MANUAL_IMPORT, OwnershipExitType.TRANSFER_OUT)));

        assertThatCode(() -> guard.requireUnambiguousOwnershipOnDate(
                GOAT_ID, 10L, LocalDate.of(2026, 9, 9))).doesNotThrowAnyException();
    }

    @Test
    void transferDayAndPartialEntryFailClosed() {
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        Instant transfer = LocalDate.of(2026, 9, 10).atStartOfDay(zone).toInstant().plus(14, ChronoUnit.HOURS);
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                period(1L, 10L, LocalDate.of(2026, 9, 1).atStartOfDay(zone).toInstant(), transfer,
                        OwnershipEntryType.MANUAL_IMPORT, OwnershipExitType.TRANSFER_OUT),
                period(2L, 20L, transfer, null,
                        OwnershipEntryType.TRANSFER_IN, null)));

        assertThatThrownBy(() -> guard.requireUnambiguousOwnershipOnDate(
                GOAT_ID, 10L, LocalDate.of(2026, 9, 10)))
                .isInstanceOf(AuthorizationDeniedException.class);
        assertThatThrownBy(() -> guard.requireUnambiguousOwnershipOnDate(
                GOAT_ID, 20L, LocalDate.of(2026, 9, 10)))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    @Test
    void exactCivilDayBoundariesAreAccepted() {
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        LocalDate date = LocalDate.of(2026, 9, 11);
        Instant dayStart = date.atStartOfDay(zone).toInstant();
        Instant nextDayStart = date.plusDays(1).atStartOfDay(zone).toInstant();
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                period(1L, 10L, dayStart, nextDayStart,
                        OwnershipEntryType.MANUAL_IMPORT, OwnershipExitType.TRANSFER_OUT)));

        assertThatCode(() -> guard.requireUnambiguousOwnershipOnDate(GOAT_ID, 10L, date))
                .doesNotThrowAnyException();
    }

    @Test
    void ownershipGapFailsClosedForDatePolicy() {
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        LocalDate date = LocalDate.of(2026, 9, 12);
        Instant dayStart = date.atStartOfDay(zone).toInstant();
        when(ownershipQuery.findOwnershipHistory(GOAT_ID)).thenReturn(List.of(
                period(1L, 10L, dayStart.minus(2, ChronoUnit.DAYS), dayStart.minus(1, ChronoUnit.HOURS),
                        OwnershipEntryType.MANUAL_IMPORT, OwnershipExitType.TRANSFER_OUT),
                period(2L, 10L, dayStart.plus(1, ChronoUnit.HOURS), null,
                        OwnershipEntryType.TRANSFER_IN, null)));

        assertThatThrownBy(() -> guard.requireUnambiguousOwnershipOnDate(GOAT_ID, 10L, date))
                .isInstanceOf(AuthorizationDeniedException.class);
    }

    private GoatOwnershipPeriod period(Long id, long farmId, String startedAt, String endedAt,
                                       OwnershipEntryType entryType, OwnershipExitType exitType) {
        return GoatOwnershipPeriod.rehydrate(id, GOAT_ID, farmId,
                Instant.parse(startedAt), endedAt == null ? null : Instant.parse(endedAt),
                entryType, exitType, "TEST");
    }

    private GoatOwnershipPeriod period(Long id, long farmId, Instant startedAt, Instant endedAt,
                                       OwnershipEntryType entryType, OwnershipExitType exitType) {
        return GoatOwnershipPeriod.rehydrate(id, GOAT_ID, farmId, startedAt, endedAt,
                entryType, exitType, "TEST");
    }
}
