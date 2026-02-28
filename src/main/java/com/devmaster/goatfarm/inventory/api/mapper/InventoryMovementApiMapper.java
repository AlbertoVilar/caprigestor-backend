package com.devmaster.goatfarm.inventory.api.mapper;

import com.devmaster.goatfarm.inventory.api.dto.InventoryMovementCreateRequestDTO;
import com.devmaster.goatfarm.inventory.api.dto.InventoryMovementHistoryResponseDTO;
import com.devmaster.goatfarm.inventory.api.dto.InventoryMovementResponseDTO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementHistoryResponseVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementResponseVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMovementApiMapper {

    InventoryMovementCreateRequestVO toRequestVO(InventoryMovementCreateRequestDTO dto);

    InventoryMovementResponseDTO toResponseDTO(InventoryMovementResponseVO vo);

    InventoryMovementHistoryResponseDTO toHistoryResponseDTO(InventoryMovementHistoryResponseVO vo);
}
