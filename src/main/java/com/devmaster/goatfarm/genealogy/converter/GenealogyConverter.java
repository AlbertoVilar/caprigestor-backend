package com.devmaster.goatfarm.genealogy.converter;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class BuildGenealogyMapper {

    //Building Genealogy Response to use in DAO
    @Transactional
    public GenealogyResponseVO buildGenealogyFromGoat(Goat goat) {
        GenealogyResponseVO genealogy = new GenealogyResponseVO();

        genealogy.setGoatName(goat.getName());
        genealogy.setGoatRegistration(goat.getRegistrationNumber());
        genealogy.setBreeder(goat.getFarm() != null ? goat.getFarm().getName() : null);
        genealogy.setOwner(goat.getOwner() != null ? goat.getOwner().getName() : null);
        genealogy.setBreed(goat.getBreed() != null ? goat.getBreed().name() : null);
        genealogy.setColor(goat.getColor());
        genealogy.setStatus(goat.getStatus() != null ? goat.getStatus().name() : null);
        genealogy.setGender(goat.getGender() != null ? goat.getGender().name() : null);
        genealogy.setCategory(goat.getCategory() != null ? goat.getCategory().name() : null);
        genealogy.setTod(goat.getTod());
        genealogy.setToe(goat.getToe());
        genealogy.setBirthDate(goat.getBirthDate() != null ? goat.getBirthDate().toString() : null);

        // Father side
        Goat father = goat.getFather();
        if (father != null) {
            genealogy.setFatherName(father.getName());
            genealogy.setFatherRegistration(father.getRegistrationNumber());

            Goat paternalGrandfather = father.getFather();
            if (paternalGrandfather != null) {
                genealogy.setPaternalGrandfatherName(paternalGrandfather.getName());
                genealogy.setPaternalGrandfatherRegistration(paternalGrandfather.getRegistrationNumber());

                Goat ggFather1 = paternalGrandfather.getFather();
                if (ggFather1 != null) {
                    genealogy.setPaternalGreatGrandfather1Name(ggFather1.getName());
                    genealogy.setPaternalGreatGrandfather1Registration(ggFather1.getRegistrationNumber());
                }

                Goat ggMother1 = paternalGrandfather.getMother();
                if (ggMother1 != null) {
                    genealogy.setPaternalGreatGrandmother1Name(ggMother1.getName());
                    genealogy.setPaternalGreatGrandmother1Registration(ggMother1.getRegistrationNumber());
                }
            }

            Goat paternalGrandmother = father.getMother();
            if (paternalGrandmother != null) {
                genealogy.setPaternalGrandmotherName(paternalGrandmother.getName());
                genealogy.setPaternalGrandmotherRegistration(paternalGrandmother.getRegistrationNumber());

                Goat ggFather2 = paternalGrandmother.getFather();
                if (ggFather2 != null) {
                    genealogy.setPaternalGreatGrandfather2Name(ggFather2.getName());
                    genealogy.setPaternalGreatGrandfather2Registration(ggFather2.getRegistrationNumber());
                }

                Goat ggMother2 = paternalGrandmother.getMother();
                if (ggMother2 != null) {
                    genealogy.setPaternalGreatGrandmother2Name(ggMother2.getName());
                    genealogy.setPaternalGreatGrandmother2Registration(ggMother2.getRegistrationNumber());
                }
            }
        }

        // Mother side
        Goat mother = goat.getMother();
        if (mother != null) {
            genealogy.setMotherName(mother.getName());
            genealogy.setMotherRegistration(mother.getRegistrationNumber());

            Goat maternalGrandfather = mother.getFather();
            if (maternalGrandfather != null) {
                genealogy.setMaternalGrandfatherName(maternalGrandfather.getName());
                genealogy.setMaternalGrandfatherRegistration(maternalGrandfather.getRegistrationNumber());

                Goat ggFather1 = maternalGrandfather.getFather();
                if (ggFather1 != null) {
                    genealogy.setMaternalGreatGrandfather1Name(ggFather1.getName());
                    genealogy.setMaternalGreatGrandfather1Registration(ggFather1.getRegistrationNumber());
                }

                Goat ggMother1 = maternalGrandfather.getMother();
                if (ggMother1 != null) {
                    genealogy.setMaternalGreatGrandmother1Name(ggMother1.getName());
                    genealogy.setMaternalGreatGrandmother1Registration(ggMother1.getRegistrationNumber());
                }
            }

            Goat maternalGrandmother = mother.getMother();
            if (maternalGrandmother != null) {
                genealogy.setMaternalGrandmotherName(maternalGrandmother.getName());
                genealogy.setMaternalGrandmotherRegistration(maternalGrandmother.getRegistrationNumber());

                Goat ggFather2 = maternalGrandmother.getFather();
                if (ggFather2 != null) {
                    genealogy.setMaternalGreatGrandfather2Name(ggFather2.getName());
                    genealogy.setMaternalGreatGrandfather2Registration(ggFather2.getRegistrationNumber());
                }

                Goat ggMother2 = maternalGrandmother.getMother();
                if (ggMother2 != null) {
                    genealogy.setMaternalGreatGrandmother2Name(ggMother2.getName());
                    genealogy.setMaternalGreatGrandmother2Registration(ggMother2.getRegistrationNumber());
                }
            }
        }

        return genealogy;
    }
}
