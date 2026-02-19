package com.devmaster.goatfarm.inventory.business.bo;

import java.util.Objects;

public record InventoryMovementResultVO(
        InventoryMovementResponseVO response,
        boolean replayed
) {
    public InventoryMovementResultVO {
        Objects.requireNonNull(response, "response nao pode ser nulo.");
    }
}
