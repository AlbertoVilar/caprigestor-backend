package com.devmaster.goatfarm.inventory.business.bo;

public record InventoryItemResponseVO(
        Long id,
        Long farmId,
        String name,
        boolean trackLot,
        boolean active
) {
}
