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
        String reason
) {
}
