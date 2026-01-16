package com.devmaster.goatfarm.reproduction.business.bo;

import com.devmaster.goatfarm.reproduction.enums.PregnancyCloseReason;
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
public class PregnancyCloseRequestVO {
    private LocalDate closeDate;
    private PregnancyStatus status;
    private PregnancyCloseReason closeReason;
    private String notes;
}
