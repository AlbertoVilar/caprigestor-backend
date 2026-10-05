package com.devmaster.goatfarm.inventory.persistence.adapter;

import com.devmaster.goatfarm.inventory.application.ports.out.InventoryLotPersistencePort;
import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.application.pagination.SortDirection;
import com.devmaster.goatfarm.application.pagination.SortSpec;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotCreateVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotResponseVO;
import com.devmaster.goatfarm.inventory.persistence.entity.InventoryLotEntity;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryItemRepository;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryLotRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InventoryLotPersistenceAdapter implements InventoryLotPersistencePort {

    private final InventoryLotRepository lotRepository;
    private final InventoryItemRepository itemRepository;

    public InventoryLotPersistenceAdapter(
            InventoryLotRepository lotRepository,
            InventoryItemRepository itemRepository
    ) {
        this.lotRepository = lotRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public InventoryLotResponseVO save(InventoryLotCreateVO lot) {
        InventoryLotEntity entity = new InventoryLotEntity();
        entity.setFarmId(lot.farmId());
        entity.setItemId(lot.itemId());
        entity.setCode(lot.code());
        entity.setDescription(lot.description());
        entity.setExpirationDate(lot.expirationDate());
        entity.setActive(lot.active());

        return toResponseVO(lotRepository.save(entity));
    }

    @Override
    public PageResult<InventoryLotResponseVO> listLots(InventoryLotFilterVO filter, PageQuery page) {
        var springPage = lotRepository.searchLots(filter.farmId(), filter.itemId(), filter.active(), toPageable(page));
        return new PageResult<>(springPage.getContent().stream().map(this::toResponseVO).toList(),
                springPage.getTotalElements(), page.page(), page.size());
    }

    private Pageable toPageable(PageQuery page) {
        var orders = page.sort().stream().map(this::toOrder).toList();
        return PageRequest.of(page.page(), page.size(), Sort.by(orders));
    }

    private Sort.Order toOrder(SortSpec spec) {
        return spec.direction() == SortDirection.ASC
                ? Sort.Order.asc(spec.field())
                : Sort.Order.desc(spec.field());
    }

    @Override
    public Optional<InventoryLotResponseVO> findByFarmIdAndId(Long farmId, Long lotId) {
        return lotRepository.findByFarmIdAndId(farmId, lotId)
                .map(this::toResponseVO);
    }

    @Override
    public Optional<InventoryLotResponseVO> findByFarmIdAndItemIdAndCodeNormalized(Long farmId, Long itemId, String codeNormalized) {
        return lotRepository.findByFarmIdAndItemIdAndCodeNormalized(farmId, itemId, codeNormalized)
                .map(this::toResponseVO);
    }

    @Override
    public Optional<InventoryItemSnapshotVO> findItemSnapshot(Long farmId, Long itemId) {
        return itemRepository.findByFarmIdAndId(farmId, itemId)
                .map(entity -> new InventoryItemSnapshotVO(entity.getId(), entity.isTrackLot()));
    }

    @Override
    public Optional<InventoryLotResponseVO> updateActive(Long farmId, Long lotId, boolean active) {
        return lotRepository.findByFarmIdAndId(farmId, lotId)
                .map(entity -> {
                    entity.setActive(active);
                    return toResponseVO(lotRepository.save(entity));
                });
    }

    private InventoryLotResponseVO toResponseVO(InventoryLotEntity entity) {
        return new InventoryLotResponseVO(
                entity.getId(),
                entity.getFarmId(),
                entity.getItemId(),
                entity.getCode(),
                entity.getDescription(),
                entity.getExpirationDate(),
                entity.isActive()
        );
    }
}
