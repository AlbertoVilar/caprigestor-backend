package com.devmaster.goatfarm.genealogy.converter;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyResponseDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import org.springframework.transaction.annotation.Transactional;

public class GenealogyDTOConverter {

    @Transactional(readOnly = true)
    public static GenealogyResponseDTO toResponseDTO(GenealogyResponseVO responseVO) {
        return GenealogyResponseDTO.builder()

                .goatName(responseVO.getGoatName())
                .goatRegistration(responseVO.getGoatRegistration())
                .breeder(responseVO.getBreeder())
                .owner(responseVO.getOwner())
                .breed(responseVO.getBreed())
                .color(responseVO.getColor())
                .status(responseVO.getStatus())
                .gender(responseVO.getGender())
                .category(responseVO.getCategory())
                .tod(responseVO.getTod())
                .toe(responseVO.getToe())
                .birthDate(responseVO.getBirthDate())


                .fatherName(responseVO.getFatherName())
                .fatherRegistration(responseVO.getFatherRegistration())
                .motherName(responseVO.getMotherName())
                .motherRegistration(responseVO.getMotherRegistration())


                .paternalGrandfatherName(responseVO.getPaternalGrandfatherName())
                .paternalGrandfatherRegistration(responseVO.getPaternalGrandfatherRegistration())
                .paternalGrandmotherName(responseVO.getPaternalGrandmotherName())
                .paternalGrandmotherRegistration(responseVO.getPaternalGrandmotherRegistration())


                .maternalGrandfatherName(responseVO.getMaternalGrandfatherName())
                .maternalGrandfatherRegistration(responseVO.getMaternalGrandfatherRegistration())
                .maternalGrandmotherName(responseVO.getMaternalGrandmotherName())
                .maternalGrandmotherRegistration(responseVO.getMaternalGrandmotherRegistration())


                .paternalGreatGrandfather1Name(responseVO.getPaternalGreatGrandfather1Name())
                .paternalGreatGrandfather1Registration(responseVO.getPaternalGreatGrandfather1Registration())
                .paternalGreatGrandmother1Name(responseVO.getPaternalGreatGrandmother1Name())
                .paternalGreatGrandmother1Registration(responseVO.getPaternalGreatGrandmother1Registration())
                .paternalGreatGrandfather2Name(responseVO.getPaternalGreatGrandfather2Name())
                .paternalGreatGrandfather2Registration(responseVO.getPaternalGreatGrandfather2Registration())
                .paternalGreatGrandmother2Name(responseVO.getPaternalGreatGrandmother2Name())
                .paternalGreatGrandmother2Registration(responseVO.getPaternalGreatGrandmother2Registration())


                .maternalGreatGrandfather1Name(responseVO.getMaternalGreatGrandfather1Name())
                .maternalGreatGrandfather1Registration(responseVO.getMaternalGreatGrandfather1Registration())
                .maternalGreatGrandmother1Name(responseVO.getMaternalGreatGrandmother1Name())
                .maternalGreatGrandmother1Registration(responseVO.getMaternalGreatGrandmother1Registration())
                .maternalGreatGrandfather2Name(responseVO.getMaternalGreatGrandfather2Name())
                .maternalGreatGrandfather2Registration(responseVO.getMaternalGreatGrandfather2Registration())
                .maternalGreatGrandmother2Name(responseVO.getMaternalGreatGrandmother2Name())
                .maternalGreatGrandmother2Registration(responseVO.getMaternalGreatGrandmother2Registration())

                .build();
    }
}
