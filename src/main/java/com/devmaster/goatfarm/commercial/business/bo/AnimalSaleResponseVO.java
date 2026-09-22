package com.devmaster.goatfarm.commercial.business.bo;

import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AnimalSaleResponseVO(
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
    /** Compatibility constructor for callers created before GoatId exposure. */
    public AnimalSaleResponseVO(
            Long id,
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
        this(id, null, goatRegistrationNumber, goatName, customerId, customerName,
                saleDate, amount, dueDate, paymentStatus, paymentDate, notes);
    }

    public AnimalSaleResponseVO(
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
