package com.devmaster.goatfarm.commercial.business.bo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AnimalSaleRequestVO(
        String goatId,
        Long customerId,
        LocalDate saleDate,
        BigDecimal amount,
        LocalDate dueDate,
        LocalDate paymentDate,
        String notes
) {
}
