package com.devmaster.goatfarm.owner.business.bo;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OwnerRequestVO {

    private Long id;
    private String name;
    private String cpf;
    private String email;


}
