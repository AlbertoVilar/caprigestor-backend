package com.devmaster.goatfarm.goatownership.api.dto;

import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;

import java.time.Instant;

/** Stable HTTP representation of an ownership transfer. */
public record OwnershipTransferResponseDTO(
        Long id,
        Long goatId,
        Long sourceFarmId,
        Long targetFarmId,
        OwnershipTransferKind kind,
        OwnershipTransferStatus status,
        String reason,
        Instant requestedAt,
        Instant acceptedAt,
        Instant effectiveAt,
        Instant completedAt,
        Instant cancelledAt
) {
}
