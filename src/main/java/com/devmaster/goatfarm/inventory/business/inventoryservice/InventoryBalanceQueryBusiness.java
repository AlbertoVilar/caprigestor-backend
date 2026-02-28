package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryBalanceQueryUseCase;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryBalanceQueryPort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryBalanceQueryBusiness implements InventoryBalanceQueryUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final InventoryBalanceQueryPort queryPort;

    public InventoryBalanceQueryBusiness(InventoryBalanceQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryBalanceResponseVO> listBalances(InventoryBalanceFilterVO filter) {
        // Valida o filtro de leitura antes de consultar o saldo materializado.
        validateFilter(filter);
        return queryPort.listBalances(filter);
    }

    private void validateFilter(InventoryBalanceFilterVO filter) {
        if (filter == null) {
            throw new InvalidArgumentException("filter", "Filtro da consulta é obrigatório.");
        }

        validatePositive("farmId", filter.farmId(), "farmId deve ser positivo.");
        validateOptionalPositive("itemId", filter.itemId());
        validateOptionalPositive("lotId", filter.lotId());

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
