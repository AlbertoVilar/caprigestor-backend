package com.devmaster.goatfarm.reproduction.api.dto;

import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PregnancyResponseDTO {
    private Long id;
    private Long farmId;
    private String goatId;
    private PregnancyStatus status;
    private LocalDate breedingDate;
    private LocalDate confirmedAt;
    private LocalDate expectedDueDate;
    private LocalDate recommendedDryDate;
    private LocalDate closedAt;
}
