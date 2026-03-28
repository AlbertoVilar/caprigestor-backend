package com.devmaster.goatfarm.inventory.business.bo;

import com.devmaster.goatfarm.inventory.domain.enums.InventoryAdjustDirection;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryMovementCreateRequestVO(
        InventoryMovementType type,
        BigDecimal quantity,
        Long itemId,
        Long lotId,
        InventoryAdjustDirection adjustDirection,
        LocalDate movementDate,
        String reason,
        BigDecimal unitCost,
        BigDecimal totalCost,
        LocalDate purchaseDate,
        String supplierName
) {

    public InventoryMovementCreateRequestVO(
            InventoryMovementType type,
            BigDecimal quantity,
            Long itemId,
            Long lotId,
            InventoryAdjustDirection adjustDirection,
            LocalDate movementDate,
            String reason
    ) {
        this(type, quantity, itemId, lotId, adjustDirection, movementDate, reason, null, null, null, null);
    }
}
