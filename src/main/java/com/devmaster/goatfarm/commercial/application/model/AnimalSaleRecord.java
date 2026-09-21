package com.devmaster.goatfarm.commercial.application.model;

import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AnimalSaleRecord(
        Long id,
        Long farmId,
        Long customerId,
        CustomerReference customer,
        Long goatTechnicalId,
        String goatRegistrationNumber,
        String goatName,
        LocalDate saleDate,
        BigDecimal amount,
        LocalDate dueDate,
        SalePaymentStatus paymentStatus,
        LocalDate paymentDate,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long targetFarmId
) {
    public AnimalSaleRecord(Long id, Long farmId, Long customerId, CustomerReference customer,
                            Long goatTechnicalId, String goatRegistrationNumber, String goatName,
                            LocalDate saleDate, BigDecimal amount, LocalDate dueDate,
                            SalePaymentStatus paymentStatus, LocalDate paymentDate, String notes,
                            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, farmId, customerId, customer, goatTechnicalId, goatRegistrationNumber, goatName,
                saleDate, amount, dueDate, paymentStatus, paymentDate, notes, createdAt, updatedAt, null);
    }

}
