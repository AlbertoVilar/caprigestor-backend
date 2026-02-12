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
        OffsetDateTime createdAt
) {
}
