package com.devmaster.goatfarm.farm.business.bo;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.authority.business.bo.UserRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoatFarmFullRequestVO {

    private GoatFarmRequestVO farm;
    private UserRequestVO user;
    private AddressRequestVO address;
    private List<PhoneRequestVO> phones;
}
