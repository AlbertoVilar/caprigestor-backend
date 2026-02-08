package com.devmaster.goatfarm.farm.api.mapper;

import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmUpdateFarmDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullRequestVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.phone.api.mapper.PhoneMapper;
import com.devmaster.goatfarm.address.api.mapper.AddressMapper;
import com.devmaster.goatfarm.authority.api.mapper.UserMapper;

@Mapper(componentModel = "spring", uses = {PhoneMapper.class, UserMapper.class, AddressMapper.class})
public interface GoatFarmMapper {

    GoatFarmFullRequestVO toFullRequestVO(GoatFarmFullRequestDTO dto);

    @Mapping(target = "address", expression = "java(toAddressDTO(vo))")
    @Mapping(target = "user", expression = "java(toUserDTO(vo))")
    GoatFarmFullResponseDTO toFullDTO(GoatFarmFullResponseVO vo);

    GoatFarmResponseDTO toResponseDTO(GoatFarmResponseVO vo);

    GoatFarmRequestVO toRequestVO(GoatFarmRequestDTO dto);

    GoatFarmRequestVO toRequestVO(GoatFarmUpdateFarmDTO dto);

    // Construção manual do AddressResponseDTO a partir dos campos planos do VO
    default AddressResponseDTO toAddressDTO(GoatFarmFullResponseVO vo) {
        if (vo == null) return null;
        boolean noAddressData = vo.getAddressId() == null
                && (vo.getStreet() == null || vo.getStreet().isBlank())
                && (vo.getDistrict() == null || vo.getDistrict().isBlank())
                && (vo.getCity() == null || vo.getCity().isBlank())
                && (vo.getState() == null || vo.getState().isBlank())
                && (vo.getZipCode() == null || vo.getZipCode().isBlank());
        if (noAddressData) return null;

        return new AddressResponseDTO(
                vo.getAddressId(),
                vo.getStreet(),
                vo.getDistrict(), // neighborhood
                vo.getCity(),
                vo.getState(),
                vo.getZipCode(),
                vo.getCountry()
        );
    }

    // Construção manual do UserResponseDTO a partir dos campos planos do VO
    default UserResponseDTO toUserDTO(GoatFarmFullResponseVO vo) {
        if (vo == null) return null;
        boolean noUserData = vo.getUserId() == null
                && (vo.getUserName() == null || vo.getUserName().isBlank())
                && (vo.getUserEmail() == null || vo.getUserEmail().isBlank())
                && (vo.getUserCpf() == null || vo.getUserCpf().isBlank());
        if (noUserData) return null;

        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(vo.getUserId());
        dto.setName(vo.getUserName());
        dto.setEmail(vo.getUserEmail());
        dto.setCpf(vo.getUserCpf());
        dto.setRoles(vo.getUserRoles() != null ? vo.getUserRoles() : java.util.Collections.emptyList());
        return dto;
    }

}
