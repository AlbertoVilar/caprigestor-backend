package com.devmaster.goatfarm.commercial.business.bo;

import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

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
        String notes
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
}
