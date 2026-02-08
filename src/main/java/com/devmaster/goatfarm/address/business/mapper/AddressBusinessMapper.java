package com.devmaster.goatfarm.address.business.mapper;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.persistence.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AddressBusinessMapper {

    @Mapping(target = "id", ignore = true)
    Address toEntity(AddressRequestVO vo);

    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget Address target, AddressRequestVO vo);

    AddressResponseVO toResponseVO(Address entity);
}
