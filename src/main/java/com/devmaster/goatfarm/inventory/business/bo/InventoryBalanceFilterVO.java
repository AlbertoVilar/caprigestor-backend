package com.devmaster.goatfarm.inventory.business.bo;

public record InventoryBalanceFilterVO(
        Long farmId,
        Long itemId,
        Long lotId,
        boolean activeOnly
) {
}
