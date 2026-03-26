package com.devmaster.goatfarm.commercial.business.bo;

import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MilkSaleResponseVO(
        Long id,
        Long customerId,
        String customerName,
        LocalDate saleDate,
        BigDecimal quantityLiters,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        LocalDate dueDate,
        SalePaymentStatus paymentStatus,
        LocalDate paymentDate,
        String notes
) {
}
