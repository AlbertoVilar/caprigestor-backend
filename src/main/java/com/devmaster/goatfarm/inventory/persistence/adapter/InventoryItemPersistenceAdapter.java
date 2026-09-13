package com.devmaster.goatfarm.inventory.persistence.adapter;

import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.application.pagination.SortDirection;
import com.devmaster.goatfarm.application.pagination.SortSpec;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryItemPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemCreateVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemResponseVO;
import com.devmaster.goatfarm.inventory.persistence.entity.InventoryItemEntity;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryItemRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public PageResult<InventoryItemResponseVO> listByFarmId(Long farmId, PageQuery page) {
        var springPage = itemRepository.findByFarmId(farmId, toPageable(page));
        return new PageResult<>(springPage.getContent().stream().map(this::toResponseVO).toList(),
                springPage.getTotalElements(), page.page(), page.size());
    }

    private Pageable toPageable(PageQuery page) {
        var orders = page.sort().stream()
                .map(this::toOrder)
                .toList();
        return PageRequest.of(page.page(), page.size(), Sort.by(orders));
    }

    private Sort.Order toOrder(SortSpec spec) {
        return spec.direction() == SortDirection.ASC
                ? Sort.Order.asc(spec.field())
                : Sort.Order.desc(spec.field());
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
