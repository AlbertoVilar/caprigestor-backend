package com.devmaster.goatfarm.inventory.persistence.adapter;

import com.devmaster.goatfarm.inventory.application.ports.out.InventoryLotPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotCreateVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotResponseVO;
import com.devmaster.goatfarm.inventory.persistence.entity.InventoryLotEntity;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryItemRepository;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryLotRepository;
import org.springframework.data.domain.Page;
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
    public Page<InventoryLotResponseVO> listLots(InventoryLotFilterVO filter) {
        return lotRepository.searchLots(filter.farmId(), filter.itemId(), filter.active(), filter.pageable())
                .map(this::toResponseVO);
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
