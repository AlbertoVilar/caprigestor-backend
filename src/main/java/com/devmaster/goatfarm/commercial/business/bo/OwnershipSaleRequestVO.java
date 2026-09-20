package com.devmaster.goatfarm.commercial.business.bo;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Request for a farm-to-farm animal sale. Acceptance and payment are independent prerequisites for ownership completion. */
public record OwnershipSaleRequestVO(
        String goatId,
        Long customerId,
        Long targetFarmId,
        LocalDate saleDate,
        BigDecimal amount,
        LocalDate dueDate,
        String notes,
        String idempotencyKey
) {
}
