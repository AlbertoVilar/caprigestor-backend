package com.devmaster.goatfarm.milk.api.dto;

import com.devmaster.goatfarm.milk.enums.MilkingShift;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MilkProductionRequestDTO {

    @NotNull(message = "Data da ordenha é obrigatória")
    @Schema(description = "Data da ordenha (obrigatória)", example = "2026-01-30", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate date;

    @NotNull(message = "Turno da ordenha e obrigatorio")
    private MilkingShift shift;
    @NotNull(message = "Volume produzido e obrigatorio")
    private BigDecimal volumeLiters;
    private String notes;
}
