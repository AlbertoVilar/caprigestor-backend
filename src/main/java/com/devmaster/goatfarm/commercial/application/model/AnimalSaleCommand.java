package com.devmaster.goatfarm.commercial.application.model;

import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AnimalSaleCommand(
        Long id,
        Long farmId,
        Long customerId,
        Long goatTechnicalId,
        String goatRegistrationNumber,
        String goatName,
        LocalDate saleDate,
        BigDecimal amount,
        LocalDate dueDate,
        SalePaymentStatus paymentStatus,
        LocalDate paymentDate,
        String notes,
        Long targetFarmId
) {
    public AnimalSaleCommand(Long id, Long farmId, Long customerId, Long goatTechnicalId,
                             String goatRegistrationNumber, String goatName, LocalDate saleDate,
                             BigDecimal amount, LocalDate dueDate, SalePaymentStatus paymentStatus,
                             LocalDate paymentDate, String notes) {
        this(id, farmId, customerId, goatTechnicalId, goatRegistrationNumber, goatName, saleDate,
                amount, dueDate, paymentStatus, paymentDate, notes, null);
    }

}
