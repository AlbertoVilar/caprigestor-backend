package com.devmaster.goatfarm.inventory.persistence.adapter;

import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementFilterVO;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryAdjustDirection;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import com.devmaster.goatfarm.inventory.persistence.entity.InventoryItemEntity;
import com.devmaster.goatfarm.inventory.persistence.entity.InventoryMovementEntity;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryItemRepository;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryMovementRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class InventoryMovementQueryPersistenceAdapterIntegrationTest {

    @Autowired
    private InventoryItemRepository itemRepository;

    @Autowired
    private InventoryMovementRepository movementRepository;

    @Autowired
    private EntityManager entityManager;

    private InventoryMovementQueryPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InventoryMovementQueryPersistenceAdapter(movementRepository, entityManager);
    }

    @Test
    void listMovements_shouldApplyFilters_andReturnItemNameInHistory() {
        Long farmId = 51L;
        InventoryItemEntity item = persistItem(farmId, "Ração Premium", true);
        persistMovement(farmId, item.getId(), 501L, InventoryMovementType.OUT, null, "2.000",
                LocalDate.of(2026, 2, 28), "Baixa por aplicação", "18.750", "2026-02-28T12:15:00Z");
        persistMovement(farmId, item.getId(), 501L, InventoryMovementType.IN, null, "5.000",
                LocalDate.of(2026, 2, 20), "Reposição", "20.750", "2026-02-20T09:00:00Z");

        var page = adapter.listMovements(new InventoryMovementFilterVO(
                farmId,
                item.getId(),
                501L,
                InventoryMovementType.OUT,
                LocalDate.of(2026, 2, 25),
                LocalDate.of(2026, 2, 28),
                PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "movementDate"))
        ));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).singleElement().satisfies(movement -> {
            assertThat(movement.itemName()).isEqualTo("Ração Premium");
            assertThat(movement.type()).isEqualTo(InventoryMovementType.OUT);
            assertThat(movement.reason()).isEqualTo("Baixa por aplicação");
            assertThat(movement.resultingBalance()).isEqualByComparingTo("18.750");
        });
    }

    @Test
    void listMovements_shouldRespectOrderingAndPagination() {
        Long farmId = 52L;
        InventoryItemEntity item = persistItem(farmId, "Milho", false);
        persistMovement(farmId, item.getId(), null, InventoryMovementType.IN, null, "3.000",
                LocalDate.of(2026, 2, 10), "Entrada", "3.000", "2026-02-10T08:00:00Z");
        persistMovement(farmId, item.getId(), null, InventoryMovementType.ADJUST, InventoryAdjustDirection.INCREASE, "1.500",
                LocalDate.of(2026, 2, 12), "Ajuste", "4.500", "2026-02-12T11:00:00Z");

        var page = adapter.listMovements(new InventoryMovementFilterVO(
                farmId,
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "movementDate").and(Sort.by(Sort.Direction.DESC, "createdAt")))
        ));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).singleElement().satisfies(movement -> {
            assertThat(movement.type()).isEqualTo(InventoryMovementType.ADJUST);
            assertThat(movement.itemName()).isEqualTo("Milho");
            assertThat(movement.movementDate()).isEqualTo(LocalDate.of(2026, 2, 12));
        });
    }

    private InventoryItemEntity persistItem(Long farmId, String name, boolean trackLot) {
        InventoryItemEntity item = new InventoryItemEntity();
        item.setFarmId(farmId);
        item.setName(name);
        item.setTrackLot(trackLot);
        item.setActive(true);
        return itemRepository.save(item);
    }

    private void persistMovement(
            Long farmId,
            Long itemId,
            Long lotId,
            InventoryMovementType type,
            InventoryAdjustDirection adjustDirection,
            String quantity,
            LocalDate movementDate,
            String reason,
            String resultingBalance,
            String createdAt
    ) {
        InventoryMovementEntity entity = new InventoryMovementEntity();
        entity.setFarmId(farmId);
        entity.setItemId(itemId);
        entity.setLotId(lotId);
        entity.setType(type);
        entity.setAdjustDirection(adjustDirection);
        entity.setQuantity(new BigDecimal(quantity));
        entity.setMovementDate(movementDate);
        entity.setReason(reason);
        entity.setResultingBalance(new BigDecimal(resultingBalance));
        entity.setCreatedAt(OffsetDateTime.parse(createdAt));
        movementRepository.save(entity);
    }
}
