package com.devmaster.goatfarm.commercial.api.dto;

import com.devmaster.goatfarm.commercial.enums.OperationalExpenseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OperationalExpenseRequestDTO(
        @NotNull(message = "category e obrigatoria.")
        OperationalExpenseCategory category,

        @NotBlank(message = "description e obrigatoria.")
        @Size(max = 200, message = "description deve ter no maximo 200 caracteres.")
        String description,

        @NotNull(message = "amount e obrigatorio.")
        @Positive(message = "amount deve ser maior que zero.")
        BigDecimal amount,

        @NotNull(message = "expenseDate e obrigatoria.")
        LocalDate expenseDate,

        @Size(max = 500, message = "notes deve ter no maximo 500 caracteres.")
        String notes
) {
}
