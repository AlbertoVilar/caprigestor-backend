package com.devmaster.goatfarm.owner.business.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@Builder
public class OwnerResponseVO {

    private Long id;
    private String name;
    private String cpf;
    private String email;

}
