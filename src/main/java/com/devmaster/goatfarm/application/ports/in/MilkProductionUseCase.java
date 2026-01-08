package com.devmaster.goatfarm.application.ports.in;

import com.devmaster.goatfarm.milk.business.bo.MilkProductionRequestVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface MilkProductionUseCase {

    MilkProductionResponseVO createMilkProduction(Long farmId, String goatId, MilkProductionRequestVO requestVO);

    Page<MilkProductionResponseVO> getMilkProductions(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );
}
