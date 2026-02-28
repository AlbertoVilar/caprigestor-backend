package com.devmaster.goatfarm.inventory.persistence.adapter;

import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceFilterVO;
import com.devmaster.goatfarm.inventory.persistence.entity.InventoryBalanceEntity;
import com.devmaster.goatfarm.inventory.persistence.entity.InventoryItemEntity;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryBalanceRepository;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class InventoryBalanceQueryPersistenceAdapterIntegrationTest {

    @Autowired
    private InventoryItemRepository itemRepository;

    @Autowired
    private InventoryBalanceRepository balanceRepository;

    private InventoryBalanceQueryPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new InventoryBalanceQueryPersistenceAdapter(balanceRepository);
    }

    @Test
    void listBalances_shouldApplyFilters_andReturnItemNameWithoutAdditionalLookups() {
        Long farmId = 41L;
        InventoryItemEntity activeItem = persistItem(farmId, "Ração Premium", true, true);
        InventoryItemEntity inactiveItem = persistItem(farmId, "Suplemento Antigo", false, false);
        persistBalance(farmId, activeItem.getId(), 501L, "18.750");
        persistBalance(farmId, activeItem.getId(), 502L, "4.000");
        persistBalance(farmId, inactiveItem.getId(), null, "3.500");

        var page = adapter.listBalances(new InventoryBalanceFilterVO(
                farmId,
                activeItem.getId(),
                501L,
                true,
                PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "itemId"))
        ));

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).singleElement().satisfies(balance -> {
            assertThat(balance.itemId()).isEqualTo(activeItem.getId());
            assertThat(balance.itemName()).isEqualTo("Ração Premium");
            assertThat(balance.trackLot()).isTrue();
            assertThat(balance.lotId()).isEqualTo(501L);
            assertThat(balance.quantity()).isEqualByComparingTo("18.750");
        });
    }

    @Test
    void listBalances_shouldRespectPagination_andAllowInactiveItemsWhenRequested() {
        Long farmId = 42L;
        InventoryItemEntity first = persistItem(farmId, "Milho", false, true);
        InventoryItemEntity second = persistItem(farmId, "Suplemento", false, false);
        persistBalance(farmId, first.getId(), null, "9.000");
        persistBalance(farmId, second.getId(), null, "1.250");

        var page = adapter.listBalances(new InventoryBalanceFilterVO(
                farmId,
                null,
                null,
                false,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "itemId"))
        ));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).singleElement().satisfies(balance -> {
            assertThat(balance.itemId()).isEqualTo(first.getId());
            assertThat(balance.itemName()).isEqualTo("Milho");
        });
    }

    private InventoryItemEntity persistItem(Long farmId, String name, boolean trackLot, boolean active) {
        InventoryItemEntity item = new InventoryItemEntity();
        item.setFarmId(farmId);
        item.setName(name);
        item.setTrackLot(trackLot);
        item.setActive(active);
        return itemRepository.save(item);
    }

    private void persistBalance(Long farmId, Long itemId, Long lotId, String quantity) {
        InventoryBalanceEntity entity = new InventoryBalanceEntity();
        entity.setFarmId(farmId);
        entity.setItemId(itemId);
        entity.setLotId(lotId);
        entity.setQuantity(new BigDecimal(quantity));
        balanceRepository.save(entity);
    }
}
