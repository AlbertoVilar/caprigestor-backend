package com.devmaster.goatfarm.inventory.business.bo;

import java.math.BigDecimal;

public record InventoryBalanceResponseVO(
        Long itemId,
        String itemName,
        boolean trackLot,
        Long lotId,
        BigDecimal quantity
) {
}
