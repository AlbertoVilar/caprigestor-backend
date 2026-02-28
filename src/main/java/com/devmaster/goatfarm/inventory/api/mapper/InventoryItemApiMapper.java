package com.devmaster.goatfarm.inventory.api.mapper;

import com.devmaster.goatfarm.inventory.api.dto.InventoryItemCreateRequestDTO;
import com.devmaster.goatfarm.inventory.api.dto.InventoryItemResponseDTO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemResponseVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryItemApiMapper {

    InventoryItemCreateRequestVO toCreateRequestVO(InventoryItemCreateRequestDTO dto);

    InventoryItemResponseDTO toResponseDTO(InventoryItemResponseVO vo);
}
