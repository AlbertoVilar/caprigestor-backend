package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.GoatOwnershipLockState;
import com.devmaster.goatfarm.goatownership.application.model.TerminalOwnershipExitCommand;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipLockPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipPeriodPersistencePort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipQueryPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferPersistencePort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class GoatOwnershipExitBusinessTest {
    private static final GoatId GOAT = GoatId.of(42L);
    private static final long SOURCE = 10L;
    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant NOW = Instant.parse("2026-02-01T00:00:00Z");

    @Mock FarmAuthorizationUseCase authorization;
    @Mock GoatOwnershipLockPort ownershipLock;
    @Mock GoatOwnershipPeriodPersistencePort periodPersistence;
    @Mock GoatOwnershipQueryPort ownershipQuery;
    @Mock OwnershipTransferPersistencePort transferPersistence;

    private GoatOwnershipExitBusiness business;

    @BeforeEach
    void setUp() {
        business = new GoatOwnershipExitBusiness(authorization, ownershipLock, periodPersistence,
                ownershipQuery, transferPersistence, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void closesOpenPeriodForEverySupportedTerminalExit() {
        for (OwnershipExitType exitType : new OwnershipExitType[]{
                OwnershipExitType.EXTERNAL_SALE,
                OwnershipExitType.DONATION,
                OwnershipExitType.DEATH,
                OwnershipExitType.RETIREMENT}) {
            GoatOwnershipPeriod open = openPeriod();
            stubSuccessfulFlow(open);

            GoatOwnershipPeriod closed = business.closeTerminalOwnership(
                    new TerminalOwnershipExitCommand(GOAT, SOURCE, exitType));

            assertThat(closed.isOpen()).isFalse();
            assertThat(closed.endedAt()).isEqualTo(NOW);
            assertThat(closed.exitType()).isEqualTo(exitType);
        }
    }

    @Test
    void rejectsTransferOutAndInvalidInput() {
        assertThatThrownBy(() -> business.closeTerminalOwnership(
                new TerminalOwnershipExitCommand(GOAT, SOURCE, OwnershipExitType.TRANSFER_OUT)))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> business.closeTerminalOwnership(null))
                .isInstanceOf(InvalidArgumentException.class);
        assertThatThrownBy(() -> business.closeTerminalOwnership(
                new TerminalOwnershipExitCommand(null, SOURCE, OwnershipExitType.DEATH)))
                .isInstanceOf(InvalidArgumentException.class);
        assertThatThrownBy(() -> business.closeTerminalOwnership(
                new TerminalOwnershipExitCommand(GOAT, 0L, OwnershipExitType.DEATH)))
                .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    void rejectsCanonicalSourceMismatchAndUnauthorizedPrincipal() {
        GoatOwnershipPeriod open = openPeriod();
        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(
                new GoatOwnershipLockState(GOAT, Optional.of(open))));
        assertThatThrownBy(() -> business.closeTerminalOwnership(
                new TerminalOwnershipExitCommand(GOAT, SOURCE + 1, OwnershipExitType.DEATH)))
                .isInstanceOf(AuthorizationDeniedException.class);

        when(authorization.canAdministerFarm(SOURCE)).thenReturn(false);
        assertThatThrownBy(() -> business.closeTerminalOwnership(
                new TerminalOwnershipExitCommand(GOAT, SOURCE, OwnershipExitType.DEATH)))
                .isInstanceOf(AuthorizationDeniedException.class);
        verify(periodPersistence, never()).save(any());
    }

    @Test
    void rejectsMissingOpenPeriodAndPendingTransfer() {
        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(
                new GoatOwnershipLockState(GOAT, Optional.empty())));
        assertThatThrownBy(() -> business.closeTerminalOwnership(
                new TerminalOwnershipExitCommand(GOAT, SOURCE, OwnershipExitType.DEATH)))
                .hasMessageContaining("no canonical open ownership period");

        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(
                new GoatOwnershipLockState(GOAT, Optional.of(openPeriod()))));
        when(authorization.canAdministerFarm(SOURCE)).thenReturn(true);
        when(transferPersistence.findPendingByGoatId(GOAT)).thenReturn(Optional.of(
                com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer.request(
                        GOAT, SOURCE, 20L,
                        com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind.INTERNAL_TRANSFER,
                        "pending", "key", START, 7L, null)));
        assertThatThrownBy(() -> business.closeTerminalOwnership(
                new TerminalOwnershipExitCommand(GOAT, SOURCE, OwnershipExitType.DEATH)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("pending");
        verify(periodPersistence, never()).save(any());
    }

    @Test
    void usesInjectedClockAndFailsWhenEffectiveTimeIsNotAfterPeriodStart() {
        GoatOwnershipPeriod open = GoatOwnershipPeriod.rehydrate(1L, GOAT, SOURCE, NOW, null,
                OwnershipEntryType.PURCHASE, null, "test");
        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(
                new GoatOwnershipLockState(GOAT, Optional.of(open))));
        when(authorization.canAdministerFarm(SOURCE)).thenReturn(true);
        when(transferPersistence.findPendingByGoatId(GOAT)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> business.closeTerminalOwnership(
                new TerminalOwnershipExitCommand(GOAT, SOURCE, OwnershipExitType.DEATH)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("effectiveAt");
        verify(periodPersistence, never()).save(any());
    }

    @Test
    void closesOnlyTheExistingPeriodChecksFullHistoryAndDoesNotUseProjection() {
        GoatOwnershipPeriod open = openPeriod();
        stubSuccessfulFlow(open);

        GoatOwnershipPeriod closed = business.closeTerminalOwnership(
                new TerminalOwnershipExitCommand(GOAT, SOURCE, OwnershipExitType.RETIREMENT));

        assertThat(closed.endedAt()).isEqualTo(NOW);
        assertThat(closed.exitType()).isEqualTo(OwnershipExitType.RETIREMENT);
        verify(ownershipQuery).findOwnershipHistory(GOAT);
        verify(periodPersistence).save(closed);
        InOrder order = inOrder(ownershipLock, authorization, transferPersistence);
        order.verify(ownershipLock).lockGoatOwnership(GOAT);
        order.verify(authorization).canAdministerFarm(SOURCE);
        order.verify(transferPersistence).findPendingByGoatId(GOAT);
    }

    private GoatOwnershipPeriod openPeriod() {
        return GoatOwnershipPeriod.rehydrate(1L, GOAT, SOURCE, START, null,
                OwnershipEntryType.PURCHASE, null, "test");
    }

    private void stubSuccessfulFlow(GoatOwnershipPeriod open) {
        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(
                new GoatOwnershipLockState(GOAT, Optional.of(open))));
        when(authorization.canAdministerFarm(SOURCE)).thenReturn(true);
        when(transferPersistence.findPendingByGoatId(GOAT)).thenReturn(Optional.empty());
        when(ownershipQuery.findOwnershipHistory(GOAT)).thenReturn(List.of(open));
        when(periodPersistence.save(any(GoatOwnershipPeriod.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }
}
