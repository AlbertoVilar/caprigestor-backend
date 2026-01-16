package com.devmaster.goatfarm.reproduction.business.bo;

import com.devmaster.goatfarm.reproduction.enums.BreedingType;
import com.devmaster.goatfarm.reproduction.enums.PregnancyCheckResult;
import com.devmaster.goatfarm.reproduction.enums.ReproductiveEventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReproductiveEventResponseVO {
    private Long id;
    private Long farmId;
    private String goatId;
    private Long pregnancyId;
    private ReproductiveEventType eventType;
    private LocalDate eventDate;
    private BreedingType breedingType;
    private String breederRef;
    private String notes;
    private LocalDate checkScheduledDate;
    private PregnancyCheckResult checkResult;
    // checkDate removed
}
