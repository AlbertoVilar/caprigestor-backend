package com.devmaster.goatfarm.inventory.business.bo;

import java.util.Objects;

public record InventoryIdempotencyVO(
        Long farmId,
        String idempotencyKey,
        String requestHash,
        InventoryMovementResponseVO response
) {
    public InventoryIdempotencyVO {
        Objects.requireNonNull(farmId, "farmId não pode ser nulo.");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey não pode ser nulo.");
        Objects.requireNonNull(requestHash, "requestHash não pode ser nulo.");
        Objects.requireNonNull(response, "response não pode ser nulo.");
    }
}
