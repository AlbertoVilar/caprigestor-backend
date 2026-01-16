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
public class PregnancyConfirmRequestDTO {
    @NotNull(message = "Data da confirmação é obrigatória")
    private LocalDate checkDate;

    @NotNull(message = "Resultado da confirmação é obrigatório")
    private PregnancyCheckResult checkResult;
    private String notes;
}
