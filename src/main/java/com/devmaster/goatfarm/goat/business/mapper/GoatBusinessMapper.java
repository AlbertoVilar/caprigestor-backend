package com.devmaster.goatfarm.goat.business.mapper;

import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GoatBusinessMapper {
    @Mapping(target = "fatherName", source = "father.name")
    @Mapping(target = "fatherRegistrationNumber", source = "father.registrationNumber")
    @Mapping(target = "motherName", source = "mother.name")
    @Mapping(target = "motherRegistrationNumber", source = "mother.registrationNumber")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "farmId", source = "farm.id")
    @Mapping(target = "farmName", source = "farm.name")
    GoatResponseVO toResponseVO(Goat entity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "farm", ignore = true)
    @Mapping(target = "father", ignore = true)
    @Mapping(target = "mother", ignore = true)
    Goat toEntity(GoatRequestVO vo);

    @Mapping(target = "registrationNumber", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "farm", ignore = true)
    @Mapping(target = "name", source = "vo.name")
    @Mapping(target = "gender", source = "vo.gender")
    @Mapping(target = "breed", source = "vo.breed")
    @Mapping(target = "color", source = "vo.color")
    @Mapping(target = "birthDate", source = "vo.birthDate")
    @Mapping(target = "status", source = "vo.status")
    @Mapping(target = "tod", source = "vo.tod")
    @Mapping(target = "toe", source = "vo.toe")
    @Mapping(target = "category", source = "vo.category")
    @Mapping(target = "father", source = "father")
    @Mapping(target = "mother", source = "mother")
    void updateEntity(@MappingTarget Goat entity, GoatRequestVO vo, Goat father, Goat mother);
}
