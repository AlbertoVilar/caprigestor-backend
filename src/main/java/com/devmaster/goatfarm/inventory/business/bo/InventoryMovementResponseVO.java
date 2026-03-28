package com.devmaster.goatfarm.inventory.business.bo;

import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record InventoryMovementResponseVO(
        Long movementId,
        InventoryMovementType type,
        BigDecimal quantity,
        Long itemId,
        Long lotId,
        LocalDate movementDate,
        BigDecimal resultingBalance,
        BigDecimal unitCost,
        BigDecimal totalCost,
        LocalDate purchaseDate,
        String supplierName,
        OffsetDateTime createdAt
) {

    public InventoryMovementResponseVO(
            Long movementId,
            InventoryMovementType type,
            BigDecimal quantity,
            Long itemId,
            Long lotId,
            LocalDate movementDate,
            BigDecimal resultingBalance,
            OffsetDateTime createdAt
    ) {
        this(movementId, type, quantity, itemId, lotId, movementDate, resultingBalance, null, null, null, null, createdAt);
    }
}
