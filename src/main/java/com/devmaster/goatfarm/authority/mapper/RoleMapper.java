package com.devmaster.goatfarm.authority.mapper;

import com.devmaster.goatfarm.authority.model.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "id", ignore = true)
    Role toEntity(String authority);
}
