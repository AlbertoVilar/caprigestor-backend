package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.dao.GenealogyDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenealogyBusiness {

    @Autowired
    private GenealogyDAO genealogyDAO;

    public GenealogyResponseVO findGenealogy(String goatRegistrationNumber) {

        return genealogyDAO.findGenealogy(goatRegistrationNumber);
    }


    @Transactional // Adicionando a anotação @Transactional
    public GenealogyResponseVO createGenealogy(String goatRegistrationNumber) {
        return genealogyDAO.createGenealogy(goatRegistrationNumber);
    }

    @Transactional
    public GenealogyResponseVO createGenealogyWithData(GenealogyRequestDTO requestDTO) {
        return genealogyDAO.createGenealogyWithData(requestDTO);
    }

}
