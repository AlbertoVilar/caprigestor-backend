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
public class PregnancyDueAlertItemDTO {
    private Long pregnancyId;
    private String goatId;
    private LocalDate expectedDueDate;
    private int daysOverdue;
}
