package com.devmaster.goatfarm.reproduction.api.dto;

import com.devmaster.goatfarm.reproduction.enums.DiagnosisRecommendationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisRecommendationResponseDTO {
    private DiagnosisRecommendationStatus status;
    private LocalDate eligibleDate;
    private DiagnosisRecommendationCoverageDTO lastCoverage;
    private DiagnosisRecommendationCheckDTO lastCheck;
    private List<String> warnings;
}
