package com.devmaster.goatfarm.genealogy.business.genealogyservice;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.mapper.GenealogyMapper;
import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import com.devmaster.goatfarm.genealogy.model.repository.GenealogyRepository;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenealogyBusiness {

    @Autowired
    private GenealogyRepository genealogyRepository;

    @Autowired
    private GoatRepository goatRepository;

    @Autowired
    private GenealogyMapper genealogyMapper;

    @Transactional(readOnly = true)
    public GenealogyResponseVO findGenealogy(String goatRegistrationNumber) {
        return genealogyRepository
                .findByGoatRegistration(goatRegistrationNumber)
                .map(genealogyMapper::toResponseVO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Genealogia não encontrada com o número de registro: " + goatRegistrationNumber));
    }

    @Transactional
    public GenealogyResponseVO createGenealogy(String goatRegistrationNumber) {
        if (genealogyRepository.existsByGoatRegistration(goatRegistrationNumber)) {
            throw new DatabaseException("Genealogia já existe para o animal: " + goatRegistrationNumber);
        }

        Goat goat = goatRepository.findByRegistrationNumber(goatRegistrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Animal não encontrado: " + goatRegistrationNumber));

        final Genealogy entity = buildGenealogyFromGoat(goat);
        genealogyRepository.save(entity);
        return genealogyMapper.toResponseVO(entity);
    }

    @Transactional
    public GenealogyResponseVO createGenealogyWithData(GenealogyRequestDTO requestDTO) {
        if (genealogyRepository.existsByGoatRegistration(requestDTO.getGoatRegistration())) {
            throw new DatabaseException("Genealogia já existe para o animal: " + requestDTO.getGoatRegistration());
        }
        Genealogy entity = genealogyMapper.toEntity(requestDTO);
        try {
            entity = genealogyRepository.save(entity);
        } catch (Exception e) {
            throw new DatabaseException("Erro ao persistir genealogia: " + e.getMessage());
        }
        return genealogyMapper.toResponseVO(entity);
    }

    private Genealogy buildGenealogyFromGoat(Goat goat) {
        Genealogy genealogy = new Genealogy();
        try {
            genealogy.setGoatName(goat.getName());
            genealogy.setGoatRegistration(goat.getRegistrationNumber());
            genealogy.setGoatBreed(goat.getBreed() != null ? goat.getBreed().toString() : null);
            genealogy.setGoatCoatColor(goat.getColor());
            genealogy.setGoatStatus(goat.getStatus() != null ? goat.getStatus().toString() : null);
            genealogy.setGoatSex(goat.getGender() != null ? goat.getGender().toString() : null);
            genealogy.setGoatCategory(goat.getCategory() != null ? goat.getCategory().toString() : null);
            genealogy.setGoatTOD(goat.getTod());
            genealogy.setGoatTOE(goat.getToe());
            genealogy.setGoatBirthDate(goat.getBirthDate() != null ? goat.getBirthDate().toString() : null);

            if (goat.getUser() != null) {
                genealogy.setGoatCreator(goat.getUser().getName());
            }
            if (goat.getFarm() != null && goat.getFarm().getUser() != null) {
                genealogy.setGoatOwner(goat.getFarm().getUser().getName());
            }

            if (goat.getFather() != null) {
                genealogy.setFatherName(goat.getFather().getName());
                genealogy.setFatherRegistration(goat.getFather().getRegistrationNumber());
                if (goat.getFather().getFather() != null) {
                    genealogy.setPaternalGrandfatherName(goat.getFather().getFather().getName());
                    genealogy.setPaternalGrandfatherRegistration(goat.getFather().getFather().getRegistrationNumber());
                    if (goat.getFather().getFather().getFather() != null) {
                        genealogy.setPaternalGreatGrandfather1Name(goat.getFather().getFather().getFather().getName());
                        genealogy.setPaternalGreatGrandfather1Registration(goat.getFather().getFather().getFather().getRegistrationNumber());
                    }
                    if (goat.getFather().getFather().getMother() != null) {
                        genealogy.setPaternalGreatGrandmother1Name(goat.getFather().getFather().getMother().getName());
                        genealogy.setPaternalGreatGrandmother1Registration(goat.getFather().getFather().getMother().getRegistrationNumber());
                    }
                }
                if (goat.getFather().getMother() != null) {
                    genealogy.setPaternalGrandmotherName(goat.getFather().getMother().getName());
                    genealogy.setPaternalGrandmotherRegistration(goat.getFather().getMother().getRegistrationNumber());
                    if (goat.getFather().getMother().getFather() != null) {
                        genealogy.setPaternalGreatGrandfather2Name(goat.getFather().getMother().getFather().getName());
                        genealogy.setPaternalGreatGrandfather2Registration(goat.getFather().getMother().getFather().getRegistrationNumber());
                    }
                    if (goat.getFather().getMother().getMother() != null) {
                        genealogy.setPaternalGreatGrandmother2Name(goat.getFather().getMother().getMother().getName());
                        genealogy.setPaternalGreatGrandmother2Registration(goat.getFather().getMother().getMother().getRegistrationNumber());
                    }
                }
            }

            if (goat.getMother() != null) {
                genealogy.setMotherName(goat.getMother().getName());
                genealogy.setMotherRegistration(goat.getMother().getRegistrationNumber());
                if (goat.getMother().getFather() != null) {
                    genealogy.setMaternalGrandfatherName(goat.getMother().getFather().getName());
                    genealogy.setMaternalGrandfatherRegistration(goat.getMother().getFather().getRegistrationNumber());
                    if (goat.getMother().getFather().getFather() != null) {
                        genealogy.setMaternalGreatGrandfather1Name(goat.getMother().getFather().getFather().getName());
                        genealogy.setMaternalGreatGrandfather1Registration(goat.getMother().getFather().getFather().getRegistrationNumber());
                    }
                    if (goat.getMother().getFather().getMother() != null) {
                        genealogy.setMaternalGreatGrandmother1Name(goat.getMother().getFather().getMother().getName());
                        genealogy.setMaternalGreatGrandmother1Registration(goat.getMother().getFather().getMother().getRegistrationNumber());
                    }
                }
                if (goat.getMother().getMother() != null) {
                    genealogy.setMaternalGrandmotherName(goat.getMother().getMother().getName());
                    genealogy.setMaternalGrandmotherRegistration(goat.getMother().getMother().getRegistrationNumber());
                    if (goat.getMother().getMother().getFather() != null) {
                        genealogy.setMaternalGreatGrandfather2Name(goat.getMother().getMother().getFather().getName());
                        genealogy.setMaternalGreatGrandfather2Registration(goat.getMother().getMother().getFather().getRegistrationNumber());
                    }
                    if (goat.getMother().getMother().getMother() != null) {
                        genealogy.setMaternalGreatGrandmother2Name(goat.getMother().getMother().getMother().getName());
                        genealogy.setMaternalGreatGrandmother2Registration(goat.getMother().getMother().getMother().getRegistrationNumber());
                    }
                }
            }

            return genealogy;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao construir genealogia: " + e.getMessage(), e);
        }
    }
}
