package com.devmaster.goatfarm.milk.business.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LactationDryOffAlertVO {
    private Long lactationId;
    private String goatId;
    private LocalDate startDatePregnancy;
    private LocalDate breedingDate;
    private LocalDate confirmDate;
    private LocalDate dryOffDate;
    private int dryAtPregnancyDays;
    private int gestationDays;
    private int daysOverdue;
    private boolean dryOffRecommendation;
}
