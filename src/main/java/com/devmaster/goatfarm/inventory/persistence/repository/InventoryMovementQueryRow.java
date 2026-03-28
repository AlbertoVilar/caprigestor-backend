package com.devmaster.goatfarm.inventory.persistence.repository;

import com.devmaster.goatfarm.inventory.domain.enums.InventoryAdjustDirection;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record InventoryMovementQueryRow(
        Long movementId,
        InventoryMovementType type,
        InventoryAdjustDirection adjustDirection,
        BigDecimal quantity,
        Long itemId,
        String itemName,
        Long lotId,
        LocalDate movementDate,
        String reason,
        BigDecimal resultingBalance,
        BigDecimal unitCost,
        BigDecimal totalCost,
        LocalDate purchaseDate,
        String supplierName,
        OffsetDateTime createdAt
) {
}
