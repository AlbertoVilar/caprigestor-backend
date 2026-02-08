package com.devmaster.goatfarm.reproduction.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PregnancyDiagnosisAlertItemDTO {
    private String goatId;
    private LocalDate eligibleDate;
    private int daysOverdue;
    private LocalDate lastCoverageDate;
    private LocalDate lastCheckDate;
}
