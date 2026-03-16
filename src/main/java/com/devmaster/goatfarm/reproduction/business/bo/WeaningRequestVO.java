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
public class WeaningRequestVO {
    private LocalDate weaningDate;
    private String notes;
}
