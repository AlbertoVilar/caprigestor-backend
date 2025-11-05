package com.devmaster.goatfarm.farm.api.dto;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GoatFarmUpdateRequestDTO {

    @NotNull(message = "Os dados da fazenda sÃ£o obrigatÃ³rios.")
    @Valid
    private GoatFarmRequestDTO farm;

    @NotNull(message = "Dados do usuÃ¡rio sÃ£o obrigatÃ³rios")
    @Valid
    private UserRequestDTO user;

    @NotNull(message = "Os dados do endereÃ§o sÃ£o obrigatÃ³rios.")
    @Valid
    private AddressRequestDTO address;

    @NotNull(message = "A lista de telefones Ã© obrigatÃ³ria.")
    @Valid
    private List<PhoneRequestDTO> phones;

        public GoatFarmRequestDTO getFarm() { return farm; }
    public void setFarm(GoatFarmRequestDTO farm) { this.farm = farm; }
    
    public UserRequestDTO getUser() { return user; }
    public void setUser(UserRequestDTO user) { this.user = user; }
    
    public AddressRequestDTO getAddress() { return address; }
    public void setAddress(AddressRequestDTO address) { this.address = address; }
    
    public List<PhoneRequestDTO> getPhones() { return phones; }
    public void setPhones(List<PhoneRequestDTO> phones) { this.phones = phones; }
}

