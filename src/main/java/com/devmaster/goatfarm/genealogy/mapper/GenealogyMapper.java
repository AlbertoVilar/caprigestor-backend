package com.devmaster.goatfarm.genealogy.mapper;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GenealogyMapper {

    @Mapping(target = "goatName", source = "name")
    @Mapping(target = "goatRegistration", source = "registrationNumber")
    @Mapping(target = "breed", expression = "java(goat.getBreed() != null ? goat.getBreed().toString() : null)")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "status", expression = "java(goat.getStatus() != null ? goat.getStatus().toString() : null)")
    @Mapping(target = "gender", expression = "java(goat.getGender() != null ? goat.getGender().toString() : null)")
    @Mapping(target = "category", expression = "java(goat.getCategory() != null ? goat.getCategory().toString() : null)")
    @Mapping(target = "tod", source = "tod")
    @Mapping(target = "toe", source = "toe")
    @Mapping(target = "birthDate", expression = "java(goat.getBirthDate() != null ? goat.getBirthDate().toString() : null)")
    @Mapping(target = "breeder", source = "user.name")
    @Mapping(target = "farmOwner", source = "farm.user.name")
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
    GenealogyResponseVO toResponseVO(Goat goat);
}
