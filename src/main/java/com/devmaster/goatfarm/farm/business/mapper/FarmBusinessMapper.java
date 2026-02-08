package com.devmaster.goatfarm.farm.business.mapper;

import com.devmaster.goatfarm.authority.persistence.entity.Role;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.persistence.entity.Phone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface FarmBusinessMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "phones", ignore = true)
    @Mapping(target = "goats", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    GoatFarm toEntity(GoatFarmRequestVO vo);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "phones", ignore = true)
    @Mapping(target = "goats", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget GoatFarm entity, GoatFarmRequestVO vo);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "userCpf", source = "user.cpf")
    @Mapping(target = "addressId", source = "address.id")
    @Mapping(target = "street", source = "address.street")
    @Mapping(target = "district", source = "address.neighborhood")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "state", source = "address.state")
    @Mapping(target = "zipCode", source = "address.zipCode")
    @Mapping(target = "country", source = "address.country")
    @Mapping(target = "userRoles", source = "user.roles", qualifiedByName = "rolesToStringList")
    @Mapping(target = "phones", source = "phones")
    GoatFarmFullResponseVO toFullResponseVO(GoatFarm entity);

    GoatFarmResponseVO toResponseVO(GoatFarm entity);

    PhoneResponseVO toPhoneResponseVO(Phone entity);

    List<PhoneResponseVO> toPhoneResponseVOList(List<Phone> entities);

    // Auxiliar para conversão de timestamps
    default LocalDateTime map(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneId.systemDefault());
    }

    @Named("rolesToStringList")
    default List<String> rolesToStringList(Set<Role> roles) {
        return roles == null ? null : roles.stream().map(Role::getAuthority).collect(Collectors.toList());
    }
}
