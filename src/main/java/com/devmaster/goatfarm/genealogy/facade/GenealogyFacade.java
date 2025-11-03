package com.devmaster.goatfarm.genealogy.facade;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.api.dto.GenealogyResponseDTO;
import com.devmaster.goatfarm.genealogy.business.genealogyservice.GenealogyBusiness;
import com.devmaster.goatfarm.genealogy.mapper.GenealogyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenealogyFacade {

    @Autowired
    private GenealogyBusiness genealogyBusiness;

    @Autowired
    private GenealogyMapper genealogyMapper;

    public GenealogyResponseDTO findGenealogy(Long farmId, String goatId) {
        return genealogyMapper.toResponseDTO(genealogyBusiness.findGenealogy(farmId, goatId));
    }

    public GenealogyResponseDTO createGenealogy(Long farmId, String goatId) {
        return genealogyMapper.toResponseDTO(genealogyBusiness.createGenealogy(farmId, goatId));
    }

    public GenealogyResponseDTO createGenealogyWithData(Long farmId, String goatId, GenealogyRequestDTO requestDTO) {
        return genealogyMapper.toResponseDTO(genealogyBusiness.createGenealogyWithData(farmId, goatId, requestDTO));
    }
}
