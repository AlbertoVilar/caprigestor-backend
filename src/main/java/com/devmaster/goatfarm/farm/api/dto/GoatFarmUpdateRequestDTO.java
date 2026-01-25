package com.devmaster.goatfarm.farm.api.dto;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserUpdateRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GoatFarmUpdateRequestDTO {

    @Valid
    @NotNull(message = "Os dados da fazenda são obrigatórios.")
    private GoatFarmUpdateFarmDTO farm;

    @Valid
    @NotNull(message = "Os dados do usuário são obrigatórios.")
    private UserUpdateRequestDTO user;

    @Valid
    @NotNull(message = "Os dados do endereço são obrigatórios.")
    private AddressRequestDTO address;

    @Valid
    @NotEmpty(message = "É obrigatório informar ao menos um telefone.")
    private List<PhoneRequestDTO> phones;
}

