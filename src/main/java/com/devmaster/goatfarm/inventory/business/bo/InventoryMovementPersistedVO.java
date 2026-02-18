package com.devmaster.goatfarm.inventory.business.bo;

import com.devmaster.goatfarm.inventory.domain.enums.InventoryAdjustDirection;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

public record InventoryMovementPersistedVO(
        Long movementId,
        Long farmId,
        InventoryMovementType type,
        InventoryAdjustDirection adjustDirection,
        BigDecimal quantity,
        Long itemId,
        Long lotId,
        LocalDate movementDate,
        String reason,
        BigDecimal resultingBalance,
        OffsetDateTime createdAt
) {
    public InventoryMovementPersistedVO {
        Objects.requireNonNull(farmId, "farmId nao pode ser nulo.");
        Objects.requireNonNull(type, "type nao pode ser nulo.");
        Objects.requireNonNull(quantity, "quantity nao pode ser nulo.");
        Objects.requireNonNull(itemId, "itemId nao pode ser nulo.");
        Objects.requireNonNull(movementDate, "movementDate nao pode ser nulo.");
        Objects.requireNonNull(resultingBalance, "resultingBalance nao pode ser nulo.");
        Objects.requireNonNull(createdAt, "createdAt nao pode ser nulo.");
    }
}
