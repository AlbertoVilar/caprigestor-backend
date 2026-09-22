package com.devmaster.goatfarm.goatownership.domain;

import com.devmaster.goatfarm.goat.domain.GoatId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OwnershipTransferTest {
    private static final GoatId GOAT = new GoatId(10L);
    private static final Instant REQUESTED_AT = Instant.parse("2026-09-01T10:00:00Z");
    private static final Instant ACCEPTED_AT = Instant.parse("2026-09-02T10:00:00Z");

    @Test
    void requestStartsWithoutEffectiveAtUntilBuyerAcceptanceCompletesIt() {
        OwnershipTransfer transfer = OwnershipTransfer.request(
                GOAT, 1L, 2L, OwnershipTransferKind.INTERNAL_SALE,
                "sale", "request-1", REQUESTED_AT, 100L, 77L);

        assertEquals(OwnershipTransferStatus.REQUESTED, transfer.status());
        assertNull(transfer.effectiveAt());
        assertNull(transfer.completedAt());
        transfer.acceptAndComplete(ACCEPTED_AT, ACCEPTED_AT, 200L, ACCEPTED_AT);

        assertEquals(OwnershipTransferStatus.COMPLETED, transfer.status());
        assertEquals(ACCEPTED_AT, transfer.effectiveAt());
        assertEquals(200L, transfer.acceptedBy());
        assertEquals(200L, transfer.completedBy());
        assertTrue(transfer.isTerminal());
    }

    @Test
    void internalSaleCanCompleteDirectlyFromPaymentWithoutBuyerAcceptance() {
        OwnershipTransfer transfer = OwnershipTransfer.request(
                GOAT, 1L, 2L, OwnershipTransferKind.INTERNAL_SALE,
                "sale", "request-payment-1", REQUESTED_AT, 100L, 77L);

        transfer.completeFromPayment(ACCEPTED_AT, 100L, ACCEPTED_AT);

        assertEquals(OwnershipTransferStatus.COMPLETED, transfer.status());
        assertNull(transfer.acceptedAt());
        assertNull(transfer.acceptedBy());
        assertEquals(ACCEPTED_AT, transfer.effectiveAt());
        assertEquals(100L, transfer.completedBy());
    }

    @Test
    void legacyCompletedInternalSaleWithAcceptanceStillRehydrates() {
        OwnershipTransfer transfer = OwnershipTransfer.rehydrate(
                42L, GOAT, 1L, 2L, OwnershipTransferKind.INTERNAL_SALE,
                OwnershipTransferStatus.COMPLETED, "sale", "legacy-sale", REQUESTED_AT,
                ACCEPTED_AT, ACCEPTED_AT, ACCEPTED_AT, null, 100L, 200L, 200L, 77L);

        assertEquals(OwnershipTransferStatus.COMPLETED, transfer.status());
        assertEquals(200L, transfer.acceptedBy());
    }

    @Test
    void normalAcceptanceCannotBeRepeatedAfterCompletion() {
        OwnershipTransfer transfer = OwnershipTransfer.request(
                GOAT, 1L, 2L, OwnershipTransferKind.INTERNAL_TRANSFER,
                "relocation", "request-2", REQUESTED_AT, 100L, null);
        transfer.acceptAndComplete(ACCEPTED_AT, ACCEPTED_AT, 200L, ACCEPTED_AT);

        assertThrows(IllegalStateException.class,
                () -> transfer.acceptAndComplete(ACCEPTED_AT.plusSeconds(1), ACCEPTED_AT.plusSeconds(1), 201L, ACCEPTED_AT.plusSeconds(1)));
    }

    @Test
    void acceptanceRejectsBackdatedTimestampsAndMismatchedEffectiveTime() {
        OwnershipTransfer transfer = OwnershipTransfer.request(
                GOAT, 1L, 2L, OwnershipTransferKind.INTERNAL_TRANSFER,
                "relocation", "request-backdate", REQUESTED_AT, 100L, null);

        assertThrows(IllegalArgumentException.class,
                () -> transfer.acceptAndComplete(REQUESTED_AT.minusSeconds(1), ACCEPTED_AT, 200L, ACCEPTED_AT));
        assertThrows(IllegalArgumentException.class,
                () -> transfer.acceptAndComplete(ACCEPTED_AT, ACCEPTED_AT, 200L, ACCEPTED_AT.plusSeconds(1)));
    }

    @Test
    void rejectionAndCancellationAreTerminalWithoutChangingPeriods() {
        OwnershipTransfer rejected = OwnershipTransfer.request(
                GOAT, 1L, 2L, OwnershipTransferKind.INTERNAL_TRANSFER,
                "relocation", "request-3", REQUESTED_AT, 100L, null);
        rejected.reject();
        assertEquals(OwnershipTransferStatus.REJECTED, rejected.status());
        assertTrue(rejected.isTerminal());

        OwnershipTransfer cancelled = OwnershipTransfer.request(
                GOAT, 1L, 2L, OwnershipTransferKind.RETURN,
                "return", "request-4", REQUESTED_AT, 100L, null);
        cancelled.cancel(ACCEPTED_AT);
        assertEquals(OwnershipTransferStatus.CANCELLED, cancelled.status());
        assertTrue(cancelled.isTerminal());
    }

    @Test
    void externalClaimHasNoSourceFarmButStillTargetsAnInternalFarm() {
        OwnershipTransfer claim = OwnershipTransfer.request(
                GOAT, null, 2L, OwnershipTransferKind.EXTERNAL_CLAIM,
                "evidence", "claim-1", REQUESTED_AT, 100L, null);

        assertNull(claim.sourceFarmId());
        assertEquals(2L, claim.targetFarmId());
    }

    @Test
    void internalTransferRequiresDistinctFarmsAndExternalClaimRequiresNullSource() {
        assertThrows(IllegalArgumentException.class, () -> OwnershipTransfer.request(
                GOAT, 1L, 1L, OwnershipTransferKind.INTERNAL_TRANSFER,
                "bad", "request-5", REQUESTED_AT, 100L, null));
        assertThrows(IllegalArgumentException.class, () -> OwnershipTransfer.request(
                GOAT, 1L, 2L, OwnershipTransferKind.EXTERNAL_CLAIM,
                "bad", "request-6", REQUESTED_AT, 100L, null));
    }

    @Test
    void completedRehydrationRequiresEffectiveTimestamp() {
        assertThrows(IllegalArgumentException.class, () -> OwnershipTransfer.rehydrate(
                1L, GOAT, 1L, 2L, OwnershipTransferKind.INTERNAL_TRANSFER,
                OwnershipTransferStatus.COMPLETED, "relocation", "request-7", REQUESTED_AT,
                ACCEPTED_AT, null, ACCEPTED_AT, null, 100L, 200L, 200L, null));
    }

    @Test
    void rehydrationRequiresStateConsistentLifecycleFields() {
        assertThrows(IllegalArgumentException.class, () -> OwnershipTransfer.rehydrate(
                1L, GOAT, 1L, 2L, OwnershipTransferKind.INTERNAL_TRANSFER,
                OwnershipTransferStatus.ACCEPTED, "relocation", "request-8", REQUESTED_AT,
                null, null, null, null, 100L, 200L, null, null));
        assertThrows(IllegalArgumentException.class, () -> OwnershipTransfer.rehydrate(
                1L, GOAT, 1L, 2L, OwnershipTransferKind.INTERNAL_TRANSFER,
                OwnershipTransferStatus.REQUESTED, "relocation", "request-9", REQUESTED_AT,
                ACCEPTED_AT, null, null, null, 100L, 200L, null, null));
        assertThrows(IllegalArgumentException.class, () -> OwnershipTransfer.rehydrate(
                1L, GOAT, 1L, 2L, OwnershipTransferKind.INTERNAL_TRANSFER,
                OwnershipTransferStatus.CANCELLED, "relocation", "request-10", REQUESTED_AT,
                null, null, null, null, 100L, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> OwnershipTransfer.rehydrate(
                1L, GOAT, 1L, 2L, OwnershipTransferKind.INTERNAL_TRANSFER,
                OwnershipTransferStatus.CANCELLED, "relocation", "request-11", REQUESTED_AT,
                ACCEPTED_AT, null, null, ACCEPTED_AT, 100L, null, null, null));
    }
}
