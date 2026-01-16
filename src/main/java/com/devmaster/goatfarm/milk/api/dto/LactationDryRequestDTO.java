package com.devmaster.goatfarm.milk.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LactationDryRequestDTO {

    @NotNull(message = "Data de encerramento é obrigatória")
    @Schema(description = "Date when lactation ended (dry date)", example = "2026-10-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate endDate;
}
