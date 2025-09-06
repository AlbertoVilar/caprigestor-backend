package com.devmaster.goatfarm.owner.converter;

import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;
import com.devmaster.goatfarm.owner.model.entity.Owner;
import org.springframework.stereotype.Component;

@Component
public class OwnerEntityConverter {

    public static Owner toEntity(OwnerRequestVO requestVO) {

        return new Owner(

                requestVO.getId(),
                requestVO.getName(),
                requestVO.getCpf(),
                requestVO.getEmail()

        );

    }

    public static void entityUpdate(Owner owner, OwnerRequestVO requestVO) {

        owner.setName(requestVO.getName());
        owner.setCpf(requestVO.getCpf());
        owner.setEmail(requestVO.getEmail());

    }

    public static OwnerResponseVO toVO(Owner owner) {

        return new OwnerResponseVO(

                owner.getId(),
                owner.getName(),
                owner.getCpf(),
                owner.getEmail()

        );
    }
}
