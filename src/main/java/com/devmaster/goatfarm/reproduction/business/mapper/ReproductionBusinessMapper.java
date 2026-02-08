package com.devmaster.goatfarm.reproduction.business.mapper;

import com.devmaster.goatfarm.reproduction.business.bo.PregnancyResponseVO;
import com.devmaster.goatfarm.reproduction.business.bo.ReproductiveEventResponseVO;
import com.devmaster.goatfarm.reproduction.persistence.entity.Pregnancy;
import com.devmaster.goatfarm.reproduction.persistence.entity.ReproductiveEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReproductionBusinessMapper {
    PregnancyResponseVO toPregnancyResponseVO(Pregnancy entity);

    ReproductiveEventResponseVO toReproductiveEventResponseVO(ReproductiveEvent entity);
}
