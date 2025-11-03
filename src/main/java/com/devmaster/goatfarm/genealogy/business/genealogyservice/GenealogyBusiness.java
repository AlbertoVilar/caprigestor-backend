package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.dao.GenealogyDAO;
import com.devmaster.goatfarm.genealogy.mapper.GenealogyMapper;
import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import com.devmaster.goatfarm.goat.dao.GoatDAO;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenealogyBusiness {

    @Autowired
    private GenealogyDAO genealogyDAO;

    @Autowired
    private GoatDAO goatDAO;

    @Autowired
    private GenealogyMapper genealogyMapper;

    @Transactional(readOnly = true)
    public GenealogyResponseVO findGenealogy(String goatRegistrationNumber) {
        return genealogyDAO
                .findByGoatRegistration(goatRegistrationNumber)
                .map(genealogyMapper::toResponseVO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Genealogia não encontrada com o número de registro: " + goatRegistrationNumber));
    }

    @Transactional
    public GenealogyResponseVO createGenealogy(String goatRegistrationNumber) {
        if (genealogyDAO.existsByGoatRegistration(goatRegistrationNumber)) {
            throw new DatabaseException("Genealogia já existe para o animal: " + goatRegistrationNumber);
        }

        Goat goat = goatDAO.findByRegistrationNumber(goatRegistrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + goatRegistrationNumber));

        final Genealogy entity = genealogyMapper.toEntity(goat);
        genealogyDAO.save(entity);
        return genealogyMapper.toResponseVO(entity);
    }

    @Transactional
    public GenealogyResponseVO createGenealogyWithData(GenealogyRequestDTO requestDTO) {
        if (genealogyDAO.existsByGoatRegistration(requestDTO.getGoatRegistration())) {
            throw new DatabaseException("Genealogia já existe para o animal: " + requestDTO.getGoatRegistration());
        }
        Genealogy entity = genealogyMapper.toEntity(requestDTO);
        try {
            entity = genealogyDAO.save(entity);
        } catch (Exception e) {
            throw new DatabaseException("Erro ao persistir genealogia: " + e.getMessage());
        }
        return genealogyMapper.toResponseVO(entity);
    }
}
