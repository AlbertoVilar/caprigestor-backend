package com.devmaster.goatfarm.farm.mapper;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import com.devmaster.goatfarm.phone.mapper.PhoneMapper;
import com.devmaster.goatfarm.address.mapper.AddressMapper;
import com.devmaster.goatfarm.authority.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PhoneMapper.class, AddressMapper.class, UserMapper.class})
public interface GoatFarmMapper {

    // DTO -> Entity
    GoatFarm toEntity(GoatFarmRequestDTO dto);

    // VO -> Entity (para criar/atualizar a partir do VO)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "goats", ignore = true)
    @Mapping(target = "phones", ignore = true)
    GoatFarm toEntity(GoatFarmRequestVO vo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "goats", ignore = true)
    @Mapping(target = "phones", ignore = true)
    void updateEntity(@MappingTarget GoatFarm target, GoatFarmRequestVO vo);

    // Entity -> DTO
    GoatFarmResponseDTO toResponseDTO(GoatFarm entity);

    // VO -> DTO
    GoatFarmResponseDTO toResponseDTO(GoatFarmResponseVO vo);

    // Full VO -> Full DTO
    GoatFarmFullResponseDTO toFullDTO(GoatFarmFullResponseVO vo);

    // DTO -> VO
    GoatFarmRequestVO toRequestVO(GoatFarmRequestDTO dto);
    GoatFarmFullRequestVO toFullRequestVO(GoatFarmFullRequestDTO dto);

    // Entity -> VO
    GoatFarmResponseVO toResponseVO(GoatFarm entity);

    // Entity -> Full VO (mapeia campos aninhados)
    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "tod", target = "tod")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "user.cpf", target = "userCpf")
    @Mapping(source = "address.id", target = "addressId")
    @Mapping(source = "address.street", target = "street")
    @Mapping(source = "address.neighborhood", target = "district")
    @Mapping(source = "address.city", target = "city")
    @Mapping(source = "address.state", target = "state")
    @Mapping(source = "address.zipCode", target = "zipCode")
    @Mapping(source = "phones", target = "phones")
    GoatFarmFullResponseVO toFullResponseVO(GoatFarm entity);

}