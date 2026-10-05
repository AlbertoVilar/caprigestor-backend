package com.devmaster.goatfarm.inventory.business.bo;

public record InventoryLotFilterVO(
        Long farmId,
        Long itemId,
        Boolean active
) {
}
