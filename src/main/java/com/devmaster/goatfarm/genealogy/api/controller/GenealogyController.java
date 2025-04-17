package com.devmaster.goatfarm.genealogy.api.controller;


import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.dao.GenealogyDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GenealogyController {

    @Autowired
    private GenealogyDAO genealogyDAO;

    @GetMapping("/genealogy/{goatRegistrationNumber}")
    public GenealogyResponseVO getGenealogy(@PathVariable String goatRegistrationNumber) {
        return genealogyDAO.buildGenealogy(goatRegistrationNumber);
    }
}