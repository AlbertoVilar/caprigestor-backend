package com.devmaster.goatfarm.milk.business.bo;

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
public class LactationResponseVO {
    private Long id;
    private LactationStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate pregnancyStartDate;
    private LocalDate dryStartDate;
}
