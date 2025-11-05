package com.devmaster.goatfarm.farm.api.dto;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserUpdateRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GoatFarmUpdateRequestDTO {

    @Valid
    @NotNull
    private GoatFarmUpdateFarmDTO farm;

    @Valid
    @NotNull
    private UserUpdateRequestDTO user;

    @Valid
    @NotNull
    private AddressRequestDTO address;

    @Valid
    @NotNull
    private List<PhoneRequestDTO> phones;
}

