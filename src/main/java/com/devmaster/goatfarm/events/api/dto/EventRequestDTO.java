package com.devmaster.goatfarm.events.api.dto;

import com.devmaster.goatfarm.events.enuns.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "DTO de requisição para criação ou atualização de um evento")
public record EventRequestDTO(

        @Schema(description = "Número de registro da cabra", example = "2114517012")
        @NotBlank(message = "O número de registro não pode estar em branco.")
        @Size(min = 10, max = 12, message = "O registro deve ter entre {min} e {max} caracteres.")
        String goatId,

        @Schema(description = "Tipo do evento", example = "SAUDE")
        @NotNull(message = "O tipo do evento é obrigatório")
        EventType eventType,

        @Schema(description = "Data do evento (formato: yyyy-MM-dd)", example = "2025-05-10")
        @NotNull(message = "A data do evento não pode estar em branco.")
        LocalDate date,

        @Schema(description = "Descrição detalhada do evento", example = "Aplicação de vacina contra clostridiose.")
        @NotBlank(message = "A descrição é obrigatória")
        @Size(max = 200, message = "A descrição do evento deve ter no máximo 200 caracteres")
        String description,

        @Schema(description = "Local onde o evento ocorreu", example = "Capril Vilar")
        @NotBlank(message = "O local é obrigatório")
        String location,

        @Schema(description = "Nome do veterinário responsável", example = "Dra. Ana Silva")
        @NotBlank(message = "O nome do veterinário é obrigatório")
        String veterinarian,

        @Schema(description = "Resultado ou observação do evento", example = "Animal vacinado com sucesso, sem reações.")
        @NotBlank(message = "O resultado é obrigatório")
        @Size(max = 500, message = "O resultado do evento deve ter no máximo 500 caracteres")
        String outcome
) {}
