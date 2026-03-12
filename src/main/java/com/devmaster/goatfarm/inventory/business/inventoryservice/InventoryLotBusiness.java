package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryLotCommandUseCase;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryLotQueryUseCase;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryLotPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotActivationRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotCreateVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotResponseVO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Locale;

@Service
public class InventoryLotBusiness implements InventoryLotCommandUseCase, InventoryLotQueryUseCase {

    private final InventoryLotPersistencePort persistencePort;

    public InventoryLotBusiness(InventoryLotPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    @Transactional
    public InventoryLotResponseVO createLot(Long farmId, InventoryLotCreateRequestVO request) {
        validateCreateRequest(farmId, request);

        Long itemId = request.itemId();
        persistencePort.findItemSnapshot(farmId, itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item de estoque não encontrado."));

        String normalizedCode = normalizeCode(request.code());
        persistencePort.findByFarmIdAndItemIdAndCodeNormalized(farmId, itemId, normalizedCode)
                .ifPresent(existing -> {
                    throw new DuplicateEntityException(
                            "code",
                            "Já existe um lote com esse código para este item nesta fazenda."
                    );
                });

        return persistencePort.save(new InventoryLotCreateVO(
                farmId,
                itemId,
                normalizeRequiredText(request.code()),
                normalizeNullableText(request.description()),
                request.expirationDate(),
                request.active() == null || request.active()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryLotResponseVO> listLots(InventoryLotFilterVO filter) {
        if (filter == null) {
            throw new InvalidArgumentException("filter", "Filtro da listagem de lotes é obrigatório.");
        }

        if (filter.farmId() == null) {
            throw new InvalidArgumentException("farmId", "farmId é obrigatório.");
        }

        return persistencePort.listLots(filter);
    }

    @Override
    @Transactional
    public InventoryLotResponseVO updateLotActive(Long farmId, Long lotId, InventoryLotActivationRequestVO request) {
        if (farmId == null) {
            throw new InvalidArgumentException("farmId", "farmId é obrigatório.");
        }

        if (lotId == null) {
            throw new InvalidArgumentException("lotId", "lotId é obrigatório.");
        }

        if (request == null || request.active() == null) {
            throw new InvalidArgumentException("active", "active é obrigatório.");
        }

        persistencePort.findByFarmIdAndId(farmId, lotId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote de estoque não encontrado."));

        return persistencePort.updateActive(farmId, lotId, request.active())
                .orElseThrow(() -> new ResourceNotFoundException("Lote de estoque não encontrado."));
    }

    private void validateCreateRequest(Long farmId, InventoryLotCreateRequestVO request) {
        if (farmId == null) {
            throw new InvalidArgumentException("farmId", "farmId é obrigatório.");
        }

        if (request == null) {
            throw new InvalidArgumentException("request", "Payload do lote é obrigatório.");
        }

        if (request.itemId() == null) {
            throw new InvalidArgumentException("itemId", "itemId é obrigatório.");
        }

        String normalizedCode = normalizeRequiredText(request.code());
        if (normalizedCode == null) {
            throw new InvalidArgumentException("code", "code é obrigatório.");
        }

        if (normalizedCode.length() > 80) {
            throw new InvalidArgumentException("code", "code deve ter no máximo 80 caracteres.");
        }

        String normalizedDescription = normalizeNullableText(request.description());
        if (normalizedDescription != null && normalizedDescription.length() > 500) {
            throw new InvalidArgumentException("description", "description deve ter no máximo 500 caracteres.");
        }
    }

    private String normalizeRequiredText(String input) {
        if (input == null) {
            return null;
        }
        String normalized = input.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeNullableText(String input) {
        return normalizeRequiredText(input);
    }

    private String normalizeCode(String input) {
        String normalized = normalizeRequiredText(input);
        if (normalized == null) {
            return null;
        }
        String noDiacritics = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noDiacritics.toLowerCase(Locale.ROOT);
    }
}
