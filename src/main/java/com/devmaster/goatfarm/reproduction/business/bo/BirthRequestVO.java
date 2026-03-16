package com.devmaster.goatfarm.reproduction.business.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BirthRequestVO {
    private LocalDate birthDate;
    private String fatherRegistrationNumber;
    private String notes;
    private List<BirthKidRequestVO> kids;
}
