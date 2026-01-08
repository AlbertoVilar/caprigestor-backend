package com.devmaster.goatfarm.milk.business.milkproductionservice;

import com.devmaster.goatfarm.application.ports.in.MilkProductionUseCase;
import com.devmaster.goatfarm.application.ports.out.LactationPersistencePort;
import com.devmaster.goatfarm.application.ports.out.MilkProductionPersistencePort;
import com.devmaster.goatfarm.config.exceptions.NoActiveLactationException;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionRequestVO;
import com.devmaster.goatfarm.milk.business.bo.MilkProductionResponseVO;
import com.devmaster.goatfarm.config.exceptions.DuplicateMilkProductionException;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.devmaster.goatfarm.milk.mapper.MilkProductionMapper;
import com.devmaster.goatfarm.milk.model.entity.Lactation;
import com.devmaster.goatfarm.milk.model.entity.MilkProduction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MilkProductionBusiness implements MilkProductionUseCase {

    /** Ports (infra abstraída) */
    private final MilkProductionPersistencePort milkProductionPersistencePort;
    private final LactationPersistencePort lactationPersistencePort;

    /** Mapper de domínio */
    private final MilkProductionMapper milkProductionMapper;

    /**
     * Criação de produção diária de leite
     */
    @Override
    public MilkProductionResponseVO createMilkProduction(
            Long farmId,
            String goatId,
            MilkProductionRequestVO requestVO
    ) {
        //=======================
        // *** VALIDAÇÃO *** //
        //=======================
        // Regra 1: Não permitir produção duplicada para a mesma data e turno
        validateNoDuplicateProduction(farmId, goatId, requestVO.getDate(), requestVO.getShift());
        Lactation lactation = getRequiredActiveLactation(farmId, goatId, requestVO.getDate());

        MilkProduction milkProduction = milkProductionMapper.toEntity(requestVO);
        milkProduction.setFarmId(farmId);
        milkProduction.setGoatId(goatId);
        milkProduction.setLactation(lactation);
        MilkProduction saved = milkProductionPersistencePort.save(milkProduction);
        return milkProductionMapper.toResponseVO(saved);
    }

    /**
     * Consulta de produções por período
     */
    @Override
    public List<MilkProductionResponseVO> getMilkProductions(
            Long farmId,
            String goatId,
            LocalDate from,
            LocalDate to
    ) {
        return null;
    }

    /* ==========================================================
       Regras de domínio (assinaturas internas)
       ========================================================== */

    /**
     * Regra 1:
     * Não permitir produção duplicada para a mesma data e turno
     */
    private void validateNoDuplicateProduction(
            Long farmId,
            String goatId,
            LocalDate date,
            MilkingShift shift
    ) {
        // implementação futura
        if (milkProductionPersistencePort.existsByFarmIdAndGoatIdAndDateAndShift(farmId, goatId, date, shift)) {
            throw new DuplicateMilkProductionException();
        }
    }

    /**
     * Regra 2:
     * Produção só pode existir se houver lactação ativa
     */
    private Lactation getRequiredActiveLactation(
            Long farmId,
            String goatId,
            LocalDate productionDate
    ) {
        return lactationPersistencePort
                .findActiveByFarmIdAndGoatId(farmId, goatId)
                .orElseThrow(NoActiveLactationException::new);
    }

}
