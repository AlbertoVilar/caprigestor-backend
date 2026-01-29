package com.devmaster.goatfarm.goat.mapper;

import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm; // CORRIGIDO: Importação correta para GoatFarm
import com.devmaster.goatfarm.authority.persistence.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GoatMapper {
    @Mapping(target = "fatherName", source = "father.name")
    @Mapping(target = "fatherRegistrationNumber", source = "father.registrationNumber")
    @Mapping(target = "motherName", source = "mother.name")
    @Mapping(target = "motherRegistrationNumber", source = "mother.registrationNumber")
    @Mapping(target = "userName", source = "user.name")
    @Mapping(target = "farmId", source = "farm.id")
    @Mapping(target = "farmName", source = "farm.name")
    GoatResponseVO toResponseVO(Goat entity);

    GoatResponseDTO toResponseDTO(GoatResponseVO vo);

    GoatRequestVO toRequestVO(GoatRequestDTO dto);

    // Para toEntity:
    // - 'id' é ignorado defensivamente, caso exista no VO e não na Entity Goat.
    // - 'registrationNumber' é a PK e deve ser mapeada do VO.
    // - 'user', 'farm', 'father', 'mother' são setados na camada Business.
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "farm", ignore = true)
    @Mapping(target = "father", ignore = true)
    @Mapping(target = "mother", ignore = true)
    Goat toEntity(GoatRequestVO vo);

    // Para updateEntity:
    // - 'id' é ignorado defensivamente.
    // - 'registrationNumber' é a PK e não deve ser alterada em um update via VO.
    // - 'user', 'farm' são ignorados, pois não devem ser atualizados via VO aqui.
    // - Propriedades ambíguas (name, gender, etc.) são explicitamente mapeadas de 'vo'.
    // - 'father' e 'mother' são mapeados dos parâmetros.
    @Mapping(target = "registrationNumber", ignore = true) // PK não deve ser atualizada via VO
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "farm", ignore = true)
    @Mapping(target = "name", source = "vo.name")
    @Mapping(target = "gender", source = "vo.gender")
    @Mapping(target = "breed", source = "vo.breed")
    @Mapping(target = "color", source = "vo.color")
    @Mapping(target = "birthDate", source = "vo.birthDate")
    @Mapping(target = "status", source = "vo.status")
    @Mapping(target = "tod", source = "vo.tod")
    @Mapping(target = "toe", source = "vo.toe")
    @Mapping(target = "category", source = "vo.category")
    @Mapping(target = "father", source = "father")
    @Mapping(target = "mother", source = "mother")
    void updateEntity(@MappingTarget Goat entity, GoatRequestVO vo, Goat father, Goat mother);
}
