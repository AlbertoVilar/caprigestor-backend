package com.devmaster.goatfarm.farm.api.dto;

import com.devmaster.goatfarm.address.api.dto.AddressResponseDTO;
import com.devmaster.goatfarm.authority.api.dto.UserResponseDTO;
import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.time.LocalDateTime;

@Getter
@Setter
public class GoatFarmFullResponseDTO {
    private Long id;
    private String name;
    private String tod;
    private UserResponseDTO user;
    private AddressResponseDTO address;
    private List<PhoneResponseDTO> phones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
}
