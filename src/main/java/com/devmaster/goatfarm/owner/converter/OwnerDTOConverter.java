package com.devmaster.goatfarm.owner.converter;

import com.devmaster.goatfarm.owner.api.dto.OwnerRequestDTO;
import com.devmaster.goatfarm.owner.api.dto.OwnerResponseDTO;
import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;
import org.springframework.stereotype.Component;

@Component
public class OwnerDTOConverter {

    public static OwnerResponseDTO toDTO(OwnerResponseVO responseVO) {

        return new OwnerResponseDTO(

                responseVO.getId(),
                responseVO.getName(),
                responseVO.getCpf(),
                responseVO.getEmail()
        );
    }

    public static OwnerRequestVO toVO(OwnerRequestDTO requestDTO) {

        return new OwnerRequestVO(


                requestDTO.getName(),
                requestDTO.getCpf(),
                requestDTO.getEmail()
        );
    }
}
