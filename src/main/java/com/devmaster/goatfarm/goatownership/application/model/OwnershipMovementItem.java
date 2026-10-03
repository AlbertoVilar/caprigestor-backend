package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Neutral read projection of one canonical ownership_transfer row. */
public record OwnershipMovementItem(
        Long movementId,
        Long goatId,
        String goatName,
        String goatRegistrationNumber,
        Long sourceFarmId,
        String sourceFarmName,
        Long targetFarmId,
        String targetFarmName,
        OwnershipMovementKind movementKind,
        OwnershipTransferStatus status,
        OwnershipMovementDirection direction,
        String reason,
        Instant requestedAt,
        Instant acceptedAt,
        Instant effectiveAt,
        Instant completedAt,
        Instant cancelledAt,
        boolean realized,
        Long saleId,
        LocalDate saleDate,
        BigDecimal amount,
        String paymentStatus,
        LocalDate paymentDate
) {
}
