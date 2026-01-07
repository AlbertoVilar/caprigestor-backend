package com.devmaster.goatfarm.milk.business.bo;

import com.devmaster.goatfarm.milk.enums.LactationStatus;
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

    /** Identificadores de contexto */
    private Long lactationId;
    private String goatId;

    /** Período da lactação */
    private LocalDate startDate;
    private LocalDate endDate;
    private LactationStatus status;

    /** Métricas de produção */
    private BigDecimal totalLiters;
    private Integer daysInLactation;
    private Integer daysMeasured;
    private BigDecimal averagePerDay;

    /** Pico de produção */
    private BigDecimal peakLiters;
    private LocalDate peakDate;
}
