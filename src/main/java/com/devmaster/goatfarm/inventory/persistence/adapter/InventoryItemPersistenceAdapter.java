package com.devmaster.goatfarm.inventory.persistence.adapter;

import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryItemPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemCreateVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemResponseVO;
import com.devmaster.goatfarm.inventory.persistence.entity.InventoryItemEntity;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryItemRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class InventoryItemPersistenceAdapter implements InventoryItemPersistencePort {

    private static final String UNIQUE_ITEM_CONSTRAINT = "uk_inventory_item_farm_name_normalized";

    private final InventoryItemRepository itemRepository;

    public InventoryItemPersistenceAdapter(InventoryItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    public InventoryItemResponseVO save(InventoryItemCreateVO item) {
        InventoryItemEntity entity = new InventoryItemEntity();
        entity.setFarmId(item.farmId());
        entity.setName(item.name());
        entity.setTrackLot(item.trackLot());
        entity.setActive(item.active());

        try {
            InventoryItemEntity saved = itemRepository.saveAndFlush(entity);
            return toResponseVO(saved);
        } catch (DataIntegrityViolationException ex) {
            if (isDuplicateItemConstraint(ex)) {
                throw new DuplicateEntityException(
                        "name",
                        "Já existe um item de estoque com esse nome nesta fazenda."
                );
            }
            throw ex;
        }
    }

    @Override
    public Page<InventoryItemResponseVO> listByFarmId(Long farmId, Pageable pageable) {
        return itemRepository.findByFarmId(farmId, pageable).map(this::toResponseVO);
    }

    private InventoryItemResponseVO toResponseVO(InventoryItemEntity entity) {
        return new InventoryItemResponseVO(
                entity.getId(),
                entity.getFarmId(),
                entity.getName(),
                entity.isTrackLot(),
                entity.isActive()
        );
    }

    private boolean isDuplicateItemConstraint(DataIntegrityViolationException ex) {
        Throwable rootCause = ex.getRootCause();
        String message = rootCause != null ? rootCause.getMessage() : ex.getMessage();
        return message != null && message.toLowerCase().contains(UNIQUE_ITEM_CONSTRAINT);
    }
}
