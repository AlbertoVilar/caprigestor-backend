package com.devmaster.goatfarm.reproduction.business.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoverageCorrectionRequestVO {
    private LocalDate correctedDate;
    private String notes;
}
