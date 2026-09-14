package com.devmaster.goatfarm.goatownership.business;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.GoatOwnershipLockState;
import com.devmaster.goatfarm.goatownership.application.model.InternalOwnershipTransferRequest;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatCurrentOwnerProjectionPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipLockPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipPeriodPersistencePort;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferPersistencePort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoatOwnershipTransferBusinessTest {
    private static final GoatId GOAT = GoatId.of(42L);
    private static final long SOURCE = 10L;
    private static final long TARGET = 20L;
    private static final long ACTOR = 7L;
    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant NOW = Instant.parse("2026-02-01T00:00:00Z");

    @Mock CurrentPrincipalQueryUseCase principalQuery;
    @Mock FarmAuthorizationUseCase authorization;
    @Mock GoatFarmPersistencePort farmPersistence;
    @Mock GoatOwnershipLockPort ownershipLock;
    @Mock GoatOwnershipPeriodPersistencePort periodPersistence;
    @Mock OwnershipTransferPersistencePort transferPersistence;
    @Mock GoatCurrentOwnerProjectionPort projection;

    private GoatOwnershipTransferBusiness business;

    @BeforeEach
    void setUp() {
        business = new GoatOwnershipTransferBusiness(principalQuery, authorization, farmPersistence,
                ownershipLock, periodPersistence, transferPersistence, projection,
                Clock.fixed(NOW, ZoneOffset.UTC));
        lenient().when(principalQuery.requireCurrent()).thenReturn(principal("ROLE_FARM_OWNER"));
        lenient().when(farmPersistence.findById(TARGET)).thenReturn(Optional.of(farm(TARGET)));
        lenient().when(transferPersistence.findGoatIdByTransferId(any(Long.class))).thenReturn(Optional.of(GOAT));
        lenient().when(transferPersistence.save(any(OwnershipTransfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void sourceOwnerCanRequestAndSourceComesFromCanonicalOpenPeriod() {
        var open = openPeriod(1L, SOURCE, START);
        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(new GoatOwnershipLockState(GOAT, Optional.of(open))));
        when(authorization.canAdministerFarm(SOURCE)).thenReturn(true);

        OwnershipTransfer result = business.requestInternalTransfer(
                new InternalOwnershipTransferRequest(GOAT, TARGET, "  move herd  ", " key-1 "));

        assertThat(result.status()).isEqualTo(OwnershipTransferStatus.REQUESTED);
        assertThat(result.sourceFarmId()).isEqualTo(SOURCE);
        assertThat(result.reason()).isEqualTo("move herd");
        assertThat(result.idempotencyKey()).isEqualTo("key-1");
        verify(transferPersistence).save(any(OwnershipTransfer.class));
    }

    @Test
    void operatorCannotRequestEvenWhenCanManageWouldAllowOperation() {
        when(principalQuery.requireCurrent()).thenReturn(principal("ROLE_OPERATOR"));
        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(
                new GoatOwnershipLockState(GOAT, Optional.of(openPeriod(1L, SOURCE, START)))));
        when(authorization.canAdministerFarm(SOURCE)).thenReturn(false);

        assertThatThrownBy(() -> business.requestInternalTransfer(
                new InternalOwnershipTransferRequest(GOAT, TARGET, "move", "key")))
                .isInstanceOf(AuthorizationDeniedException.class);
        verify(transferPersistence, never()).save(any());
    }

    @Test
    void requestRejectsMissingOpenPeriodAndSameSourceTarget() {
        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(
                new GoatOwnershipLockState(GOAT, Optional.empty())));
        assertThatThrownBy(() -> business.requestInternalTransfer(
                new InternalOwnershipTransferRequest(GOAT, TARGET, "move", "key")))
                .hasMessageContaining("no canonical open ownership");

        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(
                new GoatOwnershipLockState(GOAT, Optional.of(openPeriod(1L, SOURCE, START)))));
        assertThatThrownBy(() -> business.requestInternalTransfer(
                new InternalOwnershipTransferRequest(GOAT, SOURCE, "move", "key-2")))
                .hasMessageContaining("source and target farms must differ");
    }

    @Test
    void exactIdempotentRetryReturnsExistingTransferAndDifferentPayloadConflicts() {
        var existing = requestedTransfer(99L, "move", "same-key", SOURCE, TARGET);
        when(transferPersistence.findByRequesterAndIdempotencyKey(ACTOR, "same-key"))
                .thenReturn(Optional.of(existing));

        assertThat(business.requestInternalTransfer(
                new InternalOwnershipTransferRequest(GOAT, TARGET, " move ", "same-key")))
                .isSameAs(existing);
        assertThatThrownBy(() -> business.requestInternalTransfer(
                new InternalOwnershipTransferRequest(GoatId.of(43L), TARGET, "move", "same-key")))
                .hasMessageContaining("different transfer request");
        verify(ownershipLock, never()).lockGoatOwnership(any());
    }

    @Test
    void targetOwnerAcceptsAtomicallyAndMovesProjection() {
        var source = openPeriod(1L, SOURCE, START);
        var transfer = requestedTransfer(99L, "move", "accept-key", SOURCE, TARGET);
        when(transferPersistence.findById(99L)).thenReturn(Optional.of(transfer));
        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(new GoatOwnershipLockState(GOAT, Optional.of(source))));
        when(authorization.canAdministerFarm(TARGET)).thenReturn(true);
        when(projection.moveFromTo(GOAT, SOURCE, TARGET)).thenReturn(true);
        when(periodPersistence.findByGoatIdOrderByStartedAt(GOAT)).thenReturn(List.of(source));

        OwnershipTransfer completed = business.acceptTransfer(99L);

        assertThat(completed.status()).isEqualTo(OwnershipTransferStatus.COMPLETED);
        assertThat(completed.acceptedAt()).isEqualTo(NOW);
        assertThat(completed.effectiveAt()).isEqualTo(NOW);
        assertThat(completed.completedAt()).isEqualTo(NOW);
        ArgumentCaptor<GoatOwnershipPeriod> target = ArgumentCaptor.forClass(GoatOwnershipPeriod.class);
        verify(periodPersistence).handoff(any(GoatOwnershipPeriod.class), target.capture());
        assertThat(target.getValue().farmId()).isEqualTo(TARGET);
        assertThat(target.getValue().startedAt()).isEqualTo(NOW);
        verify(projection).moveFromTo(GOAT, SOURCE, TARGET);
        verify(transferPersistence).save(transfer);
    }

    @Test
    void projectionDriftFailsClosedBeforeCompletingTransfer() {
        var source = openPeriod(1L, SOURCE, START);
        var transfer = requestedTransfer(99L, "move", "drift-key", SOURCE, TARGET);
        when(transferPersistence.findById(99L)).thenReturn(Optional.of(transfer));
        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(new GoatOwnershipLockState(GOAT, Optional.of(source))));
        when(authorization.canAdministerFarm(TARGET)).thenReturn(true);
        when(periodPersistence.findByGoatIdOrderByStartedAt(GOAT)).thenReturn(List.of(source));
        when(projection.moveFromTo(GOAT, SOURCE, TARGET)).thenReturn(false);

        assertThatThrownBy(() -> business.acceptTransfer(99L))
                .hasMessageContaining("projection drift");
        verify(transferPersistence, never()).save(transfer);
    }

    @Test
    void operatorAndUnauthorizedTargetCannotAccept() {
        var transfer = requestedTransfer(99L, "move", "key", SOURCE, TARGET);
        when(transferPersistence.findById(99L)).thenReturn(Optional.of(transfer));
        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(
                new GoatOwnershipLockState(GOAT, Optional.of(openPeriod(1L, SOURCE, START)))));
        when(authorization.canAdministerFarm(TARGET)).thenReturn(false);

        assertThatThrownBy(() -> business.acceptTransfer(99L))
                .isInstanceOf(AuthorizationDeniedException.class);
        verify(periodPersistence, never()).handoff(any(), any());
    }

    @Test
    void rejectAndCancelDoNotChangeOwnership() {
        var reject = requestedTransfer(99L, "reject", "reject-key", SOURCE, TARGET);
        when(transferPersistence.findById(99L)).thenReturn(Optional.of(reject));
        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(
                new GoatOwnershipLockState(GOAT, Optional.of(openPeriod(1L, SOURCE, START)))));
        when(authorization.canAdministerFarm(TARGET)).thenReturn(true);
        assertThat(business.rejectTransfer(99L).status()).isEqualTo(OwnershipTransferStatus.REJECTED);
        verify(periodPersistence, never()).handoff(any(), any());
        verify(projection, never()).moveFromTo(any(), any(Long.class), any(Long.class));

        var cancel = requestedTransfer(100L, "cancel", "cancel-key", SOURCE, TARGET);
        when(transferPersistence.findById(100L)).thenReturn(Optional.of(cancel));
        when(authorization.canAdministerFarm(SOURCE)).thenReturn(true);
        assertThat(business.cancelTransfer(100L).status()).isEqualTo(OwnershipTransferStatus.CANCELLED);
    }

    @Test
    void completedRetryValidatesCanonicalTargetAndDoesNotCreateAnotherPeriod() {
        var completed = completedTransfer(99L);
        when(transferPersistence.findById(99L)).thenReturn(Optional.of(completed));
        when(ownershipLock.lockGoatOwnership(GOAT)).thenReturn(Optional.of(
                new GoatOwnershipLockState(GOAT, Optional.of(openPeriod(2L, TARGET, NOW)))));
        when(authorization.canAdministerFarm(TARGET)).thenReturn(true);
        when(periodPersistence.findByGoatIdOrderByStartedAt(GOAT)).thenReturn(List.of(
                closedPeriod(1L, SOURCE, START, NOW), openPeriod(2L, TARGET, NOW)));

        assertThat(business.acceptTransfer(99L)).isSameAs(completed);
        verify(periodPersistence, never()).handoff(any(), any());
        verify(projection, never()).moveFromTo(any(), any(Long.class), any(Long.class));
    }

    private AuthenticatedPrincipal principal(String role) {
        return new AuthenticatedPrincipal(ACTOR, "actor@example.com", "Actor", Set.of(role));
    }

    private FarmRecord farm(long id) {
        return new FarmRecord(id, "Farm " + id, "TOD" + id, null, null, null, List.of(), START, START, 0);
    }

    private GoatOwnershipPeriod openPeriod(long id, long farmId, Instant startedAt) {
        return GoatOwnershipPeriod.rehydrate(id, GOAT, farmId, startedAt, null,
                OwnershipEntryType.PURCHASE, null, "test");
    }

    private GoatOwnershipPeriod closedPeriod(long id, long farmId, Instant startedAt, Instant endedAt) {
        return GoatOwnershipPeriod.rehydrate(id, GOAT, farmId, startedAt, endedAt,
                OwnershipEntryType.PURCHASE, com.devmaster.goatfarm.goatownership.domain.OwnershipExitType.TRANSFER_OUT, "test");
    }

    private OwnershipTransfer requestedTransfer(long id, String reason, String key, long source, long target) {
        return OwnershipTransfer.rehydrate(id, GOAT, source, target, OwnershipTransferKind.INTERNAL_TRANSFER,
                OwnershipTransferStatus.REQUESTED, reason, key, START, null, null, null, null, ACTOR, null, null, null);
    }

    private OwnershipTransfer completedTransfer(long id) {
        return OwnershipTransfer.rehydrate(id, GOAT, SOURCE, TARGET, OwnershipTransferKind.INTERNAL_TRANSFER,
                OwnershipTransferStatus.COMPLETED, "move", "key-completed", START, NOW, NOW, NOW, null,
                ACTOR, ACTOR, ACTOR, null);
    }
}
