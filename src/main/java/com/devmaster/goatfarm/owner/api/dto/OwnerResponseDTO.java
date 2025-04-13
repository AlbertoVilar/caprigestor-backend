package com.devmaster.goatfarm.owner.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@Builder
public class OwnerResponseDTO {

    private Long id;
    private String name;
    private String cpf;
    private String email;

}
