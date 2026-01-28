package com.devmaster.goatfarm.milk.business.bo;

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
public class LactationSummaryResponseVO {
    private LactationSummaryLactationVO lactation;
    private LactationSummaryProductionVO production;
    private LactationSummaryPregnancyVO pregnancy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LactationSummaryLactationVO {
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
    public static class LactationSummaryProductionVO {
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
    public static class LactationSummaryPregnancyVO {
        private Integer gestationDays;
        private Boolean dryOffRecommendation;
        private LocalDate recommendedDryOffDate;
        private String message;
    }
}
