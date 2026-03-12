package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryLotPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotActivationRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotCreateVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotResponseVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryLotBusinessTest {

    @Mock
    private InventoryLotPersistencePort persistencePort;

    @InjectMocks
    private InventoryLotBusiness business;

    @Test
    void shouldCreateLot_whenPayloadIsValid() {
        Long farmId = 1L;
        Long itemId = 10L;
        InventoryLotCreateRequestVO request = new InventoryLotCreateRequestVO(
                itemId,
                " Ração Março ",
                "  entrega principal  ",
                LocalDate.of(2026, 9, 30),
                null
        );
        InventoryLotResponseVO response = new InventoryLotResponseVO(
                55L,
                farmId,
                itemId,
                "Ração Março",
                "entrega principal",
                LocalDate.of(2026, 9, 30),
                true
        );

        when(persistencePort.findItemSnapshot(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, true)));
        when(persistencePort.findByFarmIdAndItemIdAndCodeNormalized(farmId, itemId, "racao marco")).thenReturn(Optional.empty());
        when(persistencePort.save(any())).thenReturn(response);

        InventoryLotResponseVO created = business.createLot(farmId, request);

        assertThat(created.id()).isEqualTo(55L);
        ArgumentCaptor<InventoryLotCreateVO> captor = ArgumentCaptor.forClass(InventoryLotCreateVO.class);
        verify(persistencePort).save(captor.capture());
        assertThat(captor.getValue().farmId()).isEqualTo(farmId);
        assertThat(captor.getValue().itemId()).isEqualTo(itemId);
        assertThat(captor.getValue().code()).isEqualTo("Ração Março");
        assertThat(captor.getValue().description()).isEqualTo("entrega principal");
        assertThat(captor.getValue().active()).isTrue();
    }

    @Test
    void shouldRejectDuplicateCode_whenFarmItemCodeAlreadyExists() {
        Long farmId = 1L;
        Long itemId = 10L;
        InventoryLotCreateRequestVO request = new InventoryLotCreateRequestVO(itemId, "Ração Março", null, null, true);

        when(persistencePort.findItemSnapshot(farmId, itemId)).thenReturn(Optional.of(new InventoryItemSnapshotVO(itemId, true)));
        when(persistencePort.findByFarmIdAndItemIdAndCodeNormalized(farmId, itemId, "racao marco"))
                .thenReturn(Optional.of(new InventoryLotResponseVO(99L, farmId, itemId, "Ração Março", null, null, true)));

        DuplicateEntityException exception = assertThrows(
                DuplicateEntityException.class,
                () -> business.createLot(farmId, request)
        );

        assertThat(exception.getMessage()).contains("Já existe um lote");
        verify(persistencePort, never()).save(any());
    }

    @Test
    void shouldRejectWhenItemDoesNotExist() {
        when(persistencePort.findItemSnapshot(1L, 10L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> business.createLot(1L, new InventoryLotCreateRequestVO(10L, "Lote 1", null, null, true))
        );

        assertThat(exception.getMessage()).contains("Item de estoque");
    }

    @Test
    void shouldUpdateLotActiveStatus() {
        InventoryLotResponseVO response = new InventoryLotResponseVO(12L, 1L, 10L, "Lote 12", null, null, false);

        when(persistencePort.findByFarmIdAndId(1L, 12L)).thenReturn(
                Optional.of(new InventoryLotResponseVO(12L, 1L, 10L, "Lote 12", null, null, true))
        );
        when(persistencePort.updateActive(1L, 12L, false)).thenReturn(Optional.of(response));

        InventoryLotResponseVO updated = business.updateLotActive(1L, 12L, new InventoryLotActivationRequestVO(false));

        assertThat(updated.active()).isFalse();
    }

    @Test
    void shouldValidateMandatoryCode() {
        InvalidArgumentException exception = assertThrows(
                InvalidArgumentException.class,
                () -> business.createLot(1L, new InventoryLotCreateRequestVO(10L, "   ", null, null, true))
        );

        assertThat(exception.getMessage()).contains("code");
    }

    @Test
    void shouldListLotsFromPersistencePort() {
        InventoryLotFilterVO filter = new InventoryLotFilterVO(1L, 10L, true, PageRequest.of(0, 20));
        when(persistencePort.listLots(filter)).thenReturn(new PageImpl<>(
                List.of(new InventoryLotResponseVO(1L, 1L, 10L, "Lote 1", null, null, true)),
                PageRequest.of(0, 20),
                1
        ));

        var page = business.listLots(filter);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).code()).isEqualTo("Lote 1");
    }
}
