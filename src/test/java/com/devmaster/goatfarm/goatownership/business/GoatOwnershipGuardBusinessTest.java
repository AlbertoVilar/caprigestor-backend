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
import java.util.List;

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

    private GoatOwnershipPeriod period(Long id, long farmId, String startedAt, String endedAt,
                                       OwnershipEntryType entryType, OwnershipExitType exitType) {
        return GoatOwnershipPeriod.rehydrate(id, GOAT_ID, farmId,
                Instant.parse(startedAt), endedAt == null ? null : Instant.parse(endedAt),
                entryType, exitType, "TEST");
    }
}
