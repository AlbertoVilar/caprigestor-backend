package com.devmaster.goatfarm.commercial.application.model;

import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MilkSaleCommand(
        Long id,
        Long farmId,
        Long customerId,
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
