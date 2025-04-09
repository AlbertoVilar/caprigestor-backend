package com.devmaster.goatfarm.farm.converter;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import org.springframework.stereotype.Component;

@Component
public class GoatFarmConverter {

    public static GoatFarm toEntity(GoatFarmRequestVO requestVO) {

        return new GoatFarm(

                null,
                requestVO.getName(),
                requestVO.getTod()

        );

    }

    public static void entityUpdate(GoatFarm goatFarm, GoatFarmRequestVO requestVO) {

        goatFarm.setId(requestVO.getId());
        goatFarm.setName(goatFarm.getName());
        goatFarm.setTod(goatFarm.getTod());

    }

    public static GoatFarmResponseVO toVO(GoatFarm goatFarm) {

        return new GoatFarmResponseVO(

                goatFarm.getId(),
                goatFarm.getName(),
                goatFarm.getTod(),
                goatFarm.getCreatedAt(),
                goatFarm.getUpdatedAt()
        );
    }
}
