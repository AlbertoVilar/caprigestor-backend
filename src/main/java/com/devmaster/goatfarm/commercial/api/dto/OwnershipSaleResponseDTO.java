package com.devmaster.goatfarm.commercial.api.dto;

import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OwnershipSaleResponseDTO(
        Long saleId,
        Long sourceFarmId,
        Long targetFarmId,
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
        Long ownershipTransferId,
        OwnershipTransferStatus ownershipTransferStatus
) {
}
