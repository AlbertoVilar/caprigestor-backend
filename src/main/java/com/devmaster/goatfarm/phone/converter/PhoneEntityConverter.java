package com.devmaster.goatfarm.phone.converter;

import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import org.springframework.stereotype.Component;

@Component
public class PhoneEntityConverter {

    public static Phone toEntity(PhoneRequestVO requestVO, GoatFarm capril) {
        return new Phone(
                requestVO.getId(),
                requestVO.getDdd(),
                requestVO.getNumber(),
                capril
        );
    }

    public static void toUpdateEntity(Phone phone, PhoneRequestVO requestVO) {
        phone.setDdd(requestVO.getDdd());
        phone.setNumber(requestVO.getNumber());
    }

    public static PhoneResponseVO toVO(Phone phone) {
        return new PhoneResponseVO(
                phone.getId(),
                phone.getDdd(),
                phone.getNumber()
        );
    }
}
