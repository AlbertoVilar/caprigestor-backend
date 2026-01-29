package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.application.ports.in.GenealogyQueryUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatGenealogyQueryPort;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.mapper.GenealogyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenealogyBusiness implements GenealogyQueryUseCase {

    private final GoatGenealogyQueryPort goatGenealogyQueryPort;
    private final GenealogyMapper genealogyMapper;

    public GenealogyBusiness(GoatGenealogyQueryPort goatGenealogyQueryPort, GenealogyMapper genealogyMapper) {
        this.goatGenealogyQueryPort = goatGenealogyQueryPort;
        this.genealogyMapper = genealogyMapper;
    }

    @Transactional(readOnly = true)
    @Override
    public GenealogyResponseVO findGenealogy(Long farmId, String goatId) {
        return goatGenealogyQueryPort.findByIdAndFarmIdWithFamilyGraph(goatId, farmId)
                .map(genealogyMapper::toResponseVO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cabra não encontrada ou não pertence à fazenda informada: " + goatId));
    }
}
