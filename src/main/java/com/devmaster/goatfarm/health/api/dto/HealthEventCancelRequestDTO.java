package com.devmaster.goatfarm.health.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record HealthEventCancelRequestDTO(
    @NotBlank(message = "O motivo do cancelamento é obrigatório")
    @Size(max = 1000, message = "O motivo do cancelamento deve ter no máximo 1000 caracteres")
    String notes
) {}
