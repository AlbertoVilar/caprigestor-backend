package com.devmaster.goatfarm.inventory.api.mapper;

import com.devmaster.goatfarm.inventory.api.dto.InventoryLotActivationRequestDTO;
import com.devmaster.goatfarm.inventory.api.dto.InventoryLotCreateRequestDTO;
import com.devmaster.goatfarm.inventory.api.dto.InventoryLotResponseDTO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotActivationRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotResponseVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryLotApiMapper {

    InventoryLotCreateRequestVO toCreateRequestVO(InventoryLotCreateRequestDTO dto);

    InventoryLotActivationRequestVO toActivationRequestVO(InventoryLotActivationRequestDTO dto);

    InventoryLotResponseDTO toResponseDTO(InventoryLotResponseVO vo);
}
