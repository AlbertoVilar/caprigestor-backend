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
        BigDecimal unitCost,
        BigDecimal totalCost,
        LocalDate purchaseDate,
        String supplierName,
        OffsetDateTime createdAt
) {
    public InventoryMovementPersistedVO {
        Objects.requireNonNull(farmId, "farmId não pode ser nulo.");
        Objects.requireNonNull(type, "type não pode ser nulo.");
        Objects.requireNonNull(quantity, "quantity não pode ser nulo.");
        Objects.requireNonNull(itemId, "itemId não pode ser nulo.");
        Objects.requireNonNull(movementDate, "movementDate não pode ser nulo.");
        Objects.requireNonNull(resultingBalance, "resultingBalance não pode ser nulo.");
        Objects.requireNonNull(createdAt, "createdAt não pode ser nulo.");
    }

    public InventoryMovementPersistedVO(
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
        this(
                movementId,
                farmId,
                type,
                adjustDirection,
                quantity,
                itemId,
                lotId,
                movementDate,
                reason,
                resultingBalance,
                null,
                null,
                null,
                null,
                createdAt
        );
    }
}
