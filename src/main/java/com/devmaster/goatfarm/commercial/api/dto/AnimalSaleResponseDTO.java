package com.devmaster.goatfarm.commercial.api.dto;

import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AnimalSaleResponseDTO(
        Long id,
        Long goatTechnicalId,
        String goatRegistrationNumber,
        String goatName,
        Long customerId,
        String customerName,
        LocalDate saleDate,
        BigDecimal amount,
        LocalDate dueDate,
        SalePaymentStatus paymentStatus,
        LocalDate paymentDate,
        String notes,
        boolean reversed,
        LocalDateTime reversedAt,
        String reversalReason
) {
    public AnimalSaleResponseDTO(
            Long id,
            Long goatTechnicalId,
            String goatRegistrationNumber,
            String goatName,
            Long customerId,
            String customerName,
            LocalDate saleDate,
            BigDecimal amount,
            LocalDate dueDate,
            SalePaymentStatus paymentStatus,
            LocalDate paymentDate,
            String notes
    ) {
        this(id, goatTechnicalId, goatRegistrationNumber, goatName, customerId, customerName,
                saleDate, amount, dueDate, paymentStatus, paymentDate, notes, false, null, null);
    }
}
