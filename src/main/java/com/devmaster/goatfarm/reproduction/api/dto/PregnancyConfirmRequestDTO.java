package com.devmaster.goatfarm.reproduction.api.dto;

import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
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
public class PregnancyConfirmRequestDTO {
    @NotNull(message = "Check date is required")
    private LocalDate checkDate;

    @NotNull(message = "Check result is required")
    private PregnancyCheckResult checkResult;
    private String notes;
}
