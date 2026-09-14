package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;

import java.time.Instant;
import java.util.Objects;

/** Public-safe application representation of one ownership interval. */
public record OwnershipHistoryPeriod(
        Long farmId,
        Instant startedAt,
        Instant endedAt,
        OwnershipEntryType entryType,
        OwnershipExitType exitType,
        boolean current
) {
    public OwnershipHistoryPeriod {
        if (farmId == null || farmId <= 0) {
            throw new IllegalArgumentException("farmId must be positive");
        }
        startedAt = Objects.requireNonNull(startedAt, "startedAt must not be null");
        entryType = Objects.requireNonNull(entryType, "entryType must not be null");
        if (current != (endedAt == null)) {
            throw new IllegalArgumentException("current must be derived from endedAt");
        }
    }
}
