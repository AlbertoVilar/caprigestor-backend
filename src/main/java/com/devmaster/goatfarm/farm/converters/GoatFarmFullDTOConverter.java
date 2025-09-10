package com.devmaster.goatfarm.farm.converters;

import com.devmaster.goatfarm.address.converter.AddressDTOConverter;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullRequestDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.authority.conveter.UserDTOConverter;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.converter.PhoneDTOConverter;

import java.util.List;
import java.util.stream.Collectors;

public class GoatFarmFullDTOConverter {

    public static GoatFarmFullRequestVO toVO(GoatFarmFullRequestDTO dto) {
        // VERIFICAÇÃO DE NULO -- ADICIONE ESTA VERIFICAÇÃO
        if (dto == null) {
            return null;
        }
        
        GoatFarmRequestVO farmVO = GoatFarmDTOConverter.toVO(dto.getFarm());
        UserRequestVO userVO = UserDTOConverter.toVO(dto.getUser());
        var addressVO = AddressDTOConverter.toVO(dto.getAddress());

        List<PhoneRequestVO> phoneVOs = dto.getPhones() != null ? 
                dto.getPhones().stream()
                        .map(PhoneDTOConverter::toVO)
                        .collect(Collectors.toList()) : 
                null;

        return new GoatFarmFullRequestVO(farmVO, userVO, addressVO, phoneVOs);
    }
}
