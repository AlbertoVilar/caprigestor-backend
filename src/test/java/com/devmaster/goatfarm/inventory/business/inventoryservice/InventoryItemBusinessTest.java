package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryItemPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemCreateVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemResponseVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryItemBusinessTest {

    @Mock
    private InventoryItemPersistencePort persistencePort;

    @InjectMocks
    private InventoryItemBusiness inventoryItemBusiness;

    @Test
    void createItem_shouldTrimName_andApplyDefaultsBeforePersisting() {
        when(persistencePort.save(any())).thenAnswer(invocation -> {
            InventoryItemCreateVO saved = invocation.getArgument(0);
            return new InventoryItemResponseVO(101L, saved.farmId(), saved.name(), saved.trackLot(), saved.active());
        });

        InventoryItemResponseVO response = inventoryItemBusiness.createItem(
                7L,
                new InventoryItemCreateRequestVO("  Ração Premium 22%  ", null)
        );

        ArgumentCaptor<InventoryItemCreateVO> captor = ArgumentCaptor.forClass(InventoryItemCreateVO.class);
        verify(persistencePort).save(captor.capture());

        InventoryItemCreateVO persisted = captor.getValue();
        assertThat(persisted.farmId()).isEqualTo(7L);
        assertThat(persisted.name()).isEqualTo("Ração Premium 22%");
        assertThat(persisted.trackLot()).isFalse();
        assertThat(persisted.active()).isTrue();

        assertThat(response.id()).isEqualTo(101L);
        assertThat(response.name()).isEqualTo("Ração Premium 22%");
    }

    @Test
    void createItem_shouldKeepTrackLotTrue_whenRequested() {
        when(persistencePort.save(any())).thenAnswer(invocation -> {
            InventoryItemCreateVO saved = invocation.getArgument(0);
            return new InventoryItemResponseVO(202L, saved.farmId(), saved.name(), saved.trackLot(), saved.active());
        });

        InventoryItemResponseVO response = inventoryItemBusiness.createItem(
                3L,
                new InventoryItemCreateRequestVO("Vacina", true)
        );

        assertThat(response.trackLot()).isTrue();
        verify(persistencePort).save(new InventoryItemCreateVO(3L, "Vacina", true, true));
    }

    @Test
    void createItem_shouldThrowInvalidArgument_whenNameIsBlank() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> inventoryItemBusiness.createItem(5L, new InventoryItemCreateRequestVO("   ", false))
        );

        assertThat(exception.getMessage()).contains("Nome do item é obrigatório.");
        verifyNoInteractions(persistencePort);
    }

    @Test
    void createItem_shouldThrowInvalidArgument_whenNameExceedsMaxLength() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> inventoryItemBusiness.createItem(5L, new InventoryItemCreateRequestVO("a".repeat(121), false))
        );

        assertThat(exception.getMessage()).contains("no máximo 120 caracteres");
        verifyNoInteractions(persistencePort);
    }

    @Test
    void listItems_shouldDelegateToPersistencePort() {
        PageRequest pageable = PageRequest.of(0, 20);
        when(persistencePort.listByFarmId(9L, pageable)).thenReturn(new PageImpl<>(List.of(
                new InventoryItemResponseVO(1L, 9L, "Milho", false, true)
        )));

        var page = inventoryItemBusiness.listItems(9L, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).extracting(InventoryItemResponseVO::name).containsExactly("Milho");
        verify(persistencePort).listByFarmId(9L, pageable);
    }
}
