package com.devmaster.goatfarm.farm.api.dto;

import com.devmaster.goatfarm.phone.api.dto.PhoneResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public record GoatFarmFullResponseDTO(
        Long id,
        String name,
        String tod,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,

        Long ownerId,
        String ownerName,
        String ownerEmail,     // ✅ novo
        String ownerCpf,       // ✅ novo

        Long addressId,
        String street,
        String district,
        String city,
        String state,
        String cep,

        List<PhoneResponseDTO> phones
) {}

