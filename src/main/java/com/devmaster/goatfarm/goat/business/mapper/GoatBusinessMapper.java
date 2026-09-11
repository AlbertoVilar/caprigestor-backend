package com.devmaster.goatfarm.goat.business.mapper;

import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GoatBusinessMapper {
    @Mapping(target = "fatherName", source = "father.name")
    @Mapping(target = "fatherRegistrationNumber", expression = "java(entity.getFather() != null ? entity.getFather().getRegistrationNumber() : entity.getExternalFatherRegistrationNumber())")
    @Mapping(target = "motherName", source = "mother.name")
    @Mapping(target = "motherRegistrationNumber", expression = "java(entity.getMother() != null ? entity.getMother().getRegistrationNumber() : entity.getExternalMotherRegistrationNumber())")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "farmId", source = "farm.id")
    @Mapping(target = "farmName", source = "farm.name")
    GoatResponseVO toResponseVO(GoatEntity entity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "farm", ignore = true)
    @Mapping(target = "technicalId", ignore = true)
    @Mapping(target = "fatherTechnicalId", ignore = true)
    @Mapping(target = "motherTechnicalId", ignore = true)
    @Mapping(target = "technicalFather", ignore = true)
    @Mapping(target = "technicalMother", ignore = true)
    @Mapping(target = "father", ignore = true)
    @Mapping(target = "mother", ignore = true)
    @Mapping(target = "externalFatherRegistrationNumber", ignore = true)
    @Mapping(target = "externalMotherRegistrationNumber", ignore = true)
    GoatEntity toEntity(GoatRequestVO vo);

    @Mapping(target = "registrationNumber", ignore = true)
    @Mapping(target = "technicalId", ignore = true)
    @Mapping(target = "fatherTechnicalId", ignore = true)
    @Mapping(target = "motherTechnicalId", ignore = true)
    @Mapping(target = "technicalFather", ignore = true)
    @Mapping(target = "technicalMother", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "farm", ignore = true)
    @Mapping(target = "name", source = "vo.name")
    @Mapping(target = "gender", source = "vo.gender")
    @Mapping(target = "breed", source = "vo.breed")
    @Mapping(target = "color", source = "vo.color")
    @Mapping(target = "birthDate", source = "vo.birthDate")
    @Mapping(target = "status", source = "vo.status")
    @Mapping(target = "exitType", ignore = true)
    @Mapping(target = "exitDate", ignore = true)
    @Mapping(target = "exitNotes", ignore = true)
    @Mapping(target = "tod", source = "vo.tod")
    @Mapping(target = "toe", source = "vo.toe")
    @Mapping(target = "category", source = "vo.category")
    @Mapping(target = "father", source = "father")
    @Mapping(target = "mother", source = "mother")
    @Mapping(target = "externalFatherRegistrationNumber", ignore = true)
    @Mapping(target = "externalMotherRegistrationNumber", ignore = true)
    void updateEntity(@MappingTarget GoatEntity entity, GoatRequestVO vo, GoatEntity father, GoatEntity mother);
}
