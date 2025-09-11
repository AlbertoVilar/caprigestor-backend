package com.devmaster.goatfarm.genealogy.facade;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.business.genealogyservice.GenealogyBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenealogyFacade {

    @Autowired
    private GenealogyBusiness genealogyBusiness;

    public GenealogyResponseVO findGenealogy(String goatRegistrationNumber) {

        return genealogyBusiness.findGenealogy(goatRegistrationNumber);
    }

    @Transactional
    public GenealogyResponseVO createGenealogy(String goatRegistrationNumber) {
        return genealogyBusiness.createGenealogy(goatRegistrationNumber);
    }

    @Transactional
    public GenealogyResponseVO createGenealogyWithData(GenealogyRequestDTO requestDTO) {
        return genealogyBusiness.createGenealogyWithData(requestDTO);
    }
}


