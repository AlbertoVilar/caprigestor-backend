package com.devmaster.goatfarm.farm.converters;

import com.devmaster.goatfarm.address.converter.AddressDTOConverter;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.converter.OwnerDTOConverter;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.converter.PhoneDTOConverter;

import java.util.List;
import java.util.stream.Collectors;

public class GoatFarmFullDTOConverter {

    public static GoatFarmFullRequestVO toVO(GoatFarmFullRequestDTO dto) {
        GoatFarmRequestVO farmVO = GoatFarmDTOConverter.toVO(dto.getFarm());
        OwnerRequestVO ownerVO = OwnerDTOConverter.toVO(dto.getOwner());
        var addressVO = AddressDTOConverter.toVO(dto.getAddress());

        List<PhoneRequestVO> phoneVOs = dto.getPhones().stream()
                .map(PhoneDTOConverter::toVO)
                .collect(Collectors.toList());

        return new GoatFarmFullRequestVO(farmVO, ownerVO, addressVO, phoneVOs);
    }
}
