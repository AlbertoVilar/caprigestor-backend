package com.devmaster.goatfarm.inventory.business.bo;

public record InventoryItemCreateRequestVO(
        String name,
        Boolean trackLot
) {
}
