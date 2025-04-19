package com.devmaster.goatfarm.genealogy.converter;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.springframework.stereotype.Service;

@Service
public class GenealogyConverter {

    // Método principal para construir a genealogia a partir da entidade Goat
    public GenealogyResponseVO buildGenealogyFromGoat(Goat goat) {
        GenealogyResponseVO genealogy = new GenealogyResponseVO();

        // Preenchendo os dados básicos da cabra
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

        // Preenchendo pai e mãe
        if (goat.getFather() != null) {
            genealogy.setFatherName(goat.getFather().getName());
            genealogy.setFatherRegistration(goat.getFather().getRegistrationNumber());
            fillGenealogySide(goat.getFather(), genealogy, "paternal");
        }

        if (goat.getMother() != null) {
            genealogy.setMotherName(goat.getMother().getName());
            genealogy.setMotherRegistration(goat.getMother().getRegistrationNumber());
            fillGenealogySide(goat.getMother(), genealogy, "maternal");
        }

        return genealogy;
    }

    // Método para preencher os avós e bisavós de um lado (paterno ou materno)
    private void fillGenealogySide(Goat parent, GenealogyResponseVO genealogy, String side) {
        // Avô
        Goat grandfather = parent.getFather();
        if (grandfather != null) {
            if (side.equals("paternal")) {
                genealogy.setPaternalGrandfatherName(grandfather.getName());
                genealogy.setPaternalGrandfatherRegistration(grandfather.getRegistrationNumber());
                fillGreatGrandparents(grandfather, genealogy, side, 1);
            } else {
                genealogy.setMaternalGrandfatherName(grandfather.getName());
                genealogy.setMaternalGrandfatherRegistration(grandfather.getRegistrationNumber());
                fillGreatGrandparents(grandfather, genealogy, side, 1);
            }
        }

        // Avó
        Goat grandmother = parent.getMother();
        if (grandmother != null) {
            if (side.equals("paternal")) {
                genealogy.setPaternalGrandmotherName(grandmother.getName());
                genealogy.setPaternalGrandmotherRegistration(grandmother.getRegistrationNumber());
                fillGreatGrandparents(grandmother, genealogy, side, 2);
            } else {
                genealogy.setMaternalGrandmotherName(grandmother.getName());
                genealogy.setMaternalGrandmotherRegistration(grandmother.getRegistrationNumber());
                fillGreatGrandparents(grandmother, genealogy, side, 2);
            }
        }
    }

    // Método para preencher bisavós, distinguindo entre avô (index 1) e avó (index 2)
    private void fillGreatGrandparents(Goat grandparent, GenealogyResponseVO genealogy, String side, int index) {
        Goat greatGrandfather = grandparent.getFather();
        Goat greatGrandmother = grandparent.getMother();

        if ("paternal".equals(side)) {
            if (index == 1) {
                if (greatGrandfather != null) {
                    genealogy.setPaternalGreatGrandfather1Name(greatGrandfather.getName());
                    genealogy.setPaternalGreatGrandfather1Registration(greatGrandfather.getRegistrationNumber());
                }
                if (greatGrandmother != null) {
                    genealogy.setPaternalGreatGrandmother1Name(greatGrandmother.getName());
                    genealogy.setPaternalGreatGrandmother1Registration(greatGrandmother.getRegistrationNumber());
                }
            } else {
                if (greatGrandfather != null) {
                    genealogy.setPaternalGreatGrandfather2Name(greatGrandfather.getName());
                    genealogy.setPaternalGreatGrandfather2Registration(greatGrandfather.getRegistrationNumber());
                }
                if (greatGrandmother != null) {
                    genealogy.setPaternalGreatGrandmother2Name(greatGrandmother.getName());
                    genealogy.setPaternalGreatGrandmother2Registration(greatGrandmother.getRegistrationNumber());
                }
            }
        } else if ("maternal".equals(side)) {
            if (index == 1) {
                if (greatGrandfather != null) {
                    genealogy.setMaternalGreatGrandfather1Name(greatGrandfather.getName());
                    genealogy.setMaternalGreatGrandfather1Registration(greatGrandfather.getRegistrationNumber());
                }
                if (greatGrandmother != null) {
                    genealogy.setMaternalGreatGrandmother1Name(greatGrandmother.getName());
                    genealogy.setMaternalGreatGrandmother1Registration(greatGrandmother.getRegistrationNumber());
                }
            } else {
                if (greatGrandfather != null) {
                    genealogy.setMaternalGreatGrandfather2Name(greatGrandfather.getName());
                    genealogy.setMaternalGreatGrandfather2Registration(greatGrandfather.getRegistrationNumber());
                }
                if (greatGrandmother != null) {
                    genealogy.setMaternalGreatGrandmother2Name(greatGrandmother.getName());
                    genealogy.setMaternalGreatGrandmother2Registration(greatGrandmother.getRegistrationNumber());
                }
            }
        }
    }
}
