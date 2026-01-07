package com.devmaster.goatfarm.application.ports.in;

import com.devmaster.goatfarm.milk.business.bo.MilkProductionRequestVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;

import java.time.LocalDate;
import java.util.List;

public interface MilkProductionUseCase {

    MilkProductionResponseVO createMilkProduction(Long farmId, String goatId, MilkProductionRequestVO requestVO);

    List<MilkProductionResponseVO> getMilkProductions(Long farmId, String goatId, LocalDate from, LocalDate to);
}
