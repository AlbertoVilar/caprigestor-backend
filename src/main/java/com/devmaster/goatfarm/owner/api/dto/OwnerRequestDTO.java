package com.devmaster.goatfarm.owner.api.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerRequestDTO {


    private String name;
    private String cpf;
    private String email;
}
