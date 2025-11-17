package com.devmaster.goatfarm.genealogy.mapper;

import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.api.dto.GenealogyResponseDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyRequestVO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GenealogyMapper {

    @Mapping(target = "goatName", source = "goatName")
    @Mapping(target = "goatRegistration", source = "goatRegistration")
    @Mapping(target = "breed", source = "goatBreed")
    @Mapping(target = "color", source = "goatCoatColor")
    @Mapping(target = "status", source = "goatStatus")
    @Mapping(target = "gender", source = "goatSex")
    @Mapping(target = "category", source = "goatCategory")
    @Mapping(target = "tod", source = "goatTOD")
    @Mapping(target = "toe", source = "goatTOE")
    @Mapping(target = "birthDate", source = "goatBirthDate")
    @Mapping(target = "breeder", source = "goatCreator")
    @Mapping(target = "farmOwner", source = "goatOwner")
    GenealogyResponseVO toResponseVO(Genealogy entity);

    GenealogyResponseDTO toResponseDTO(GenealogyResponseVO vo);

    // Conversões para suportar VO na camada de caso de uso
    GenealogyRequestVO toRequestVO(GenealogyRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goatBirthDate", ignore = true)
    @Mapping(target = "goatBreed", ignore = true)
    @Mapping(target = "goatCategory", ignore = true)
    @Mapping(target = "goatCoatColor", ignore = true)
    @Mapping(target = "goatCreator", ignore = true)
    @Mapping(target = "goatOwner", ignore = true)
    @Mapping(target = "goatSex", ignore = true)
    @Mapping(target = "goatStatus", ignore = true)
    @Mapping(target = "goatTOD", ignore = true)
    @Mapping(target = "goatTOE", ignore = true)
    Genealogy toEntity(GenealogyRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goatBirthDate", ignore = true)
    @Mapping(target = "goatBreed", ignore = true)
    @Mapping(target = "goatCategory", ignore = true)
    @Mapping(target = "goatCoatColor", ignore = true)
    @Mapping(target = "goatCreator", ignore = true)
    @Mapping(target = "goatOwner", ignore = true)
    @Mapping(target = "goatSex", ignore = true)
    @Mapping(target = "goatStatus", ignore = true)
    @Mapping(target = "goatTOD", ignore = true)
    @Mapping(target = "goatTOE", ignore = true)
    Genealogy toEntity(GenealogyRequestVO vo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goatName", source = "name")
    @Mapping(target = "goatRegistration", source = "registrationNumber")
    @Mapping(target = "goatBreed", expression = "java(goat.getBreed() != null ? goat.getBreed().toString() : null)")
    @Mapping(target = "goatCoatColor", source = "color")
    @Mapping(target = "goatStatus", expression = "java(goat.getStatus() != null ? goat.getStatus().toString() : null)")
    @Mapping(target = "goatSex", expression = "java(goat.getGender() != null ? goat.getGender().toString() : null)")
    @Mapping(target = "goatCategory", expression = "java(goat.getCategory() != null ? goat.getCategory().toString() : null)")
    @Mapping(target = "goatTOD", source = "tod")
    @Mapping(target = "goatTOE", source = "toe")
    @Mapping(target = "goatBirthDate", expression = "java(goat.getBirthDate() != null ? goat.getBirthDate().toString() : null)")
    @Mapping(target = "goatCreator", source = "user.name")
    @Mapping(target = "goatOwner", source = "farm.user.name")
    @Mapping(target = "fatherName", source = "father.name")
    @Mapping(target = "fatherRegistration", source = "father.registrationNumber")
    @Mapping(target = "paternalGrandfatherName", source = "father.father.name")
    @Mapping(target = "paternalGrandfatherRegistration", source = "father.father.registrationNumber")
    @Mapping(target = "paternalGreatGrandfather1Name", source = "father.father.father.name")
    @Mapping(target = "paternalGreatGrandfather1Registration", source = "father.father.father.registrationNumber")
    @Mapping(target = "paternalGreatGrandmother1Name", source = "father.father.mother.name")
    @Mapping(target = "paternalGreatGrandmother1Registration", source = "father.father.mother.registrationNumber")
    @Mapping(target = "paternalGrandmotherName", source = "father.mother.name")
    @Mapping(target = "paternalGrandmotherRegistration", source = "father.mother.registrationNumber")
    @Mapping(target = "paternalGreatGrandfather2Name", source = "father.mother.father.name")
    @Mapping(target = "paternalGreatGrandfather2Registration", source = "father.mother.father.registrationNumber")
    @Mapping(target = "paternalGreatGrandmother2Name", source = "father.mother.mother.name")
    @Mapping(target = "paternalGreatGrandmother2Registration", source = "father.mother.mother.registrationNumber")
    @Mapping(target = "motherName", source = "mother.name")
    @Mapping(target = "motherRegistration", source = "mother.registrationNumber")
    @Mapping(target = "maternalGrandfatherName", source = "mother.father.name")
    @Mapping(target = "maternalGrandfatherRegistration", source = "mother.father.registrationNumber")
    @Mapping(target = "maternalGreatGrandfather1Name", source = "mother.father.father.name")
    @Mapping(target = "maternalGreatGrandfather1Registration", source = "mother.father.father.registrationNumber")
    @Mapping(target = "maternalGreatGrandmother1Name", source = "mother.father.mother.name")
    @Mapping(target = "maternalGreatGrandmother1Registration", source = "mother.father.mother.registrationNumber")
    @Mapping(target = "maternalGrandmotherName", source = "mother.mother.name")
    @Mapping(target = "maternalGrandmotherRegistration", source = "mother.mother.registrationNumber")
    @Mapping(target = "maternalGreatGrandfather2Name", source = "mother.mother.father.name")
    @Mapping(target = "maternalGreatGrandfather2Registration", source = "mother.mother.father.registrationNumber")
    @Mapping(target = "maternalGreatGrandmother2Name", source = "mother.mother.mother.name")
    @Mapping(target = "maternalGreatGrandmother2Registration", source = "mother.mother.mother.registrationNumber")
    Genealogy toEntity(Goat goat);
}
