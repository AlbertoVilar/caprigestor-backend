package com.devmaster.goatfarm.events.api.dto;

import com.devmaster.goatfarm.events.enuns.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record EventRequestDTO(

        @NotBlank(message = "O número de registro não pode estar em branco.")
        @Size(min = 10, max = 12, message = "O registro deve ter entre {min} e {max} caracteres.")
        String goatId,

        @NotNull(message = "O tipo do evento é obrigatório")
        EventType eventType,

        @NotNull(message = "A data do evento não pode estar em branco.")
        LocalDate date,

        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 200, message = "A descrição do evento deve ter no máximo 200 caracteres")
        String description,

        @NotBlank(message = "O local é obrigatório")
        String location,

        @NotBlank(message = "O nome do veterinário é obrigatório")
        String veterinarian,

        @NotBlank(message = "O resultado é obrigatório")
        @Size(max = 500, message = "O resultado do evento deve ter no máximo 500 caracteres")
        String outcome
) {}
