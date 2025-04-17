package com.devmaster.goatfarm.genealogy.converter;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class GenealogyConverter {

    // Método auxiliar para preencher os avós e bisavós
    private void fillGrandparentsAndGreatGrandparents(Goat goat, GenealogyResponseVO genealogy, String side) {
        Goat parent = side.equals("father") ? goat.getFather() : goat.getMother();

        if (parent != null) {
            // Avós
            Goat grandfather = parent.getFather();
            if (grandfather != null) {
                if (side.equals("father")) {
                    genealogy.setPaternalGrandfatherName(grandfather.getName());
                    genealogy.setPaternalGrandfatherRegistration(grandfather.getRegistrationNumber());
                } else {
                    genealogy.setMaternalGrandfatherName(grandfather.getName());
                    genealogy.setMaternalGrandfatherRegistration(grandfather.getRegistrationNumber());
                }

                fillGreatGrandparents(grandfather, genealogy, side, 1);
            }

            Goat grandmother = parent.getMother();
            if (grandmother != null) {
                if (side.equals("father")) {
                    genealogy.setPaternalGrandmotherName(grandmother.getName());
                    genealogy.setPaternalGrandmotherRegistration(grandmother.getRegistrationNumber());
                } else {
                    genealogy.setMaternalGrandmotherName(grandmother.getName());
                    genealogy.setMaternalGrandmotherRegistration(grandmother.getRegistrationNumber());
                }

                fillGreatGrandparents(grandmother, genealogy, side, 2);
            }
        }
    }

    // Método auxiliar para preencher os bisavós
    private void fillGreatGrandparents(Goat grandparent, GenealogyResponseVO genealogy, String side, int index) {
        Goat greatGrandfather = grandparent.getFather();
        if (greatGrandfather != null) {
            if (side.equals("father")) {
                if (index == 1) {
                    genealogy.setPaternalGreatGrandfather1Name(greatGrandfather.getName());
                    genealogy.setPaternalGreatGrandfather1Registration(greatGrandfather.getRegistrationNumber());
                } else {
                    genealogy.setPaternalGreatGrandfather2Name(greatGrandfather.getName());
                    genealogy.setPaternalGreatGrandfather2Registration(greatGrandfather.getRegistrationNumber());
                }
            } else {
                if (index == 1) {
                    genealogy.setMaternalGreatGrandfather1Name(greatGrandfather.getName());
                    genealogy.setMaternalGreatGrandfather1Registration(greatGrandfather.getRegistrationNumber());
                } else {
                    genealogy.setMaternalGreatGrandfather2Name(greatGrandfather.getName());
                    genealogy.setMaternalGreatGrandfather2Registration(greatGrandfather.getRegistrationNumber());
                }
            }
        }

        Goat greatGrandmother = grandparent.getMother();
        if (greatGrandmother != null) {
            if (side.equals("father")) {
                if (index == 1) {
                    genealogy.setPaternalGreatGrandmother1Name(greatGrandmother.getName());
                    genealogy.setPaternalGreatGrandmother1Registration(greatGrandmother.getRegistrationNumber());
                } else {
                    genealogy.setPaternalGreatGrandmother2Name(greatGrandmother.getName());
                    genealogy.setPaternalGreatGrandmother2Registration(greatGrandmother.getRegistrationNumber());
                }
            } else {
                if (index == 1) {
                    genealogy.setMaternalGreatGrandmother1Name(greatGrandmother.getName());
                    genealogy.setMaternalGreatGrandmother1Registration(greatGrandmother.getRegistrationNumber());
                } else {
                    genealogy.setMaternalGreatGrandmother2Name(greatGrandmother.getName());
                    genealogy.setMaternalGreatGrandmother2Registration(greatGrandmother.getRegistrationNumber());
                }
            }
        }
    }

    // Método para construir o pedigree completo
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


        fillGrandparentsAndGreatGrandparents(goat, genealogy, "father");
        fillGrandparentsAndGreatGrandparents(goat, genealogy, "mother");

        return genealogy;
    }
}
