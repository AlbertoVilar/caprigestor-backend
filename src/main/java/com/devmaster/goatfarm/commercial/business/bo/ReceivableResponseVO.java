package com.devmaster.goatfarm.commercial.business.bo;

import com.devmaster.goatfarm.commercial.enums.ReceivableSourceType;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReceivableResponseVO(
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
