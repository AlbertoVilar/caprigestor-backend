package com.devmaster.goatfarm.genealogy.dao;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.converter.GenealogyConverter; // Remova esta importação
import com.devmaster.goatfarm.genealogy.converter.GenealogyEntityConverter; // Importe o novo converter
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
    private GenealogyConverter buildGenealogyMapper; // Mantemos este para a montagem inicial

    @Transactional
    private GenealogyResponseVO buildGenealogy(String goatRegistrationNumber) {
        try {
            Goat goat = goatRepository.findByRegistrationNumber(goatRegistrationNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado com o número de registro: " + goatRegistrationNumber));
            return buildGenealogyMapper.buildGenealogyFromGoat(goat);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Erro inesperado ao construir a genealogia para o animal " + goatRegistrationNumber + ": " + e.getMessage());
        }
    }

    @Transactional
    public GenealogyResponseVO findGenealogy(String goatRegistrationNumber) {
        try {
            Goat goat = goatRepository.findByRegistrationNumber(goatRegistrationNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado com o número de registro: " + goatRegistrationNumber));

            Optional<Genealogy> genealogyOptional = genealogyRepository.findByGoatRegistration(goatRegistrationNumber);

            return genealogyOptional.map(GenealogyEntityConverter::toResponseVO).orElse(null);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Erro inesperado ao buscar a genealogia do animal " + goatRegistrationNumber + ": " + e.getMessage());
        }
    }

    @Transactional
    public GenealogyResponseVO createGenealogy(String goatRegistrationNumber) {
        try {
            Goat goat = goatRepository.findByRegistrationNumber(goatRegistrationNumber)
                    .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado com o número de registro: " + goatRegistrationNumber));

            if (genealogyRepository.existsByGoatRegistration(goatRegistrationNumber)) {
                throw new DatabaseException("A genealogia para o animal " + goatRegistrationNumber + " já existe.");
            }

            GenealogyResponseVO genealogyVO = buildGenealogy(goatRegistrationNumber);
            Genealogy genealogyEntity = GenealogyEntityConverter.toEntity(genealogyVO); // Usando o novo converter

            genealogyRepository.save(genealogyEntity);
            return genealogyVO;

        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao salvar a genealogia do animal " + goatRegistrationNumber + ": " + e.getMessage());
        } catch (ResourceNotFoundException e) {
            throw e; // Re-lança a exceção customizada, pois ela já está no formato desejado
        } catch (Exception e) {
            throw new DatabaseException("Erro inesperado ao criar a genealogia do animal " + goatRegistrationNumber);
        }
    }
}