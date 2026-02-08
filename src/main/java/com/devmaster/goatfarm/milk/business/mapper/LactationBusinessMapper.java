package com.devmaster.goatfarm.milk.business.mapper;

import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.persistence.entity.Lactation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LactationBusinessMapper {
    LactationResponseVO toResponseVO(Lactation entity);

    List<LactationResponseVO> toResponseVOList(List<Lactation> entities);
}
