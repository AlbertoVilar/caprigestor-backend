package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;

/** Input command for a consented transfer between two local farms. */
public record InternalOwnershipTransferRequest(
        GoatId goatId,
        Long targetFarmId,
        String reason,
        String idempotencyKey
) {
}
