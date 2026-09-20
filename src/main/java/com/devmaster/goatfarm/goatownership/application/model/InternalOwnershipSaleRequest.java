package com.devmaster.goatfarm.goatownership.application.model;

import com.devmaster.goatfarm.goat.domain.GoatId;

/** Neutral inbound command used by Commercial to create an ownership-backed sale. */
public record InternalOwnershipSaleRequest(
        GoatId goatId,
        Long sourceFarmId,
        Long targetFarmId,
        Long saleId,
        String reason,
        String idempotencyKey
) {
}
