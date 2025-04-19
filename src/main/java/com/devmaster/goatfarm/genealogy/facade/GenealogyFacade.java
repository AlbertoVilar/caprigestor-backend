package com.devmaster.goatfarm.genealogy.facade;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.business.genealogyservice.GenealogyBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenealogyFacade {

    @Autowired
    private GenealogyBusiness genealogyBusiness;

    public GenealogyResponseVO findGenealogy(String goatRegistrationNumber) {

        return genealogyBusiness.findGenealogy(goatRegistrationNumber);
    }
}


