package com.devmaster.goatfarm.events.api.dto;

import com.devmaster.goatfarm.events.enuns.EventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "DTO de resposta contendo os dados de um evento associado a uma cabra")
public record EventResponseDTO(

        @Schema(description = "ID do evento", example = "3")
        Long id,

        @Schema(description = "Número de registro da cabra", example = "2114517012")
        String goatId,

        @Schema(description = "Nome da cabra associada ao evento", example = "NAIDE DO CRS")
        String goatName,

        @Schema(description = "Tipo do evento", example = "PARTO")
        EventType eventType,

        @Schema(description = "Data do evento", example = "2025-05-10")
        LocalDate date,

        @Schema(description = "Descrição do evento", example = "Nascimento de dois cabritos machos sem complicações.")
        String description,

        @Schema(description = "Local do evento", example = "Capril Vilar")
        String location,

        @Schema(description = "Veterinário responsável", example = "Dr. João Silva")
        String veterinarian,

        @Schema(description = "Resultado do evento", example = "Ambos os cabritos nasceram saudáveis.")
        String outcome
) {}
