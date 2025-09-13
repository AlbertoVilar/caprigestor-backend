package com.devmaster.goatfarm.farm.business.bo;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GoatFarmUpdateRequestVO {


    private GoatFarmRequestDTO farm;


    // Funcionalidade de proprietário migrada para entidade User


    private AddressRequestDTO address;


    private List<PhoneRequestDTO> phones;
}
