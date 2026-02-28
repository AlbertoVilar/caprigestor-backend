package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryMovementQueryUseCase;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryMovementQueryPort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementHistoryResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryMovementQueryBusiness implements InventoryMovementQueryUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final InventoryMovementQueryPort queryPort;

    public InventoryMovementQueryBusiness(InventoryMovementQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryMovementHistoryResponseVO> listMovements(InventoryMovementFilterVO filter) {
        // Garante consistência dos filtros antes de consultar o histórico.
        validateFilter(filter);
        return queryPort.listMovements(filter);
    }

    private void validateFilter(InventoryMovementFilterVO filter) {
        if (filter == null) {
            throw new InvalidArgumentException("filter", "Filtro da consulta é obrigatório.");
        }

        validatePositive("farmId", filter.farmId(), "farmId deve ser positivo.");
        validateOptionalPositive("itemId", filter.itemId());
        validateOptionalPositive("lotId", filter.lotId());

        if (filter.fromDate() != null && filter.toDate() != null && filter.fromDate().isAfter(filter.toDate())) {
            throw new InvalidArgumentException(
                    "fromDate",
                    "Data inicial não pode ser maior que data final."
            );
        }

        if (filter.pageable() == null) {
            throw new InvalidArgumentException("pageable", "Paginação é obrigatória.");
        }

        if (filter.pageable().getPageSize() > MAX_PAGE_SIZE) {
            throw new InvalidArgumentException(
                    "size",
                    "size não pode ser maior que " + MAX_PAGE_SIZE + "."
            );
        }
    }

    private void validatePositive(String field, Long value, String message) {
        if (value == null || value <= 0) {
            throw new InvalidArgumentException(field, message);
        }
    }

    private void validateOptionalPositive(String field, Long value) {
        if (value != null && value <= 0) {
            throw new InvalidArgumentException(field, field + " deve ser positivo quando informado.");
        }
    }
}
