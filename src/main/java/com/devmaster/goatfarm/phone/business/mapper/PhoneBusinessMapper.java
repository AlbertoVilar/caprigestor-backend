package com.devmaster.goatfarm.phone.business.mapper;

import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.persistence.entity.Phone;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PhoneBusinessMapper {

    Phone toEntity(PhoneRequestVO vo);

    void updateEntity(@MappingTarget Phone phone, PhoneRequestVO vo);

    PhoneResponseVO toResponseVO(Phone entity);

    List<PhoneResponseVO> toResponseVOList(List<Phone> entities);
}
