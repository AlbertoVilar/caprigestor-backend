package com.devmaster.goatfarm.commercial.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MilkSaleRequestDTO(
        @NotNull(message = "Cliente e obrigatorio")
        Long customerId,
        @NotNull(message = "Data da venda e obrigatoria")
        LocalDate saleDate,
        @NotNull(message = "Quantidade e obrigatoria")
        @DecimalMin(value = "0.01", message = "Quantidade deve ser maior que zero")
        BigDecimal quantityLiters,
        @NotNull(message = "Preco unitario e obrigatorio")
        @DecimalMin(value = "0.01", message = "Preco unitario deve ser maior que zero")
        BigDecimal unitPrice,
        @NotNull(message = "Data de vencimento e obrigatoria")
        LocalDate dueDate,
        LocalDate paymentDate,
        String notes
) {
}
