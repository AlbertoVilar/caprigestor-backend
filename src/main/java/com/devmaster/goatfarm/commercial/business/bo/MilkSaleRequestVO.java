package com.devmaster.goatfarm.commercial.business.bo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MilkSaleRequestVO(
        Long customerId,
        LocalDate saleDate,
        BigDecimal quantityLiters,
        BigDecimal unitPrice,
        LocalDate dueDate,
        LocalDate paymentDate,
        String notes
) {
}
