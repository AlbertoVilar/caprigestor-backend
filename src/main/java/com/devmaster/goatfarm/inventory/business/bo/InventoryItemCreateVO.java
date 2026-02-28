package com.devmaster.goatfarm.inventory.business.bo;

public record InventoryItemCreateVO(
        Long farmId,
        String name,
        boolean trackLot,
        boolean active
) {
}
