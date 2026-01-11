package com.devmaster.goatfarm.milk.api.dto;

import com.devmaster.goatfarm.milk.enums.LactationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LactationResponseDTO {
    private Long id;
    private Long farmId;
    private String goatId;
    private LactationStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate pregnancyStartDate;
    private LocalDate dryStartDate;
}
