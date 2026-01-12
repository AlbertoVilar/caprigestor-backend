package com.devmaster.goatfarm.reproduction.api.dto;

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
    @NotNull(message = "Confirmation date is required")
    private LocalDate confirmationDate;

    private LocalDate expectedDueDate;
    private String notes;
}
