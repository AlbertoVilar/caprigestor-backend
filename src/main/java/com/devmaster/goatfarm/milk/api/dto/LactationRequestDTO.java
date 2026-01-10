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
public class LactationRequestDTO {

    @NotNull(message = "Data de início é obrigatória")
    @Schema(description = "Data de início da lactação", example = "2026-01-01", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate startDate;
}
