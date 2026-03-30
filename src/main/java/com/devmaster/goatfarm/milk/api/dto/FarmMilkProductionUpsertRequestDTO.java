package com.devmaster.goatfarm.milk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record FarmMilkProductionUpsertRequestDTO(
        @NotNull(message = "totalProduced e obrigatorio")
        @DecimalMin(value = "0.00", message = "totalProduced nao pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "totalProduced deve ter no maximo 10 digitos inteiros e 2 decimais")
        @Schema(description = "Volume total produzido no dia", example = "185.50", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal totalProduced,

        @DecimalMin(value = "0.00", message = "withdrawalProduced nao pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "withdrawalProduced deve ter no maximo 10 digitos inteiros e 2 decimais")
        @Schema(description = "Volume produzido sob carencia/restricao", example = "12.30")
        BigDecimal withdrawalProduced,

        @DecimalMin(value = "0.00", message = "marketableProduced nao pode ser negativo")
        @Digits(integer = 10, fraction = 2, message = "marketableProduced deve ter no maximo 10 digitos inteiros e 2 decimais")
        @Schema(description = "Volume liberado/comercializavel", example = "173.20")
        BigDecimal marketableProduced,

        @Size(max = 1000, message = "notes deve ter no maximo 1000 caracteres")
        @Schema(description = "Observacoes operacionais do registro diario")
        String notes
) {
}
