package com.devmaster.goatfarm.inventory.api.mapper;

import com.devmaster.goatfarm.inventory.api.dto.InventoryBalanceResponseDTO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceResponseVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryBalanceApiMapper {

    InventoryBalanceResponseDTO toResponseDTO(InventoryBalanceResponseVO vo);
}
