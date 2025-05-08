package com.devmaster.goatfarm.genealogy.dao;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.converter.GenealogyEntityConverter;
import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import com.devmaster.goatfarm.genealogy.model.repository.GenealogyRepository;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GenealogyDAO {

    @Autowired
    private GenealogyRepository genealogyRepository;

    @Autowired
    private GoatRepository goatRepository;

    @Autowired
    private com.devmaster.goatfarm.genealogy.converter.GenealogyConverter buildGenealogyMapper; // Utilizado apenas para construção inicial da genealogia

    @Transactional
    private GenealogyResponseVO buildGenealogy(String goatRegistrationNumber) {
        try {
            final Goat goat = goatRepository.findByRegistrationNumber(goatRegistrationNumber)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Animal não encontrado com o número de registro: " + goatRegistrationNumber));
            return buildGenealogyMapper.buildGenealogyFromGoat(goat);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Erro ao construir a genealogia do animal " + goatRegistrationNumber + ": " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public GenealogyResponseVO findGenealogy(String goatRegistrationNumber) {
        try {
            final Optional<Genealogy> genealogyOptional = genealogyRepository.findByGoatRegistration(goatRegistrationNumber);

            return genealogyOptional.map(GenealogyEntityConverter::toResponseVO)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Genealogia não encontrada com o número de registro: " + goatRegistrationNumber));

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Erro ao buscar a genealogia do animal " + goatRegistrationNumber + ": " + e.getMessage());
        }
    }

    @Transactional
    public GenealogyResponseVO createGenealogy(String goatRegistrationNumber) {
        try {
            final Goat goat = goatRepository.findByRegistrationNumber(goatRegistrationNumber)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Animal não encontrado com o número de registro: " + goatRegistrationNumber));

            if (genealogyRepository.existsByGoatRegistration(goatRegistrationNumber)) {
                throw new DatabaseException("A genealogia do animal " + goatRegistrationNumber + " já existe.");
            }

            final GenealogyResponseVO genealogyVO = buildGenealogy(goatRegistrationNumber);
            final Genealogy genealogyEntity = GenealogyEntityConverter.toEntity(genealogyVO);

            genealogyRepository.save(genealogyEntity);
            return genealogyVO;

        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Violação de integridade ao salvar a genealogia do animal " + goatRegistrationNumber + ": " + e.getMessage());
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Erro ao criar a genealogia do animal " + goatRegistrationNumber + ": " + e.getMessage());
        }
    }
}
