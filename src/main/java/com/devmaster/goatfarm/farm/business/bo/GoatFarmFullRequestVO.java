package com.devmaster.goatfarm.farm.business.bo;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoatFarmFullRequestVO {

    private GoatFarmRequestVO farm;
    private OwnerRequestVO owner;
    private AddressRequestVO address;
    private List<PhoneRequestVO> phones;
}
