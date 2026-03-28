package com.devmaster.goatfarm.commercial.business.bo;

import com.devmaster.goatfarm.commercial.enums.OperationalExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OperationalExpenseRequestVO(
        OperationalExpenseCategory category,
        String description,
        BigDecimal amount,
        LocalDate expenseDate,
        String notes
) {
}
