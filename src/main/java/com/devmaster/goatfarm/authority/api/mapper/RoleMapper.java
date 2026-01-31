package com.devmaster.goatfarm.authority.api.mapper;

import com.devmaster.goatfarm.authority.persistence.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    Role toEntity(String authority);
}
