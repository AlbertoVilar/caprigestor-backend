package com.devmaster.goatfarm.milk.business.mapper;

import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.domain.Lactation;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LactationBusinessMapper {
    LactationResponseVO toResponseVO(Lactation lactation);

    List<LactationResponseVO> toResponseVOList(List<Lactation> lactations);
}
