package com.devmaster.goatfarm.genealogy.dao;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.converter.GenealogyConverter;
import com.devmaster.goatfarm.genealogy.model.repository.GenealogyRepository;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenealogyDAO {

    @Autowired
    private GenealogyRepository genealogyRepository;

    @Autowired
    private GoatRepository goatRepository;

    @Autowired
    private GenealogyConverter buildGenealogyMapper;

    @Transactional
    public GenealogyResponseVO findGenealogy(String goatRegistrationNumber) {

        Goat goat = goatRepository.findByRegistrationNumber(goatRegistrationNumber)
                .orElseThrow(() -> new RuntimeException("Animal not found: " + goatRegistrationNumber));

        GenealogyResponseVO vo = buildGenealogyMapper.buildGenealogyFromGoat(goat);

        return vo;
    }
/*
    @Transactional
    public GenealogyResponseVO getGenealogy(String goatRegistrationNumber) {

        Goat goat = goatRepository.findByRegistrationNumber(goatRegistrationNumber)
                .orElseThrow(() -> new RuntimeException("Animal not found: " + goatRegistrationNumber));

        GenealogyResponseVO vo = buildGenealogyMapper.buildGenealogyFromGoat(goat);

        return vo;
    }

 */

}
