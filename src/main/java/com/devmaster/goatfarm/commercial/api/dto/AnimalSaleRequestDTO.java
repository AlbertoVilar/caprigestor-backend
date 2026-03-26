package com.devmaster.goatfarm.commercial.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AnimalSaleRequestDTO(
        @NotBlank(message = "Cabra e obrigatoria")
        String goatId,
        @NotNull(message = "Cliente e obrigatorio")
        Long customerId,
        @NotNull(message = "Data da venda e obrigatoria")
        LocalDate saleDate,
        @NotNull(message = "Valor da venda e obrigatorio")
        @DecimalMin(value = "0.01", message = "Valor da venda deve ser maior que zero")
        BigDecimal amount,
        @NotNull(message = "Data de vencimento e obrigatoria")
        LocalDate dueDate,
        LocalDate paymentDate,
        String notes
) {
}
