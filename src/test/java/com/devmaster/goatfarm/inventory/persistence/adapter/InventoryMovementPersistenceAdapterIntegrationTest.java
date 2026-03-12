package com.devmaster.goatfarm.inventory.persistence.adapter;

import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryIdempotencyVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementPersistedVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementResponseVO;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import com.devmaster.goatfarm.inventory.persistence.entity.InventoryItemEntity;
import com.devmaster.goatfarm.inventory.persistence.entity.InventoryLotEntity;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryBalanceRepository;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryIdempotencyRepository;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryItemRepository;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryLotRepository;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryMovementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class InventoryMovementPersistenceAdapterIntegrationTest {

    @Autowired
    private InventoryItemRepository itemRepository;

    @Autowired
    private InventoryBalanceRepository balanceRepository;

    @Autowired
    private InventoryMovementRepository movementRepository;

    @Autowired
    private InventoryIdempotencyRepository idempotencyRepository;

    @Autowired
    private InventoryLotRepository lotRepository;

    private InventoryMovementPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        adapter = new InventoryMovementPersistenceAdapter(
                itemRepository,
                balanceRepository,
                movementRepository,
                idempotencyRepository,
                lotRepository,
                objectMapper
        );
    }

    @Test
    void lockItemForUpdate_shouldReturnItemSnapshot() {
        Long farmId = 77L;
        InventoryItemEntity item = persistItem(farmId, "Racao mineral", true);

        var locked = adapter.lockItemForUpdate(farmId, item.getId());

        assertThat(locked).isPresent();
        assertThat(locked.get().itemId()).isEqualTo(item.getId());
        assertThat(locked.get().trackLot()).isTrue();
    }

    @Test
    void lockBalanceAndUpsert_shouldHandleNullAndNonNullLotId() {
        Long farmId = 88L;
        InventoryItemEntity item = persistItem(farmId, "Vacina clostridiose", true);
        Long itemId = item.getId();
        Long lotId = persistLot(farmId, itemId, "VAC-2026-01").getId();

        InventoryBalanceSnapshotVO firstNullLot = adapter.upsertBalance(
                new InventoryBalanceSnapshotVO(farmId, itemId, null, new BigDecimal("5.000"))
        );
        InventoryBalanceSnapshotVO secondNullLot = adapter.upsertBalance(
                new InventoryBalanceSnapshotVO(farmId, itemId, null, new BigDecimal("7.500"))
        );
        InventoryBalanceSnapshotVO nonNullLot = adapter.upsertBalance(
                new InventoryBalanceSnapshotVO(farmId, itemId, lotId, new BigDecimal("3.250"))
        );

        var lockedNullLot = adapter.lockBalanceForUpdate(farmId, itemId, null);
        var lockedNonNullLot = adapter.lockBalanceForUpdate(farmId, itemId, lotId);

        assertThat(firstNullLot.quantity()).isEqualByComparingTo("5.000");
        assertThat(secondNullLot.quantity()).isEqualByComparingTo("7.500");
        assertThat(nonNullLot.quantity()).isEqualByComparingTo("3.250");

        assertThat(lockedNullLot).isPresent();
        assertThat(lockedNullLot.get().lotId()).isNull();
        assertThat(lockedNullLot.get().quantity()).isEqualByComparingTo("7.500");

        assertThat(lockedNonNullLot).isPresent();
        assertThat(lockedNonNullLot.get().lotId()).isEqualTo(lotId);
        assertThat(lockedNonNullLot.get().quantity()).isEqualByComparingTo("3.250");

        assertThat(balanceRepository.findAll()).hasSize(2);
    }

    @Test
    void saveMovement_shouldPersistAndReturnGeneratedId() {
        Long farmId = 91L;
        Long itemId = persistItem(farmId, "Silagem", false).getId();

        InventoryMovementPersistedVO saved = adapter.saveMovement(new InventoryMovementPersistedVO(
                null,
                farmId,
                InventoryMovementType.IN,
                null,
                new BigDecimal("2.000"),
                itemId,
                null,
                LocalDate.of(2026, 2, 18),
                "Entrada de silagem",
                new BigDecimal("2.000"),
                OffsetDateTime.parse("2026-02-18T12:00:00Z")
        ));

        assertThat(saved.movementId()).isNotNull();
        assertThat(saved.farmId()).isEqualTo(farmId);
        assertThat(saved.itemId()).isEqualTo(itemId);
        assertThat(saved.resultingBalance()).isEqualByComparingTo("2.000");
    }

    @Test
    void saveAndFindIdempotency_shouldReturnPersistedResponseForReplay() {
        Long farmId = 95L;
        String idempotencyKey = "idem-replay-95";
        String requestHash = "abc123hash95";

        InventoryMovementResponseVO response = new InventoryMovementResponseVO(
                1234L,
                InventoryMovementType.OUT,
                new BigDecimal("1.250"),
                888L,
                null,
                LocalDate.of(2026, 2, 18),
                new BigDecimal("9.750"),
                OffsetDateTime.parse("2026-02-18T12:15:00Z")
        );

        InventoryIdempotencyVO saved = adapter.saveIdempotency(
                new InventoryIdempotencyVO(farmId, idempotencyKey, requestHash, response)
        );

        var found = adapter.findIdempotency(farmId, idempotencyKey);

        assertThat(saved.farmId()).isEqualTo(farmId);
        assertThat(saved.idempotencyKey()).isEqualTo(idempotencyKey);
        assertThat(saved.requestHash()).isEqualTo(requestHash);
        assertThat(saved.response().movementId()).isEqualTo(1234L);

        assertThat(found).isPresent();
        assertThat(found.get().requestHash()).isEqualTo(requestHash);
        assertThat(found.get().response().movementId()).isEqualTo(1234L);
        assertThat(found.get().response().resultingBalance()).isEqualByComparingTo("9.750");
    }

    private InventoryItemEntity persistItem(Long farmId, String name, boolean trackLot) {
        InventoryItemEntity item = new InventoryItemEntity();
        item.setFarmId(farmId);
        item.setName(name);
        item.setTrackLot(trackLot);
        item.setActive(true);
        return itemRepository.save(item);
    }

    private InventoryLotEntity persistLot(Long farmId, Long itemId, String code) {
        InventoryLotEntity lot = new InventoryLotEntity();
        lot.setFarmId(farmId);
        lot.setItemId(itemId);
        lot.setCode(code);
        lot.setActive(true);
        return lotRepository.save(lot);
    }
}
