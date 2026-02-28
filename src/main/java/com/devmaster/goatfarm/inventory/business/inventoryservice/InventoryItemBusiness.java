package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryItemCommandUseCase;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryItemQueryUseCase;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryItemPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemCreateVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryItemBusiness implements InventoryItemCommandUseCase, InventoryItemQueryUseCase {

    private static final int MAX_NAME_LENGTH = 120;

    private final InventoryItemPersistencePort persistencePort;

    public InventoryItemBusiness(InventoryItemPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    @Transactional
    public InventoryItemResponseVO createItem(Long farmId, InventoryItemCreateRequestVO request) {
        // Valida e normaliza o item antes de persistir no mesmo módulo.
        validateFarmId(farmId);
        InventoryItemCreateVO normalized = normalizeCreateRequest(farmId, request);
        return persistencePort.save(normalized);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryItemResponseVO> listItems(Long farmId, Pageable pageable) {
        // Mantém a listagem paginada restrita à fazenda informada.
        validateFarmId(farmId);
        return persistencePort.listByFarmId(farmId, pageable);
    }

    private void validateFarmId(Long farmId) {
        if (farmId == null || farmId <= 0) {
            throw new InvalidArgumentException("farmId", "farmId deve ser positivo.");
        }
    }

    private InventoryItemCreateVO normalizeCreateRequest(Long farmId, InventoryItemCreateRequestVO request) {
        if (request == null) {
            throw new InvalidArgumentException("request", "Payload da requisição é obrigatório.");
        }

        String normalizedName = normalizeName(request.name());
        boolean trackLot = Boolean.TRUE.equals(request.trackLot());

        return new InventoryItemCreateVO(farmId, normalizedName, trackLot, true);
    }

    private String normalizeName(String rawName) {
        if (rawName == null) {
            throw new InvalidArgumentException("name", "Nome do item é obrigatório.");
        }

        String trimmed = rawName.trim();
        if (trimmed.isBlank()) {
            throw new InvalidArgumentException("name", "Nome do item é obrigatório.");
        }

        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new InvalidArgumentException(
                    "name",
                    "Nome do item deve ter no máximo " + MAX_NAME_LENGTH + " caracteres."
            );
        }

        return trimmed;
    }
}
