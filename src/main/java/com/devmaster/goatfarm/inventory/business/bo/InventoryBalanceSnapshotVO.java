package com.devmaster.goatfarm.inventory.business.bo;

import java.math.BigDecimal;
import java.util.Objects;

public record InventoryBalanceSnapshotVO(
        Long farmId,
        Long itemId,
        Long lotId,
        BigDecimal quantity
) {
    public InventoryBalanceSnapshotVO {
        Objects.requireNonNull(farmId, "farmId não pode ser nulo.");
        Objects.requireNonNull(itemId, "itemId não pode ser nulo.");
        Objects.requireNonNull(quantity, "quantity não pode ser nulo.");
    }
}
