package com.devmaster.goatfarm.reproduction.business.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BirthResponseVO {
    private PregnancyResponseVO pregnancy;
    private ReproductiveEventResponseVO closeEvent;
    private List<BirthKidResponseVO> kids;
}
