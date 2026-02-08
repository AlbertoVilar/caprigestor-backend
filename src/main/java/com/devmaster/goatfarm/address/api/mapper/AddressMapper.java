package com.devmaster.goatfarm.address.api.mapper;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressRequestVO toVO(AddressRequestDTO dto);
    AddressResponseDTO toDTO(AddressResponseVO vo);
    // Somente DTO <-> VO. Mapeamentos de Entity ficam na camada business.
}

