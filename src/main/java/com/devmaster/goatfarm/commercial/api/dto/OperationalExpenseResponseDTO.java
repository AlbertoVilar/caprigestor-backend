package com.devmaster.goatfarm.commercial.api.dto;

import com.devmaster.goatfarm.commercial.enums.OperationalExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OperationalExpenseResponseDTO(
        Long id,
        OperationalExpenseCategory category,
        String description,
        BigDecimal amount,
        LocalDate expenseDate,
        String notes,
        LocalDateTime createdAt
) {
}
