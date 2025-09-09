package com.devmaster.goatfarm.farm.api.dto;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GoatFarmUpdateRequestDTO {

    @NotNull(message = "Os dados da fazenda são obrigatórios.")
    private GoatFarmRequestDTO farm;

    @NotNull(message = "Dados do usuário são obrigatórios")
    private UserRequestDTO user;

    @NotNull(message = "Os dados do endereço são obrigatórios.")
    private AddressRequestDTO address;

    @NotNull(message = "A lista de telefones é obrigatória.")
    private List<PhoneRequestDTO> phones;
}
