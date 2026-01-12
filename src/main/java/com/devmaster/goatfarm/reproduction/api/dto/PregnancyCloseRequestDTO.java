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
    @NotNull(message = "Close date is required")
    private LocalDate closeDate;

    @NotNull(message = "Status is required (e.g. CLOSED, LOST)")
    private PregnancyStatus status;

    private PregnancyCloseReason closeReason;

    private String notes;
}
