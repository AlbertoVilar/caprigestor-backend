package com.devmaster.goatfarm.health.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record HealthEventDoneRequestDTO(
    @NotNull(message = "A data de realização é obrigatória")
    @PastOrPresent(message = "A data de realização não pode ser futura")
    LocalDateTime performedAt,

    @NotBlank(message = "O responsável é obrigatório")
    @Size(max = 100, message = "O nome do responsável deve ter no máximo 100 caracteres")
    String responsible,

    @Size(max = 1000, message = "As notas devem ter no máximo 1000 caracteres")
    String notes
) {}
