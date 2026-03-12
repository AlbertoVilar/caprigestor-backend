package com.devmaster.goatfarm.inventory.business.bo;

import java.util.Objects;

public record InventoryLotSnapshotVO(
        Long lotId,
        Long farmId,
        Long itemId,
        String code,
        boolean active
) {
    public InventoryLotSnapshotVO {
        Objects.requireNonNull(lotId, "lotId não pode ser nulo.");
        Objects.requireNonNull(farmId, "farmId não pode ser nulo.");
        Objects.requireNonNull(itemId, "itemId não pode ser nulo.");
        Objects.requireNonNull(code, "code não pode ser nulo.");
    }
}
