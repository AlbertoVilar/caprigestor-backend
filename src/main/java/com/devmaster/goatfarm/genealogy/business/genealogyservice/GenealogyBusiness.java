package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.genealogy.application.ports.in.GenealogyQueryUseCase;
import com.devmaster.goatfarm.goat.application.ports.in.GoatGenealogyReadUseCase;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.business.mapper.GenealogyBusinessMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenealogyBusiness implements GenealogyQueryUseCase {

    private final GoatGenealogyReadUseCase goatGenealogyReadUseCase;
    private final GenealogyBusinessMapper genealogyMapper;

    public GenealogyBusiness(GoatGenealogyReadUseCase goatGenealogyReadUseCase, GenealogyBusinessMapper genealogyMapper) {
        this.goatGenealogyReadUseCase = goatGenealogyReadUseCase;
        this.genealogyMapper = genealogyMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public GenealogyResponseVO findGenealogy(Long farmId, String goatId) {
        return goatGenealogyReadUseCase.findGenealogyByRegistrationNumberAndFarmId(goatId, farmId)
                .map(genealogyMapper::toResponseVO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cabra não encontrada ou não pertence à fazenda informada: " + goatId));
    }
}
