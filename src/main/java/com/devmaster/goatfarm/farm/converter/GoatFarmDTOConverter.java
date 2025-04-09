package com.devmaster.goatfarm.farm.converter;

import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import org.springframework.stereotype.Component;

@Component
public class GoatFarmDTOConverter {

    public static GoatFarmResponseDTO toDTO(GoatFarmResponseVO responseVO) {

        return new GoatFarmResponseDTO(

                responseVO.getId(),
                responseVO.getName(),
                responseVO.getTod(),

                responseVO.getCreatedAt(),
                responseVO.getUpdatedAt()

        );
    }

    public static GoatFarmRequestVO toVO(GoatFarmRequestDTO requestDTO) {

        return new GoatFarmRequestVO(

                requestDTO.getId(),
                requestDTO.getName(),
                requestDTO.getTod()

        );
    }
}
