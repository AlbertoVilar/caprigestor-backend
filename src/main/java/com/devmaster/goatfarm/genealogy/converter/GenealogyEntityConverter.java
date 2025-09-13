package com.devmaster.goatfarm.genealogy.converter;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GenealogyEntityConverter {

    @Transactional
    public static Genealogy toEntity(GenealogyResponseVO vo) {
        return new Genealogy(
                null,


                vo.getGoatName(),
                vo.getGoatRegistration(),
                vo.getBreeder(),
                vo.getFarmOwner(),
                vo.getBreed(),
                vo.getColor(),
                vo.getStatus(),
                vo.getGender(),
                vo.getCategory(),
                vo.getTod(),
                vo.getToe(),
                vo.getBirthDate(),


                vo.getFatherName(),
                vo.getFatherRegistration(),
                vo.getMotherName(),
                vo.getMotherRegistration(),


                vo.getPaternalGrandfatherName(),
                vo.getPaternalGrandfatherRegistration(),
                vo.getPaternalGrandmotherName(),
                vo.getPaternalGrandmotherRegistration(),


                vo.getMaternalGrandfatherName(),
                vo.getMaternalGrandfatherRegistration(),
                vo.getMaternalGrandmotherName(),
                vo.getMaternalGrandmotherRegistration(),


                vo.getPaternalGreatGrandfather1Name(),
                vo.getPaternalGreatGrandfather1Registration(),
                vo.getPaternalGreatGrandmother1Name(),
                vo.getPaternalGreatGrandmother1Registration(),
                vo.getPaternalGreatGrandfather2Name(),
                vo.getPaternalGreatGrandfather2Registration(),
                vo.getPaternalGreatGrandmother2Name(),
                vo.getPaternalGreatGrandmother2Registration(),


                vo.getMaternalGreatGrandfather1Name(),
                vo.getMaternalGreatGrandfather1Registration(),
                vo.getMaternalGreatGrandmother1Name(),
                vo.getMaternalGreatGrandmother1Registration(),
                vo.getMaternalGreatGrandfather2Name(),
                vo.getMaternalGreatGrandfather2Registration(),
                vo.getMaternalGreatGrandmother2Name(),
                vo.getMaternalGreatGrandmother2Registration()
        );
    }

    @Transactional
    public static GenealogyResponseVO toResponseVO(Genealogy entity) {
        return GenealogyResponseVO.builder()

                .goatName(entity.getGoatName())
                .goatRegistration(entity.getGoatRegistration())
                .breeder(entity.getGoatCreator())
                .farmOwner(entity.getGoatOwner())
                .breed(entity.getGoatBreed())
                .color(entity.getGoatCoatColor())
                .status(entity.getGoatStatus())
                .gender(entity.getGoatSex())
                .category(entity.getGoatCategory())
                .tod(entity.getGoatTOD())
                .toe(entity.getGoatTOE())
                .birthDate(entity.getGoatBirthDate())


                .fatherName(entity.getFatherName())
                .fatherRegistration(entity.getFatherRegistration())
                .motherName(entity.getMotherName())
                .motherRegistration(entity.getMotherRegistration())


                .paternalGrandfatherName(entity.getPaternalGrandfatherName())
                .paternalGrandfatherRegistration(entity.getPaternalGrandfatherRegistration())
                .paternalGrandmotherName(entity.getPaternalGrandmotherName())
                .paternalGrandmotherRegistration(entity.getPaternalGrandmotherRegistration())


                .maternalGrandfatherName(entity.getMaternalGrandfatherName())
                .maternalGrandfatherRegistration(entity.getMaternalGrandfatherRegistration())
                .maternalGrandmotherName(entity.getMaternalGrandmotherName())
                .maternalGrandmotherRegistration(entity.getMaternalGrandmotherRegistration())


                .paternalGreatGrandfather1Name(entity.getPaternalGreatGrandfather1Name())
                .paternalGreatGrandfather1Registration(entity.getPaternalGreatGrandfather1Registration())
                .paternalGreatGrandmother1Name(entity.getPaternalGreatGrandmother1Name())
                .paternalGreatGrandmother1Registration(entity.getPaternalGreatGrandmother1Registration())
                .paternalGreatGrandfather2Name(entity.getPaternalGreatGrandfather2Name())
                .paternalGreatGrandfather2Registration(entity.getPaternalGreatGrandfather2Registration())
                .paternalGreatGrandmother2Name(entity.getPaternalGreatGrandmother2Name())
                .paternalGreatGrandmother2Registration(entity.getPaternalGreatGrandmother2Registration())


                .maternalGreatGrandfather1Name(entity.getMaternalGreatGrandfather1Name())
                .maternalGreatGrandfather1Registration(entity.getMaternalGreatGrandfather1Registration())
                .maternalGreatGrandmother1Name(entity.getMaternalGreatGrandmother1Name())
                .maternalGreatGrandmother1Registration(entity.getMaternalGreatGrandmother1Registration())
                .maternalGreatGrandfather2Name(entity.getMaternalGreatGrandfather2Name())
                .maternalGreatGrandfather2Registration(entity.getMaternalGreatGrandfather2Registration())
                .maternalGreatGrandmother2Name(entity.getMaternalGreatGrandmother2Name())
                .maternalGreatGrandmother2Registration(entity.getMaternalGreatGrandmother2Registration())

                .build();
    }


}
