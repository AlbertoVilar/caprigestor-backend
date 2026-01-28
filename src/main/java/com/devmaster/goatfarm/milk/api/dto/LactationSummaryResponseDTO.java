package com.devmaster.goatfarm.milk.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LactationSummaryResponseDTO {
    private LactationSummaryLactationDTO lactation;
    private LactationSummaryProductionDTO production;
    private LactationSummaryPregnancyDTO pregnancy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LactationSummaryLactationDTO {
        private Long lactationId;
        private String goatId;
        private LocalDate startDate;
        private LocalDate endDate;
        private com.devmaster.goatfarm.milk.enums.LactationStatus status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LactationSummaryProductionDTO {
        private BigDecimal totalLiters;
        private Integer daysInLactation;
        private Integer daysMeasured;
        private BigDecimal averagePerDay;
        private BigDecimal peakLiters;
        private LocalDate peakDate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LactationSummaryPregnancyDTO {
        private Integer gestationDays;
        private Boolean dryOffRecommendation;
        private LocalDate recommendedDryOffDate;
        private String message;
    }
}
