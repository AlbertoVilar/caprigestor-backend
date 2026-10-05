package com.devmaster.goatfarm.commercial.application.model;

import com.devmaster.goatfarm.commercial.enums.OperationalExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Technology-neutral persisted representation of an operational expense. */
public record OperationalExpenseRecord(
        Long id,
        Long farmId,
        OperationalExpenseCategory category,
        String description,
        BigDecimal amount,
        LocalDate expenseDate,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
