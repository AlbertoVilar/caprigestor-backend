package com.devmaster.goatfarm.inventory.persistence.repository;

import java.math.BigDecimal;

public record InventoryBalanceQueryRow(
        Long itemId,
        String itemName,
        boolean trackLot,
        Long lotId,
        BigDecimal quantity
) {
}
