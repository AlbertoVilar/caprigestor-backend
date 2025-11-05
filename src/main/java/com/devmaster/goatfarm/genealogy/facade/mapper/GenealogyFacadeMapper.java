package com.devmaster.goatfarm.genealogy.facade.mapper;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.facade.dto.GenealogyFacadeResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper para converter entre GenealogyResponseVO e GenealogyFacadeResponseDTO
 * Encapsula a conversÃ£o de dados internos (VO) para dados expostos pelo Facade (DTO)
 */
@Mapper(componentModel = "spring")
public interface GenealogyFacadeMapper {

    /**
     * Converte GenealogyResponseVO para GenealogyFacadeResponseDTO
     * @param vo VO interno do business
     * @return DTO para exposiÃ§Ã£o pelo Facade
     */
    @Mapping(source = "goatName", target = "goatName")
    @Mapping(source = "goatRegistration", target = "goatRegistration")
    @Mapping(source = "breeder", target = "breeder")
    @Mapping(source = "farmOwner", target = "farmOwner")
    @Mapping(source = "breed", target = "breed")
    @Mapping(source = "color", target = "color")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "gender", target = "gender")
    @Mapping(source = "category", target = "category")
    @Mapping(source = "tod", target = "tod")
    @Mapping(source = "toe", target = "toe")
    @Mapping(source = "birthDate", target = "birthDate")
        @Mapping(source = "fatherName", target = "fatherName")
    @Mapping(source = "fatherRegistration", target = "fatherRegistration")
    @Mapping(source = "motherName", target = "motherName")
    @Mapping(source = "motherRegistration", target = "motherRegistration")
        @Mapping(source = "paternalGrandfatherName", target = "paternalGrandfatherName")
    @Mapping(source = "paternalGrandfatherRegistration", target = "paternalGrandfatherRegistration")
    @Mapping(source = "paternalGrandmotherName", target = "paternalGrandmotherName")
    @Mapping(source = "paternalGrandmotherRegistration", target = "paternalGrandmotherRegistration")
        @Mapping(source = "maternalGrandfatherName", target = "maternalGrandfatherName")
    @Mapping(source = "maternalGrandfatherRegistration", target = "maternalGrandfatherRegistration")
    @Mapping(source = "maternalGrandmotherName", target = "maternalGrandmotherName")
    @Mapping(source = "maternalGrandmotherRegistration", target = "maternalGrandmotherRegistration")
        @Mapping(source = "paternalGreatGrandfather1Name", target = "paternalGreatGrandfather1Name")
    @Mapping(source = "paternalGreatGrandfather1Registration", target = "paternalGreatGrandfather1Registration")
    @Mapping(source = "paternalGreatGrandmother1Name", target = "paternalGreatGrandmother1Name")
    @Mapping(source = "paternalGreatGrandmother1Registration", target = "paternalGreatGrandmother1Registration")
    @Mapping(source = "paternalGreatGrandfather2Name", target = "paternalGreatGrandfather2Name")
    @Mapping(source = "paternalGreatGrandfather2Registration", target = "paternalGreatGrandfather2Registration")
    @Mapping(source = "paternalGreatGrandmother2Name", target = "paternalGreatGrandmother2Name")
    @Mapping(source = "paternalGreatGrandmother2Registration", target = "paternalGreatGrandmother2Registration")
        @Mapping(source = "maternalGreatGrandfather1Name", target = "maternalGreatGrandfather1Name")
    @Mapping(source = "maternalGreatGrandfather1Registration", target = "maternalGreatGrandfather1Registration")
    @Mapping(source = "maternalGreatGrandmother1Name", target = "maternalGreatGrandmother1Name")
    @Mapping(source = "maternalGreatGrandmother1Registration", target = "maternalGreatGrandmother1Registration")
    @Mapping(source = "maternalGreatGrandfather2Name", target = "maternalGreatGrandfather2Name")
    @Mapping(source = "maternalGreatGrandfather2Registration", target = "maternalGreatGrandfather2Registration")
    @Mapping(source = "maternalGreatGrandmother2Name", target = "maternalGreatGrandmother2Name")
    @Mapping(source = "maternalGreatGrandmother2Registration", target = "maternalGreatGrandmother2Registration")
    GenealogyFacadeResponseDTO toFacadeDTO(GenealogyResponseVO vo);
}
