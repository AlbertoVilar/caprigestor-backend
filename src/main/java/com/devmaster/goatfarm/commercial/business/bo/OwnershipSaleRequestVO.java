package com.devmaster.goatfarm.commercial.business.bo;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Request for a farm-to-farm animal sale. The destination farm is the canonical buyer. */
public record OwnershipSaleRequestVO(
        String goatId,
        Long targetFarmId,
        LocalDate saleDate,
        BigDecimal amount,
        LocalDate dueDate,
        LocalDate paymentDate,
        String notes,
        String idempotencyKey
) {
}
