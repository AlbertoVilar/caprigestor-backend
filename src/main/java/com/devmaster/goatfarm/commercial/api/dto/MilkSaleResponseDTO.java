package com.devmaster.goatfarm.commercial.api.dto;

import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MilkSaleResponseDTO(
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
