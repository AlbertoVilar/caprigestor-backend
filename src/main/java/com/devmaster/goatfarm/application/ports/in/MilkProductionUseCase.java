package com.devmaster.goatfarm.application.ports.in;

import com.devmaster.goatfarm.milk.business.bo.MilkProductionRequestVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionUpdateRequestVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface MilkProductionUseCase {

    MilkProductionResponseVO createMilkProduction(
            Long farmId,
            String goatId,
            MilkProductionRequestVO requestVO
    );

    MilkProductionResponseVO update(
            Long farmId,
            String goatId,
            Long id,
            MilkProductionUpdateRequestVO request
    );


    Page<MilkProductionResponseVO> getMilkProductions(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to,
            Pageable pageable
    );

    MilkProductionResponseVO findById(Long farmId, String goatId, Long id);

    void delete(Long farmId, String goatId, Long id);

}
