package com.devmaster.goatfarm.goatownership.api.dto;

import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;

import java.time.Instant;

/** Stable HTTP representation of one ownership period. */
public record OwnershipHistoryPeriodResponseDTO(
        Long farmId,
        Instant startedAt,
        Instant endedAt,
        OwnershipEntryType entryType,
        OwnershipExitType exitType,
        boolean current
) {
}
