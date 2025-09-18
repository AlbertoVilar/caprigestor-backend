package com.devmaster.goatfarm.genealogy.converter;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class GenealogyEntityConverter {

    @Transactional
    public static Genealogy toEntity(GenealogyResponseVO vo) {
        return Genealogy.builder()
            .goatName(vo.getGoatName())
            .goatRegistration(vo.getGoatRegistration())
            .goatCreator(vo.getBreeder())
            .goatOwner(vo.getFarmOwner())
            .goatBreed(vo.getBreed())
            .goatCoatColor(vo.getColor())
            .goatStatus(vo.getStatus())
            .goatSex(vo.getGender())
            .goatCategory(vo.getCategory())
            .goatTOD(vo.getTod())
            .goatTOE(vo.getToe())
            .goatBirthDate(vo.getBirthDate())
            .fatherName(vo.getFatherName())
            .fatherRegistration(vo.getFatherRegistration())
            .motherName(vo.getMotherName())
            .motherRegistration(vo.getMotherRegistration())
            .paternalGrandfatherName(vo.getPaternalGrandfatherName())
            .paternalGrandfatherRegistration(vo.getPaternalGrandfatherRegistration())
            .paternalGrandmotherName(vo.getPaternalGrandmotherName())
            .paternalGrandmotherRegistration(vo.getPaternalGrandmotherRegistration())
            .maternalGrandfatherName(vo.getMaternalGrandfatherName())
            .maternalGrandfatherRegistration(vo.getMaternalGrandfatherRegistration())
            .maternalGrandmotherName(vo.getMaternalGrandmotherName())
            .maternalGrandmotherRegistration(vo.getMaternalGrandmotherRegistration())
            .paternalGreatGrandfather1Name(vo.getPaternalGreatGrandfather1Name())
            .paternalGreatGrandfather1Registration(vo.getPaternalGreatGrandfather1Registration())
            .paternalGreatGrandmother1Name(vo.getPaternalGreatGrandmother1Name())
            .paternalGreatGrandmother1Registration(vo.getPaternalGreatGrandmother1Registration())
            .paternalGreatGrandfather2Name(vo.getPaternalGreatGrandfather2Name())
            .paternalGreatGrandfather2Registration(vo.getPaternalGreatGrandfather2Registration())
            .paternalGreatGrandmother2Name(vo.getPaternalGreatGrandmother2Name())
            .paternalGreatGrandmother2Registration(vo.getPaternalGreatGrandmother2Registration())
            .maternalGreatGrandfather1Name(vo.getMaternalGreatGrandfather1Name())
            .maternalGreatGrandfather1Registration(vo.getMaternalGreatGrandfather1Registration())
            .maternalGreatGrandmother1Name(vo.getMaternalGreatGrandmother1Name())
            .maternalGreatGrandmother1Registration(vo.getMaternalGreatGrandmother1Registration())
            .maternalGreatGrandfather2Name(vo.getMaternalGreatGrandfather2Name())
            .maternalGreatGrandfather2Registration(vo.getMaternalGreatGrandfather2Registration())
            .maternalGreatGrandmother2Name(vo.getMaternalGreatGrandmother2Name())
            .maternalGreatGrandmother2Registration(vo.getMaternalGreatGrandmother2Registration())
            .build();
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
