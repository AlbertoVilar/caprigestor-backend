package com.devmaster.goatfarm.inventory.persistence.adapter;

import com.devmaster.goatfarm.inventory.application.ports.out.InventoryMovementPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryIdempotencyVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementPersistedVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementResponseVO;
import com.devmaster.goatfarm.inventory.persistence.entity.InventoryBalanceEntity;
import com.devmaster.goatfarm.inventory.persistence.entity.InventoryIdempotencyEntity;
import com.devmaster.goatfarm.inventory.persistence.entity.InventoryMovementEntity;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryBalanceRepository;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryIdempotencyRepository;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryItemRepository;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryMovementRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

@Component
public class InventoryMovementPersistenceAdapter implements InventoryMovementPersistencePort {

    private final InventoryItemRepository itemRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final InventoryMovementRepository movementRepository;
    private final InventoryIdempotencyRepository idempotencyRepository;
    private final ObjectMapper objectMapper;

    public InventoryMovementPersistenceAdapter(
            InventoryItemRepository itemRepository,
            InventoryBalanceRepository balanceRepository,
            InventoryMovementRepository movementRepository,
            InventoryIdempotencyRepository idempotencyRepository,
            ObjectMapper objectMapper
    ) {
        this.itemRepository = itemRepository;
        this.balanceRepository = balanceRepository;
        this.movementRepository = movementRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<InventoryItemSnapshotVO> findItemSnapshot(Long farmId, Long itemId) {
        return itemRepository.findByFarmIdAndId(farmId, itemId)
                .map(entity -> new InventoryItemSnapshotVO(entity.getId(), entity.isTrackLot()));
    }

    @Override
    public Optional<InventoryIdempotencyVO> findIdempotency(Long farmId, String idempotencyKey) {
        return idempotencyRepository.findByFarmIdAndIdempotencyKey(farmId, idempotencyKey)
                .map(this::toIdempotencyVO);
    }

    @Override
    public InventoryIdempotencyVO saveIdempotency(InventoryIdempotencyVO vo) {
        InventoryIdempotencyEntity entity = new InventoryIdempotencyEntity();
        entity.setFarmId(vo.farmId());
        entity.setIdempotencyKey(vo.idempotencyKey());
        entity.setRequestHash(vo.requestHash());
        entity.setResponsePayload(writeResponse(vo.response()));
        entity.setCreatedAt(OffsetDateTime.now());

        InventoryIdempotencyEntity saved = idempotencyRepository.save(entity);
        return toIdempotencyVO(saved);
    }

    @Override
    public InventoryMovementPersistedVO saveMovement(InventoryMovementPersistedVO vo) {
        InventoryMovementEntity entity = new InventoryMovementEntity();
        entity.setFarmId(vo.farmId());
        entity.setType(vo.type());
        entity.setAdjustDirection(vo.adjustDirection());
        entity.setQuantity(vo.quantity());
        entity.setItemId(vo.itemId());
        entity.setLotId(vo.lotId());
        entity.setMovementDate(vo.movementDate());
        entity.setReason(vo.reason());
        entity.setResultingBalance(vo.resultingBalance());
        entity.setCreatedAt(vo.createdAt());

        InventoryMovementEntity saved = movementRepository.save(entity);

        return new InventoryMovementPersistedVO(
                saved.getId(),
                saved.getFarmId(),
                saved.getType(),
                saved.getAdjustDirection(),
                saved.getQuantity(),
                saved.getItemId(),
                saved.getLotId(),
                saved.getMovementDate(),
                saved.getReason(),
                saved.getResultingBalance(),
                saved.getCreatedAt()
        );
    }

    @Override
    public Optional<InventoryItemSnapshotVO> lockItemForUpdate(Long farmId, Long itemId) {
        return itemRepository.findByFarmIdAndIdForUpdate(farmId, itemId)
                .map(entity -> new InventoryItemSnapshotVO(entity.getId(), entity.isTrackLot()));
    }

    @Override
    public Optional<InventoryBalanceSnapshotVO> lockBalanceForUpdate(Long farmId, Long itemId, Long lotId) {
        return balanceRepository.findByBusinessKeyForUpdate(farmId, itemId, lotId)
                .map(entity -> new InventoryBalanceSnapshotVO(
                        entity.getFarmId(),
                        entity.getItemId(),
                        entity.getLotId(),
                        entity.getQuantity()
                ));
    }

    @Override
    public InventoryBalanceSnapshotVO upsertBalance(InventoryBalanceSnapshotVO vo) {
        InventoryBalanceEntity entity = balanceRepository.findByBusinessKey(vo.farmId(), vo.itemId(), vo.lotId())
                .orElseGet(InventoryBalanceEntity::new);

        entity.setFarmId(vo.farmId());
        entity.setItemId(vo.itemId());
        entity.setLotId(vo.lotId());
        entity.setQuantity(vo.quantity());

        InventoryBalanceEntity saved = balanceRepository.save(entity);
        return new InventoryBalanceSnapshotVO(saved.getFarmId(), saved.getItemId(), saved.getLotId(), saved.getQuantity());
    }

    private InventoryIdempotencyVO toIdempotencyVO(InventoryIdempotencyEntity entity) {
        InventoryMovementResponseVO response = readResponse(entity.getResponsePayload());
        return new InventoryIdempotencyVO(entity.getFarmId(), entity.getIdempotencyKey(), entity.getRequestHash(), response);
    }

    private String writeResponse(InventoryMovementResponseVO response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar resposta de idempotencia.", e);
        }
    }

    private InventoryMovementResponseVO readResponse(String payload) {
        try {
            return objectMapper.readValue(payload, InventoryMovementResponseVO.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao desserializar resposta de idempotencia.", e);
        }
    }
}
