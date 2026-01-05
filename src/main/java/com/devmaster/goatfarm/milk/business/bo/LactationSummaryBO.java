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
public class LactationSummaryBO {
    private BigDecimal totalLiters;
    private Integer daysInLactation;
    private BigDecimal averagePerDay;
    private BigDecimal peakLiters;
    private LocalDate peakDate;
}
