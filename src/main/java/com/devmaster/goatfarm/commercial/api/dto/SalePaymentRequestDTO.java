package com.devmaster.goatfarm.commercial.api.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SalePaymentRequestDTO(
        @NotNull(message = "Data de pagamento e obrigatoria")
        LocalDate paymentDate
) {
}
