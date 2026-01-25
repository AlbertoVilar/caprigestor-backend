package com.devmaster.goatfarm.farm.api.dto;

import com.devmaster.goatfarm.address.api.dto.AddressRequestDTO;
import com.devmaster.goatfarm.authority.api.dto.UserRequestDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneRequestDTO;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class GoatFarmFullRequestDTO {

    @NotNull(message = "Os dados da fazenda são obrigatórios.")
    private GoatFarmRequestDTO farm;

    @NotNull(message = "Dados do usuário são obrigatórios")
    private UserRequestDTO user;

    @NotNull(message = "Os dados do endereço são obrigatórios.")
    private AddressRequestDTO address;

    @NotEmpty(message = "É obrigatório informar ao menos um telefone.")
    private List<PhoneRequestDTO> phones;

    public GoatFarmFullRequestDTO() {
    }

    public GoatFarmFullRequestDTO(GoatFarmRequestDTO farm, UserRequestDTO user, AddressRequestDTO address, List<PhoneRequestDTO> phones) {
        this.farm = farm;
        this.user = user;
        this.address = address;
        this.phones = phones;
    }

    public GoatFarmRequestDTO getFarm() {
        return farm;
    }

    public void setFarm(GoatFarmRequestDTO farm) {
        this.farm = farm;
    }

    public UserRequestDTO getUser() {
        return user;
    }

    public void setUser(UserRequestDTO user) {
        this.user = user;
    }

    public AddressRequestDTO getAddress() {
        return address;
    }

    public void setAddress(AddressRequestDTO address) {
        this.address = address;
    }

    public List<PhoneRequestDTO> getPhones() {
        return phones;
    }

    public void setPhones(List<PhoneRequestDTO> phones) {
        this.phones = phones;
    }
}
