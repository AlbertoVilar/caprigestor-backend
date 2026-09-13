package com.devmaster.goatfarm.commercial.application.model;

import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MilkSaleRecord(
        Long id,
        Long farmId,
        Long customerId,
        CustomerReference customer,
        LocalDate saleDate,
        BigDecimal quantityLiters,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        LocalDate dueDate,
        SalePaymentStatus paymentStatus,
        LocalDate paymentDate,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
