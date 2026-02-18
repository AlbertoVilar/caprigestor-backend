package com.devmaster.goatfarm.inventory.business.inventoryservice;

import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.inventory.application.ports.in.InventoryMovementCommandUseCase;
import com.devmaster.goatfarm.inventory.application.ports.out.InventoryMovementPersistencePort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryBalanceSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryIdempotencyVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryItemSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementResponseVO;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryAdjustDirection;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class InventoryMovementBusiness implements InventoryMovementCommandUseCase {

    private final InventoryMovementPersistencePort persistencePort;

    public InventoryMovementBusiness(InventoryMovementPersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public InventoryMovementResponseVO createMovement(
            Long farmId,
            String idempotencyKey,
            InventoryMovementCreateRequestVO request
    ) {
        validateInput(farmId, idempotencyKey, request);
        String requestHash = hashRequest(request);

        Optional<InventoryIdempotencyVO> existing = persistencePort.findIdempotency(farmId, idempotencyKey);
        if (existing.isPresent()) {
            InventoryIdempotencyVO idempotencyVO = existing.get();
            if (idempotencyVO.requestHash().equals(requestHash)) {
                return idempotencyVO.response();
            }
            throw new DuplicateEntityException("idempotencyKey", "Idempotency-Key já foi usada com payload diferente.");
        }

        InventoryItemSnapshotVO item = persistencePort.findItemSnapshot(farmId, request.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item de estoque não encontrado."));

        validateTrackLot(item, request);
        Long effectiveLotId = item.trackLot() ? request.lotId() : null;

        InventoryItemSnapshotVO lockedItem = persistencePort.lockItemForUpdate(farmId, request.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item de estoque não encontrado."));

        validateTrackLot(lockedItem, request);

        InventoryBalanceSnapshotVO currentBalance = persistencePort.lockBalanceForUpdate(farmId, request.itemId(), effectiveLotId)
                .orElse(new InventoryBalanceSnapshotVO(farmId, request.itemId(), effectiveLotId, BigDecimal.ZERO));

        // Mantem a regra de saldo nao negativo para OUT e ADJUST DECREASE.
        computeResultingBalance(request, currentBalance.quantity());

        // TODO: registrar idempotencia apos persistencia real.
        // TODO: persistir movement + balance + idempotency record.
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private void validateTrackLot(InventoryItemSnapshotVO item, InventoryMovementCreateRequestVO request) {
        if (item.trackLot() && request.lotId() == null) {
            throw new InvalidArgumentException("lotId", "lotId é obrigatório quando o item possui rastreio por lote (trackLot=true).");
        }

        if (!item.trackLot() && request.lotId() != null) {
            throw new InvalidArgumentException("lotId", "lotId deve ser nulo quando o item não possui rastreio por lote (trackLot=false).");
        }
    }

    private BigDecimal computeResultingBalance(InventoryMovementCreateRequestVO request, BigDecimal currentBalance) {
        if (InventoryMovementType.IN.equals(request.type())) {
            return currentBalance.add(request.quantity());
        }

        if (InventoryMovementType.OUT.equals(request.type())) {
            BigDecimal resulting = currentBalance.subtract(request.quantity());
            if (resulting.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessRuleException("quantity",
                        "Saldo insuficiente para realizar a movimentação. Saldo atual: "
                                + currentBalance + ", solicitado: " + request.quantity() + ".");
            }
            return resulting;
        }

        if (InventoryAdjustDirection.INCREASE.equals(request.adjustDirection())) {
            return currentBalance.add(request.quantity());
        }

        BigDecimal resulting = currentBalance.subtract(request.quantity());
        if (resulting.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("quantity",
                    "Ajuste (DECREASE) não permitido: saldo ficaria negativo. Saldo atual: "
                            + currentBalance + ", ajuste: " + request.quantity() + ".");
        }
        return resulting;
    }

    private void validateInput(Long farmId, String idempotencyKey, InventoryMovementCreateRequestVO request) {
        if (farmId == null) {
            throw new InvalidArgumentException("farmId", "farmId é obrigatório.");
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidArgumentException("Idempotency-Key", "Idempotency-Key é obrigatório.");
        }

        if (request == null) {
            throw new InvalidArgumentException("request", "Payload da requisição é obrigatório.");
        }

        if (request.type() == null) {
            throw new InvalidArgumentException("type", "Tipo do movimento é obrigatório.");
        }

        if (request.quantity() == null || request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException("quantity", "Quantidade deve ser maior que zero.");
        }

        if (request.itemId() == null) {
            throw new InvalidArgumentException("itemId", "itemId é obrigatório.");
        }

        if (InventoryMovementType.ADJUST.equals(request.type()) && request.adjustDirection() == null) {
            throw new InvalidArgumentException("adjustDirection", "adjustDirection é obrigatório quando o tipo é ADJUST.");
        }
    }

    private String hashRequest(InventoryMovementCreateRequestVO request) {
        String canonical = buildCanonicalRequest(request);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 não disponível.", e);
        }
    }

    private String buildCanonicalRequest(InventoryMovementCreateRequestVO request) {
        String quantityNormalized = request.quantity().stripTrailingZeros().toPlainString();
        String lotIdOrEmpty = request.lotId() == null ? "" : request.lotId().toString();
        String adjustDirectionOrEmpty = request.adjustDirection() == null ? "" : request.adjustDirection().name();
        String movementDateOrEmpty = request.movementDate() == null ? "" : request.movementDate().toString();
        String reasonTrimOrEmpty = request.reason() == null
                ? ""
                : request.reason().trim().replaceAll("\\s+", " ");

        return request.type().name()
                + "|" + quantityNormalized
                + "|" + request.itemId()
                + "|" + lotIdOrEmpty
                + "|" + adjustDirectionOrEmpty
                + "|" + movementDateOrEmpty
                + "|" + reasonTrimOrEmpty;
    }
}
