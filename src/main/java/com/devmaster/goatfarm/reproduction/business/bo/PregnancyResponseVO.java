package com.devmaster.goatfarm.reproduction.business.bo;

import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PregnancyResponseVO {
    private Long id;
    private Long farmId;
    private String goatId;
    private PregnancyStatus status;
    private LocalDate breedingDate;
    private LocalDate confirmedAt;
    private LocalDate expectedDueDate;
    private LocalDate recommendedDryDate;
    private LocalDate closedAt;
}
