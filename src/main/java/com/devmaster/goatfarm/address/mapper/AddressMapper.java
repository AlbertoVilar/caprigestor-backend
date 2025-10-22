package com.devmaster.goatfarm.address.mapper;

import com.devmaster.goatfarm.address.api.dto.AddressDTO;
import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.address.business.bo.AddressVO;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.model.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

    AddressVO toVO(AddressDTO dto);
    AddressRequestVO toVO(AddressRequestDTO dto);
    AddressDTO toDTO(AddressVO vo);
    AddressResponseDTO toDTO(AddressResponseVO vo);

    Address toEntity(AddressVO vo);
    @Mapping(target = "id", ignore = true)
    Address toEntity(AddressRequestVO vo);
    @Mapping(target = "id", ignore = true)
    void toEntity(@MappingTarget Address target, AddressRequestVO vo);

    AddressVO toVO(Address entity);
    AddressResponseVO toResponseVO(Address entity);
}

