package com.devmaster.goatfarm.commercial.application.model;

import com.devmaster.goatfarm.commercial.enums.OperationalExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Technology-neutral command for creating or updating an operational expense. */
public record OperationalExpenseCommand(
        Long id,
        Long farmId,
        OperationalExpenseCategory category,
        String description,
        BigDecimal amount,
        LocalDate expenseDate,
        String notes
) {
}
