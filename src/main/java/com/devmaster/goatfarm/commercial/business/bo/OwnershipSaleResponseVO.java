package com.devmaster.goatfarm.commercial.business.bo;

import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Read model for a commercial sale backed by one canonical ownership transfer. */
public record OwnershipSaleResponseVO(
        Long saleId,
        Long sourceFarmId,
        Long targetFarmId,
        String targetFarmName,
        String targetFarmTod,
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
