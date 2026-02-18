package com.devmaster.goatfarm.inventory.business.bo;

import java.util.Objects;

public record InventoryItemSnapshotVO(Long itemId, boolean trackLot) {
    public InventoryItemSnapshotVO {
        Objects.requireNonNull(itemId, "itemId nao pode ser nulo.");
    }
}
