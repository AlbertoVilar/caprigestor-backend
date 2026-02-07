package com.devmaster.goatfarm.reproduction.business.bo;

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
public class DiagnosisRecommendationResponseVO {
    private DiagnosisRecommendationStatus status;
    private LocalDate eligibleDate;
    private DiagnosisRecommendationCoverageVO lastCoverage;
    private DiagnosisRecommendationCheckVO lastCheck;
    private List<String> warnings;
}
