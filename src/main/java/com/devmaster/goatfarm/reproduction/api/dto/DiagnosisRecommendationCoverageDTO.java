package com.devmaster.goatfarm.reproduction.api.dto;

import com.devmaster.goatfarm.reproduction.enums.BreedingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisRecommendationCoverageDTO {
    private Long id;
    private LocalDate eventDate;
    private LocalDate effectiveDate;
    private BreedingType breedingType;
    private String breederRef;
    private String notes;
}
