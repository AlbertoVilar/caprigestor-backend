package com.devmaster.goatfarm.inventory.business.bo;

import java.util.Objects;

public record InventoryIdempotencyVO(
        Long farmId,
        String idempotencyKey,
        String requestHash,
        InventoryMovementResponseVO response
) {
    public InventoryIdempotencyVO {
        Objects.requireNonNull(farmId, "farmId nao pode ser nulo.");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey nao pode ser nulo.");
        Objects.requireNonNull(requestHash, "requestHash nao pode ser nulo.");
        Objects.requireNonNull(response, "response nao pode ser nulo.");
    }
}
