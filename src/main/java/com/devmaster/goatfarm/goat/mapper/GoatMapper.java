package com.devmaster.goatfarm.goat.mapper;

import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface GoatMapper {
    GoatMapper INSTANCE = Mappers.getMapper(GoatMapper.class);

    // DTO <-> Entity
    Goat toEntity(GoatRequestDTO dto);
    
    @Mapping(source = "farm.name", target = "farmName")
    @Mapping(source = "farm.id", target = "farmId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "father.name", target = "fatherName")
    @Mapping(source = "father.registrationNumber", target = "fatherRegistrationNumber")
    @Mapping(source = "mother.name", target = "motherName")
    @Mapping(source = "mother.registrationNumber", target = "motherRegistrationNumber")
    GoatResponseDTO toResponseDTO(Goat entity);

    // DTO <-> VO
    GoatRequestVO toRequestVO(GoatRequestDTO dto);
    GoatResponseDTO toResponseDTO(GoatResponseVO vo);
    
    // ✅ CORREÇÃO CRÍTICA: Mapeamento de Entity para ResponseVO com objetos aninhados
    @Mapping(source = "farm.name", target = "farmName")
    @Mapping(source = "farm.id", target = "farmId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "father.name", target = "fatherName")
    @Mapping(source = "father.registrationNumber", target = "fatherRegistrationNumber")
    @Mapping(source = "mother.name", target = "motherName")
    @Mapping(source = "mother.registrationNumber", target = "motherRegistrationNumber")
    GoatResponseVO toResponseVO(Goat entity);
    
    Goat toEntity(GoatRequestVO vo);
}
