package com.devmaster.goatfarm.phone.api.mapper;

import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import org.mapstruct.Mapper;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PhoneMapper {

    PhoneRequestVO toRequestVO(PhoneRequestDTO dto);

    PhoneResponseDTO toResponseDTO(PhoneResponseVO vo);

    List<PhoneResponseDTO> toResponseDTOList(List<PhoneResponseVO> vos);
    // Somente DTO <-> VO. Mapeamentos de Entity ficam na camada business.
}
