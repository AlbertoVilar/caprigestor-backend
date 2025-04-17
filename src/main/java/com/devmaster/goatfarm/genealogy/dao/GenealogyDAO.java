package com.devmaster.goatfarm.genealogy.dao;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GenealogyDAO {

    @Autowired
    private GoatRepository goatRepository;

    @Transactional
    public GenealogyResponseVO buildGenealogy(String goatRegistrationNumber) {

        Goat goat = goatRepository.findByRegistrationNumber(goatRegistrationNumber)
                .orElseThrow(() -> new RuntimeException("Animal not found: " + goatRegistrationNumber));

        GenealogyResponseVO vo = new GenealogyResponseVO();

        vo.setGoatName(goat.getName());
        vo.setGoatRegistration(goat.getRegistrationNumber());
        vo.setBreeder(goat.getFarm() != null ? goat.getFarm().getName() : null);
        vo.setOwner(goat.getOwner() != null ? goat.getOwner().getName() : null);
        vo.setBreed(goat.getBreed() != null ? goat.getBreed().name() : null);
        vo.setColor(goat.getColor());
        vo.setStatus(goat.getStatus() != null ? goat.getStatus().name() : null);
        vo.setGender(goat.getGender() != null ? goat.getGender().name() : null);
        vo.setCategory(goat.getCategory() != null ? goat.getCategory().name() : null);
        vo.setTod(goat.getTod());
        vo.setToe(goat.getToe());
        vo.setBirthDate(goat.getBirthDate() != null ? goat.getBirthDate().toString() : null);

        // Father
        Goat father = goat.getFather();
        if (father != null) {
            vo.setFatherName(father.getName());
            vo.setFatherRegistration(father.getRegistrationNumber());

            Goat paternalGrandfather = father.getFather();
            if (paternalGrandfather != null) {
                vo.setPaternalGrandfatherName(paternalGrandfather.getName());
                vo.setPaternalGrandfatherRegistration(paternalGrandfather.getRegistrationNumber());

                Goat paternalGreatGrandfather1 = paternalGrandfather.getFather();
                if (paternalGreatGrandfather1 != null) {
                    vo.setPaternalGreatGrandfather1Name(paternalGreatGrandfather1.getName());
                    vo.setPaternalGreatGrandfather1Registration(paternalGreatGrandfather1.getRegistrationNumber());
                }
                Goat paternalGreatGrandmother1 = paternalGrandfather.getMother();
                if (paternalGreatGrandmother1 != null) {
                    vo.setPaternalGreatGrandmother1Name(paternalGreatGrandmother1.getName());
                    vo.setPaternalGreatGrandmother1Registration(paternalGreatGrandmother1.getRegistrationNumber());
                }
            }

            Goat paternalGrandmother = father.getMother();
            if (paternalGrandmother != null) {
                vo.setPaternalGrandmotherName(paternalGrandmother.getName());
                vo.setPaternalGrandmotherRegistration(paternalGrandmother.getRegistrationNumber());

                Goat paternalGreatGrandfather2 = paternalGrandmother.getFather();
                if (paternalGreatGrandfather2 != null) {
                    vo.setPaternalGreatGrandfather2Name(paternalGreatGrandfather2.getName());
                    vo.setPaternalGreatGrandfather2Registration(paternalGreatGrandfather2.getRegistrationNumber());
                }
                Goat paternalGreatGrandmother2 = paternalGrandmother.getMother();
                if (paternalGreatGrandmother2 != null) {
                    vo.setPaternalGreatGrandmother2Name(paternalGreatGrandmother2.getName());
                    vo.setPaternalGreatGrandmother2Registration(paternalGreatGrandmother2.getRegistrationNumber());
                }
            }
        }

        // Mother
        Goat mother = goat.getMother();
        if (mother != null) {
            vo.setMotherName(mother.getName());
            vo.setMotherRegistration(mother.getRegistrationNumber());

            Goat maternalGrandfather = mother.getFather();
            if (maternalGrandfather != null) {
                vo.setMaternalGrandfatherName(maternalGrandfather.getName());
                vo.setMaternalGrandfatherRegistration(maternalGrandfather.getRegistrationNumber());

                Goat maternalGreatGrandfather1 = maternalGrandfather.getFather();
                if (maternalGreatGrandfather1 != null) {
                    vo.setMaternalGreatGrandfather1Name(maternalGreatGrandfather1.getName());
                    vo.setMaternalGreatGrandfather1Registration(maternalGreatGrandfather1.getRegistrationNumber());
                }
                Goat maternalGreatGrandmother1 = maternalGrandfather.getMother();
                if (maternalGreatGrandmother1 != null) {
                    vo.setMaternalGreatGrandmother1Name(maternalGreatGrandmother1.getName());
                    vo.setMaternalGreatGrandmother1Registration(maternalGreatGrandmother1.getRegistrationNumber());
                }
            }

            Goat maternalGrandmother = mother.getMother();
            if (maternalGrandmother != null) {
                vo.setMaternalGrandmotherName(maternalGrandmother.getName());
                vo.setMaternalGrandmotherRegistration(maternalGrandmother.getRegistrationNumber());

                Goat maternalGreatGrandfather2 = maternalGrandmother.getFather();
                if (maternalGreatGrandfather2 != null) {
                    vo.setMaternalGreatGrandfather2Name(maternalGreatGrandfather2.getName());
                    vo.setMaternalGreatGrandfather2Registration(maternalGreatGrandfather2.getRegistrationNumber());
                }
                Goat maternalGreatGrandmother2 = maternalGrandmother.getMother();
                if (maternalGreatGrandmother2 != null) {
                    vo.setMaternalGreatGrandmother2Name(maternalGreatGrandmother2.getName());
                    vo.setMaternalGreatGrandmother2Registration(maternalGreatGrandmother2.getRegistrationNumber());
                }
            }
        }

        return vo;
    }
}
