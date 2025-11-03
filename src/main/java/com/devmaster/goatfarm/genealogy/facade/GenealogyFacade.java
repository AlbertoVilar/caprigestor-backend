package com.devmaster.goatfarm.genealogy.facade;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.api.dto.GenealogyResponseDTO;
import com.devmaster.goatfarm.genealogy.business.genealogyservice.GenealogyBusiness;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.mapper.GenealogyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenealogyFacade {

    @Autowired
    private GenealogyBusiness genealogyBusiness;

    @Autowired
    private GenealogyMapper genealogyMapper;

    public GenealogyResponseDTO findGenealogy(String goatRegistrationNumber) {
        GenealogyResponseVO responseVO = genealogyBusiness.findGenealogy(goatRegistrationNumber);
        return genealogyMapper.toResponseDTO(responseVO);
    }

    public GenealogyResponseDTO createGenealogy(String goatRegistrationNumber) {
        GenealogyResponseVO responseVO = genealogyBusiness.createGenealogy(goatRegistrationNumber);
        return genealogyMapper.toResponseDTO(responseVO);
    }

    public GenealogyResponseDTO createGenealogyWithData(GenealogyRequestDTO requestDTO) {
        GenealogyResponseVO responseVO = genealogyBusiness.createGenealogyWithData(requestDTO);
        return genealogyMapper.toResponseDTO(responseVO);
    }
}
