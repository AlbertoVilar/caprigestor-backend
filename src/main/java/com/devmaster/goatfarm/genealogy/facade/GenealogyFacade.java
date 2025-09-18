package com.devmaster.goatfarm.genealogy.facade;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.business.genealogyservice.GenealogyBusiness;
import com.devmaster.goatfarm.genealogy.facade.dto.GenealogyFacadeResponseDTO;
import com.devmaster.goatfarm.genealogy.facade.mapper.GenealogyFacadeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenealogyFacade {

    @Autowired
    private GenealogyBusiness genealogyBusiness;
    
    @Autowired
    private GenealogyFacadeMapper facadeMapper;

    public GenealogyFacadeResponseDTO findGenealogy(String goatRegistrationNumber) {
        GenealogyResponseVO responseVO = genealogyBusiness.findGenealogy(goatRegistrationNumber);
        return facadeMapper.toFacadeDTO(responseVO);
    }

    @Transactional
    public GenealogyFacadeResponseDTO createGenealogy(String goatRegistrationNumber) {
        GenealogyResponseVO responseVO = genealogyBusiness.createGenealogy(goatRegistrationNumber);
        return facadeMapper.toFacadeDTO(responseVO);
    }

    @Transactional
    public GenealogyFacadeResponseDTO createGenealogyWithData(GenealogyRequestDTO requestDTO) {
        GenealogyResponseVO responseVO = genealogyBusiness.createGenealogyWithData(requestDTO);
        return facadeMapper.toFacadeDTO(responseVO);
    }
}


