package com.devmaster.goatfarm.phone.mapper;

import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PhoneMapper {

    PhoneRequestVO toRequestVO(PhoneRequestDTO dto);
    
    Phone toEntity(PhoneRequestVO vo);
    
    PhoneResponseVO toResponseVO(Phone entity);
    
    PhoneResponseDTO toResponseDTO(PhoneResponseVO vo);
    
    List<PhoneResponseVO> toResponseVOList(List<Phone> entities);
    
    List<PhoneResponseDTO> toResponseDTOList(List<PhoneResponseVO> vos);
    
    void updatePhone(@MappingTarget Phone phone, PhoneRequestVO vo);
}