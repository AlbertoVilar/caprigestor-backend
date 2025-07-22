package com.devmaster.goatfarm.farm.business.bo;

import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoatFarmFullResponseVO {

    private Long id;
    private String name;
    private String tod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Proprietário
    private Long ownerId;
    private String ownerName;
    private String ownerEmail; // ✅ Novo campo
    private String ownerCpf;   // ✅ Novo campo

    // Endereço
    private Long addressId;
    private String street;
    private String district;
    private String city;
    private String state;
    private String postalCode;

    // Telefones
    private List<PhoneResponseVO> phones;
}
