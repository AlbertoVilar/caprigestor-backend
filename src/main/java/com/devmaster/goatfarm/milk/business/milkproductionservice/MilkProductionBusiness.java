package com.devmaster.goatfarm.milk.business.milkproductionservice;

import com.devmaster.goatfarm.application.ports.in.MilkProductionUseCase;
import com.devmaster.goatfarm.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.application.ports.out.MilkProductionPersistencePort;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionRequestVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import com.devmaster.goatfarm.milk.mapper.MilkProductionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MilkProductionBusiness implements MilkProductionUseCase {

    private final MilkProductionPersistencePort milkProductionPersistencePort;
    private final LactationPersistencePort lactationPersistencePort;
    private final MilkProductionMapper milkProductionMapper;

    @Override
    public MilkProductionResponseVO createMilkProduction(Long farmId, String goatId, MilkProductionRequestVO requestVO) {
        return null;
    }

    @Override
    public List<MilkProductionResponseVO> getMilkProductions(Long farmId, String goatId, LocalDate from, LocalDate to) {
        return null;
    }
}
