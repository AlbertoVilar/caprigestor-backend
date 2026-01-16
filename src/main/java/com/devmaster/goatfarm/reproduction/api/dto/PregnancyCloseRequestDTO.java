package com.devmaster.goatfarm.reproduction.api.dto;

import com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
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
public class PregnancyCloseRequestDTO {
    @NotNull(message = "Data de fechamento é obrigatória")
    private LocalDate closeDate;

    @NotNull(message = "Motivo/Status de fechamento é obrigatório (ex.: ENCERRADA, PERDIDA)")
    private PregnancyStatus status;

    private PregnancyCloseReason closeReason;

    private String notes;
}
