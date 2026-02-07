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
    @NotNull(message = "Data do diagnÃ³stico de prenhez Ã© obrigatÃ³ria")
    private LocalDate checkDate;

    @NotNull(message = "Resultado do diagnÃ³stico de prenhez Ã© obrigatÃ³rio")
    private PregnancyCheckResult checkResult;

    private String notes;
}
