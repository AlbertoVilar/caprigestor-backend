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
    @Mapping(target = "fatherName", expression = "java(goat.getFather() != null ? goat.getFather().getName() : null)")
    @Mapping(target = "fatherRegistration", expression = "java(goat.getFather() != null ? goat.getFather().getRegistrationNumber() : null)")
    @Mapping(target = "paternalGrandfatherName", expression = "java(goat.getFather() != null && goat.getFather().getFather() != null ? goat.getFather().getFather().getName() : null)")
    @Mapping(target = "paternalGrandfatherRegistration", expression = "java(goat.getFather() != null && goat.getFather().getFather() != null ? goat.getFather().getFather().getRegistrationNumber() : null)")
    @Mapping(target = "paternalGreatGrandfather1Name", expression = "java(goat.getFather() != null && goat.getFather().getFather() != null && goat.getFather().getFather().getFather() != null ? goat.getFather().getFather().getFather().getName() : null)")
    @Mapping(target = "paternalGreatGrandfather1Registration", expression = "java(goat.getFather() != null && goat.getFather().getFather() != null && goat.getFather().getFather().getFather() != null ? goat.getFather().getFather().getFather().getRegistrationNumber() : null)")
    @Mapping(target = "paternalGreatGrandmother1Name", expression = "java(goat.getFather() != null && goat.getFather().getFather() != null && goat.getFather().getFather().getMother() != null ? goat.getFather().getFather().getMother().getName() : null)")
    @Mapping(target = "paternalGreatGrandmother1Registration", expression = "java(goat.getFather() != null && goat.getFather().getFather() != null && goat.getFather().getFather().getMother() != null ? goat.getFather().getFather().getMother().getRegistrationNumber() : null)")
    @Mapping(target = "paternalGrandmotherName", expression = "java(goat.getFather() != null && goat.getFather().getMother() != null ? goat.getFather().getMother().getName() : null)")
    @Mapping(target = "paternalGrandmotherRegistration", expression = "java(goat.getFather() != null && goat.getFather().getMother() != null ? goat.getFather().getMother().getRegistrationNumber() : null)")
    @Mapping(target = "paternalGreatGrandfather2Name", expression = "java(goat.getFather() != null && goat.getFather().getMother() != null && goat.getFather().getMother().getFather() != null ? goat.getFather().getMother().getFather().getName() : null)")
    @Mapping(target = "paternalGreatGrandfather2Registration", expression = "java(goat.getFather() != null && goat.getFather().getMother() != null && goat.getFather().getMother().getFather() != null ? goat.getFather().getMother().getFather().getRegistrationNumber() : null)")
    @Mapping(target = "paternalGreatGrandmother2Name", expression = "java(goat.getFather() != null && goat.getFather().getMother() != null && goat.getFather().getMother().getMother() != null ? goat.getFather().getMother().getMother().getName() : null)")
    @Mapping(target = "paternalGreatGrandmother2Registration", expression = "java(goat.getFather() != null && goat.getFather().getMother() != null && goat.getFather().getMother().getMother() != null ? goat.getFather().getMother().getMother().getRegistrationNumber() : null)")
    @Mapping(target = "motherName", expression = "java(goat.getMother() != null ? goat.getMother().getName() : null)")
    @Mapping(target = "motherRegistration", expression = "java(goat.getMother() != null ? goat.getMother().getRegistrationNumber() : null)")
    @Mapping(target = "maternalGrandfatherName", expression = "java(goat.getMother() != null && goat.getMother().getFather() != null ? goat.getMother().getFather().getName() : null)")
    @Mapping(target = "maternalGrandfatherRegistration", expression = "java(goat.getMother() != null && goat.getMother().getFather() != null ? goat.getMother().getFather().getRegistrationNumber() : null)")
    @Mapping(target = "maternalGreatGrandfather1Name", expression = "java(goat.getMother() != null && goat.getMother().getFather() != null && goat.getMother().getFather().getFather() != null ? goat.getMother().getFather().getFather().getName() : null)")
    @Mapping(target = "maternalGreatGrandfather1Registration", expression = "java(goat.getMother() != null && goat.getMother().getFather() != null && goat.getMother().getFather().getFather() != null ? goat.getMother().getFather().getFather().getRegistrationNumber() : null)")
    @Mapping(target = "maternalGreatGrandmother1Name", expression = "java(goat.getMother() != null && goat.getMother().getFather() != null && goat.getMother().getFather().getMother() != null ? goat.getMother().getFather().getMother().getName() : null)")
    @Mapping(target = "maternalGreatGrandmother1Registration", expression = "java(goat.getMother() != null && goat.getMother().getFather() != null && goat.getMother().getFather().getMother() != null ? goat.getMother().getFather().getMother().getRegistrationNumber() : null)")
    @Mapping(target = "maternalGrandmotherName", expression = "java(goat.getMother() != null && goat.getMother().getMother() != null ? goat.getMother().getMother().getName() : null)")
    @Mapping(target = "maternalGrandmotherRegistration", expression = "java(goat.getMother() != null && goat.getMother().getMother() != null ? goat.getMother().getMother().getRegistrationNumber() : null)")
    @Mapping(target = "maternalGreatGrandfather2Name", expression = "java(goat.getMother() != null && goat.getMother().getMother() != null && goat.getMother().getMother().getFather() != null ? goat.getMother().getMother().getFather().getName() : null)")
    @Mapping(target = "maternalGreatGrandfather2Registration", expression = "java(goat.getMother() != null && goat.getMother().getMother() != null && goat.getMother().getMother().getFather() != null ? goat.getMother().getMother().getFather().getRegistrationNumber() : null)")
    @Mapping(target = "maternalGreatGrandmother2Name", expression = "java(goat.getMother() != null && goat.getMother().getMother() != null && goat.getMother().getMother().getMother() != null ? goat.getMother().getMother().getMother().getName() : null)")
    @Mapping(target = "maternalGreatGrandmother2Registration", expression = "java(goat.getMother() != null && goat.getMother().getMother() != null && goat.getMother().getMother().getMother() != null ? goat.getMother().getMother().getMother().getRegistrationNumber() : null)")
    Genealogy toEntity(Goat goat);

    /**
     * Projeção sob demanda:
     * Constrói o GenealogyResponseVO a partir de um Goat com grafo familiar carregado.
     *
     * <p>
     * Reaproveita o mapeamento existente:
     * Goat -> Genealogy (snapshot em memória) -> GenealogyResponseVO.
     * Não persiste nada em banco.
     * </p>
     */
    default GenealogyResponseVO toResponseVO(Goat goat) {
        if (goat == null) return null;
        return toResponseVO(toEntity(goat));
    }
}
