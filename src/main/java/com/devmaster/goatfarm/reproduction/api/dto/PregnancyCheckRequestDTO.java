package com.devmaster.goatfarm.reproduction.api.dto;

import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PregnancyCheckRequestDTO {
    @NotNull(message = "Data do diagnóstico de prenhez é obrigatória")
    private LocalDate checkDate;

    @NotNull(message = "Resultado do diagnóstico de prenhez é obrigatório")
    private PregnancyCheckResult checkResult;

    private String notes;
}
