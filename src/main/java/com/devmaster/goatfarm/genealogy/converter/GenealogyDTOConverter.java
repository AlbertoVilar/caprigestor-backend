package com.devmaster.goatfarm.genealogy.converter;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.api.dto.GenealogyResponseDTO;

public class GenealogyDTOConverter {
    public static GenealogyResponseDTO toResponseDTO(GenealogyResponseVO vo) {
        if (vo == null) return null;
        return GenealogyResponseDTO.builder()
            .goatName(vo.getGoatName())
            .goatRegistration(vo.getGoatRegistration())
            .breeder(vo.getBreeder())
            .farmOwner(vo.getFarmOwner())
            .breed(vo.getBreed())
            .color(vo.getColor())
            .status(vo.getStatus())
            .gender(vo.getGender())
            .category(vo.getCategory())
            .tod(vo.getTod())
            .toe(vo.getToe())
            .birthDate(vo.getBirthDate())
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
}
