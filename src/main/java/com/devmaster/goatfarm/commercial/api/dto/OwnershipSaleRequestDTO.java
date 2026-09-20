package com.devmaster.goatfarm.commercial.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OwnershipSaleRequestDTO(
        @NotBlank String goatId,
        @NotNull Long customerId,
        @NotNull Long targetFarmId,
        @NotNull LocalDate saleDate,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull LocalDate dueDate,
        String notes,
        @NotBlank String idempotencyKey
) {
}
