package com.devmaster.goatfarm.genealogy.mapper;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.api.dto.GenealogyResponseDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GenealogyMapper {
    GenealogyMapper INSTANCE = Mappers.getMapper(GenealogyMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "breeder", target = "goatCreator")
    @Mapping(source = "farmOwner", target = "goatOwner")
    @Mapping(source = "breed", target = "goatBreed")
    @Mapping(source = "color", target = "goatCoatColor")
    @Mapping(source = "status", target = "goatStatus")
    @Mapping(source = "gender", target = "goatSex")
    @Mapping(source = "category", target = "goatCategory")
    @Mapping(source = "tod", target = "goatTOD")
    @Mapping(source = "toe", target = "goatTOE")
    @Mapping(source = "birthDate", target = "goatBirthDate")
    // Parents mapping
    @Mapping(source = "fatherName", target = "fatherName")
    @Mapping(source = "fatherRegistration", target = "fatherRegistration")
    @Mapping(source = "motherName", target = "motherName")
    @Mapping(source = "motherRegistration", target = "motherRegistration")
    // Paternal grandparents mapping
    @Mapping(source = "paternalGrandfatherName", target = "paternalGrandfatherName")
    @Mapping(source = "paternalGrandfatherRegistration", target = "paternalGrandfatherRegistration")
    @Mapping(source = "paternalGrandmotherName", target = "paternalGrandmotherName")
    @Mapping(source = "paternalGrandmotherRegistration", target = "paternalGrandmotherRegistration")
    // Maternal grandparents mapping
    @Mapping(source = "maternalGrandfatherName", target = "maternalGrandfatherName")
    @Mapping(source = "maternalGrandfatherRegistration", target = "maternalGrandfatherRegistration")
    @Mapping(source = "maternalGrandmotherName", target = "maternalGrandmotherName")
    @Mapping(source = "maternalGrandmotherRegistration", target = "maternalGrandmotherRegistration")
    // Paternal great-grandparents mapping
    @Mapping(source = "paternalGreatGrandfather1Name", target = "paternalGreatGrandfather1Name")
    @Mapping(source = "paternalGreatGrandfather1Registration", target = "paternalGreatGrandfather1Registration")
    @Mapping(source = "paternalGreatGrandmother1Name", target = "paternalGreatGrandmother1Name")
    @Mapping(source = "paternalGreatGrandmother1Registration", target = "paternalGreatGrandmother1Registration")
    @Mapping(source = "paternalGreatGrandfather2Name", target = "paternalGreatGrandfather2Name")
    @Mapping(source = "paternalGreatGrandfather2Registration", target = "paternalGreatGrandfather2Registration")
    @Mapping(source = "paternalGreatGrandmother2Name", target = "paternalGreatGrandmother2Name")
    @Mapping(source = "paternalGreatGrandmother2Registration", target = "paternalGreatGrandmother2Registration")
    // Maternal great-grandparents mapping
    @Mapping(source = "maternalGreatGrandfather1Name", target = "maternalGreatGrandfather1Name")
    @Mapping(source = "maternalGreatGrandfather1Registration", target = "maternalGreatGrandfather1Registration")
    @Mapping(source = "maternalGreatGrandmother1Name", target = "maternalGreatGrandmother1Name")
    @Mapping(source = "maternalGreatGrandmother1Registration", target = "maternalGreatGrandmother1Registration")
    @Mapping(source = "maternalGreatGrandfather2Name", target = "maternalGreatGrandfather2Name")
    @Mapping(source = "maternalGreatGrandfather2Registration", target = "maternalGreatGrandfather2Registration")
    @Mapping(source = "maternalGreatGrandmother2Name", target = "maternalGreatGrandmother2Name")
    @Mapping(source = "maternalGreatGrandmother2Registration", target = "maternalGreatGrandmother2Registration")
    Genealogy toEntity(GenealogyRequestDTO dto);

    @Mapping(source = "goatCreator", target = "breeder")
    @Mapping(source = "goatOwner", target = "farmOwner")
    @Mapping(source = "goatBreed", target = "breed")
    @Mapping(source = "goatCoatColor", target = "color")
    @Mapping(source = "goatStatus", target = "status")
    @Mapping(source = "goatSex", target = "gender")
    @Mapping(source = "goatCategory", target = "category")
    @Mapping(source = "goatTOD", target = "tod")
    @Mapping(source = "goatTOE", target = "toe")
    @Mapping(source = "goatBirthDate", target = "birthDate")
    // Parents mapping
    @Mapping(source = "fatherName", target = "fatherName")
    @Mapping(source = "fatherRegistration", target = "fatherRegistration")
    @Mapping(source = "motherName", target = "motherName")
    @Mapping(source = "motherRegistration", target = "motherRegistration")
    // Paternal grandparents mapping
    @Mapping(source = "paternalGrandfatherName", target = "paternalGrandfatherName")
    @Mapping(source = "paternalGrandfatherRegistration", target = "paternalGrandfatherRegistration")
    @Mapping(source = "paternalGrandmotherName", target = "paternalGrandmotherName")
    @Mapping(source = "paternalGrandmotherRegistration", target = "paternalGrandmotherRegistration")
    // Maternal grandparents mapping
    @Mapping(source = "maternalGrandfatherName", target = "maternalGrandfatherName")
    @Mapping(source = "maternalGrandfatherRegistration", target = "maternalGrandfatherRegistration")
    @Mapping(source = "maternalGrandmotherName", target = "maternalGrandmotherName")
    @Mapping(source = "maternalGrandmotherRegistration", target = "maternalGrandmotherRegistration")
    // Paternal great-grandparents mapping
    @Mapping(source = "paternalGreatGrandfather1Name", target = "paternalGreatGrandfather1Name")
    @Mapping(source = "paternalGreatGrandfather1Registration", target = "paternalGreatGrandfather1Registration")
    @Mapping(source = "paternalGreatGrandmother1Name", target = "paternalGreatGrandmother1Name")
    @Mapping(source = "paternalGreatGrandmother1Registration", target = "paternalGreatGrandmother1Registration")
    @Mapping(source = "paternalGreatGrandfather2Name", target = "paternalGreatGrandfather2Name")
    @Mapping(source = "paternalGreatGrandfather2Registration", target = "paternalGreatGrandfather2Registration")
    @Mapping(source = "paternalGreatGrandmother2Name", target = "paternalGreatGrandmother2Name")
    @Mapping(source = "paternalGreatGrandmother2Registration", target = "paternalGreatGrandmother2Registration")
    // Maternal great-grandparents mapping
    @Mapping(source = "maternalGreatGrandfather1Name", target = "maternalGreatGrandfather1Name")
    @Mapping(source = "maternalGreatGrandfather1Registration", target = "maternalGreatGrandfather1Registration")
    @Mapping(source = "maternalGreatGrandmother1Name", target = "maternalGreatGrandmother1Name")
    @Mapping(source = "maternalGreatGrandmother1Registration", target = "maternalGreatGrandmother1Registration")
    @Mapping(source = "maternalGreatGrandfather2Name", target = "maternalGreatGrandfather2Name")
    @Mapping(source = "maternalGreatGrandfather2Registration", target = "maternalGreatGrandfather2Registration")
    @Mapping(source = "maternalGreatGrandmother2Name", target = "maternalGreatGrandmother2Name")
    @Mapping(source = "maternalGreatGrandmother2Registration", target = "maternalGreatGrandmother2Registration")
    GenealogyResponseVO toResponseVO(Genealogy entity);

    GenealogyResponseDTO toResponseDTO(GenealogyResponseVO vo);
}
