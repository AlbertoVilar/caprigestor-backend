package com.devmaster.goatfarm.genealogy.business.mapper;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.goat.application.ports.out.GoatGenealogySnapshot;
import org.springframework.stereotype.Component;

/** Maps the application-owned technical genealogy read model to the API VO. */
@Component
public class GenealogyBusinessMapper {

    public GenealogyResponseVO toResponseVO(GoatGenealogySnapshot goat) {
        GoatGenealogySnapshot paternalGrandfather = fatherOf(fatherOf(goat));
        GoatGenealogySnapshot paternalGrandmother = motherOf(fatherOf(goat));
        GoatGenealogySnapshot maternalGrandfather = fatherOf(motherOf(goat));
        GoatGenealogySnapshot maternalGrandmother = motherOf(motherOf(goat));

        return GenealogyResponseVO.builder()
                .goatName(goat.name())
                .goatRegistration(goat.registrationNumber())
                .breeder(goat.breederName())
                .farmOwner(goat.farmOwnerName())
                .breed(goat.breed() == null ? null : goat.breed().toString())
                .color(goat.color())
                .status(goat.status() == null ? null : goat.status().toString())
                .gender(goat.gender() == null ? null : goat.gender().toString())
                .category(goat.category() == null ? null : goat.category().toString())
                .tod(goat.tod())
                .toe(goat.toe())
                .birthDate(goat.birthDate() == null ? null : goat.birthDate().toString())
                .fatherName(nameOf(fatherOf(goat)))
                .fatherRegistration(registrationOf(fatherOf(goat), externalFatherOf(goat)))
                .motherName(nameOf(motherOf(goat)))
                .motherRegistration(registrationOf(motherOf(goat), externalMotherOf(goat)))
                .paternalGrandfatherName(nameOf(paternalGrandfather))
                .paternalGrandfatherRegistration(registrationOf(paternalGrandfather, null))
                .paternalGrandmotherName(nameOf(paternalGrandmother))
                .paternalGrandmotherRegistration(registrationOf(paternalGrandmother, null))
                .maternalGrandfatherName(nameOf(maternalGrandfather))
                .maternalGrandfatherRegistration(registrationOf(maternalGrandfather, null))
                .maternalGrandmotherName(nameOf(maternalGrandmother))
                .maternalGrandmotherRegistration(registrationOf(maternalGrandmother, null))
                .paternalGreatGrandfather1Name(nameOf(fatherOf(paternalGrandfather)))
                .paternalGreatGrandfather1Registration(registrationOf(fatherOf(paternalGrandfather), null))
                .paternalGreatGrandmother1Name(nameOf(motherOf(paternalGrandfather)))
                .paternalGreatGrandmother1Registration(registrationOf(motherOf(paternalGrandfather), null))
                .paternalGreatGrandfather2Name(nameOf(fatherOf(paternalGrandmother)))
                .paternalGreatGrandfather2Registration(registrationOf(fatherOf(paternalGrandmother), null))
                .paternalGreatGrandmother2Name(nameOf(motherOf(paternalGrandmother)))
                .paternalGreatGrandmother2Registration(registrationOf(motherOf(paternalGrandmother), null))
                .maternalGreatGrandfather1Name(nameOf(fatherOf(maternalGrandfather)))
                .maternalGreatGrandfather1Registration(registrationOf(fatherOf(maternalGrandfather), null))
                .maternalGreatGrandmother1Name(nameOf(motherOf(maternalGrandfather)))
                .maternalGreatGrandmother1Registration(registrationOf(motherOf(maternalGrandfather), null))
                .maternalGreatGrandfather2Name(nameOf(fatherOf(maternalGrandmother)))
                .maternalGreatGrandfather2Registration(registrationOf(fatherOf(maternalGrandmother), null))
                .maternalGreatGrandmother2Name(nameOf(motherOf(maternalGrandmother)))
                .maternalGreatGrandmother2Registration(registrationOf(motherOf(maternalGrandmother), null))
                .build();
    }

    private GoatGenealogySnapshot fatherOf(GoatGenealogySnapshot goat) {
        return localGoat(goat == null ? null : goat.father());
    }

    private GoatGenealogySnapshot motherOf(GoatGenealogySnapshot goat) {
        return localGoat(goat == null ? null : goat.mother());
    }

    private GoatGenealogySnapshot localGoat(GoatGenealogySnapshot.ParentReference reference) {
        return reference == null ? null : reference.localGoat();
    }

    private String externalFatherOf(GoatGenealogySnapshot goat) {
        return goat == null || goat.father() == null ? null : goat.father().externalRegistrationNumber();
    }

    private String externalMotherOf(GoatGenealogySnapshot goat) {
        return goat == null || goat.mother() == null ? null : goat.mother().externalRegistrationNumber();
    }

    private String nameOf(GoatGenealogySnapshot goat) {
        return goat == null ? null : goat.name();
    }

    private String registrationOf(GoatGenealogySnapshot goat, String externalRegistrationNumber) {
        return goat == null ? externalRegistrationNumber : goat.registrationNumber();
    }
}
