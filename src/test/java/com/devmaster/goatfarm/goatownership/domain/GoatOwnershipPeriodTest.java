package com.devmaster.goatfarm.goatownership.domain;

import com.devmaster.goatfarm.goat.domain.GoatId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GoatOwnershipPeriodTest {
    private static final GoatId GOAT = new GoatId(10L);
    private static final Instant START = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant TRANSFER = Instant.parse("2026-06-01T00:00:00Z");

    @Test
    void openPeriodContainsStartAndExcludesEnd() {
        GoatOwnershipPeriod period = GoatOwnershipPeriod.open(GOAT, 1L, START, OwnershipEntryType.BIRTH, "test");

        assertTrue(period.isOpen());
        assertTrue(period.contains(START));
        assertTrue(period.contains(TRANSFER));

        period.close(TRANSFER, OwnershipExitType.TRANSFER_OUT);

        assertFalse(period.isOpen());
        assertTrue(period.contains(START));
        assertFalse(period.contains(TRANSFER));
    }

    @Test
    void closeIsOneWayAndRequiresExitAfterStart() {
        GoatOwnershipPeriod period = GoatOwnershipPeriod.open(GOAT, 1L, START, OwnershipEntryType.BIRTH, "test");

        assertThrows(IllegalArgumentException.class,
                () -> period.close(START, OwnershipExitType.TRANSFER_OUT));
        period.close(TRANSFER, OwnershipExitType.TRANSFER_OUT);
        assertThrows(IllegalStateException.class,
                () -> period.close(TRANSFER.plusSeconds(1), OwnershipExitType.EXTERNAL_SALE));
    }

    @Test
    void adjacentPeriodsAreValidButOverlappingPeriodsAreRejected() {
        GoatOwnershipPeriod first = GoatOwnershipPeriod.rehydrate(1L, GOAT, 1L, START, TRANSFER,
                OwnershipEntryType.BIRTH, OwnershipExitType.TRANSFER_OUT, "test");
        GoatOwnershipPeriod second = GoatOwnershipPeriod.open(GOAT, 2L, TRANSFER,
                OwnershipEntryType.TRANSFER_IN, "test");

        assertFalse(first.overlaps(second));
        assertDoesNotThrow(() -> GoatOwnershipPeriod.ensureConsistent(List.of(first, second)));

        GoatOwnershipPeriod overlapping = GoatOwnershipPeriod.open(GOAT, 3L,
                TRANSFER.minusSeconds(1), OwnershipEntryType.TRANSFER_IN, "test");
        assertThrows(IllegalArgumentException.class,
                () -> GoatOwnershipPeriod.ensureConsistent(List.of(first, overlapping)));
    }

    @Test
    void onlyOneOpenPeriodIsAllowedPerGoat() {
        GoatOwnershipPeriod first = GoatOwnershipPeriod.open(GOAT, 1L, START, OwnershipEntryType.BIRTH, "test");
        GoatOwnershipPeriod second = GoatOwnershipPeriod.open(GOAT, 2L, TRANSFER, OwnershipEntryType.TRANSFER_IN, "test");

        assertThrows(IllegalArgumentException.class,
                () -> GoatOwnershipPeriod.ensureConsistent(List.of(first, second)));
    }
}
