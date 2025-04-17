package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.dao.GenealogyDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenealogyBusiness {

    @Autowired
    private GenealogyDAO genealogyDAO;

    public GenealogyResponseVO buildGenealogy(String goatRegistrationNumber) {

        return genealogyDAO.buildGenealogy(goatRegistrationNumber);
    }
}
