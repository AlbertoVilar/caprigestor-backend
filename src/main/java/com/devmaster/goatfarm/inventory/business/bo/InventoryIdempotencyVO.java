package com.devmaster.goatfarm.inventory.business.bo;

import java.util.Objects;

public record InventoryIdempotencyVO(
        String idempotencyKey,
        String requestHash,
        InventoryMovementResponseVO response
) {
    public InventoryIdempotencyVO {
        Objects.requireNonNull(idempotencyKey, "idempotencyKey não pode ser nulo.");
        Objects.requireNonNull(requestHash, "requestHash não pode ser nulo.");
        Objects.requireNonNull(response, "response não pode ser nulo.");
    }
}
