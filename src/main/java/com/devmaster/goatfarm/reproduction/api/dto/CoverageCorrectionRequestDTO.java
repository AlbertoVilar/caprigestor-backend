package com.devmaster.goatfarm.reproduction.api.dto;

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
public class CoverageCorrectionRequestDTO {
    @NotNull(message = "Data corrigida é obrigatória")
    private LocalDate correctedDate;

    private String notes;
}
