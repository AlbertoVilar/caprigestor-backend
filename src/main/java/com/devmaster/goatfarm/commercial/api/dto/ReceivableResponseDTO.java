package com.devmaster.goatfarm.commercial.api.dto;

import com.devmaster.goatfarm.commercial.enums.ReceivableSourceType;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceivableResponseDTO(
        ReceivableSourceType sourceType,
        Long sourceId,
        String sourceLabel,
        Long customerId,
        String customerName,
        BigDecimal amount,
        LocalDate dueDate,
        SalePaymentStatus paymentStatus,
        LocalDate paymentDate,
        String notes
) {
}
