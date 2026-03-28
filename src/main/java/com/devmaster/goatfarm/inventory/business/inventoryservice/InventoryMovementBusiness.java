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
import com.devmaster.goatfarm.inventory.business.bo.InventoryLotSnapshotVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementCreateRequestVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementPersistedVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementResultVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementResponseVO;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryAdjustDirection;
import com.devmaster.goatfarm.inventory.domain.enums.InventoryMovementType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class InventoryMovementBusiness implements InventoryMovementCommandUseCase {

    private final InventoryMovementPersistencePort persistencePort;
    private final Clock clock;

    public InventoryMovementBusiness(InventoryMovementPersistencePort persistencePort, Clock clock) {
        this.persistencePort = persistencePort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public InventoryMovementResultVO createMovement(
            Long farmId,
            String idempotencyKey,
            InventoryMovementCreateRequestVO request
    ) {
        validateRequest(farmId, idempotencyKey, request);
        InventoryMovementCreateRequestVO normalizedRequest = normalizePurchaseRequest(request);
        String requestHash = hashRequest(normalizedRequest);

        Optional<InventoryMovementResponseVO> replay = resolveIdempotencyReplay(farmId, idempotencyKey, requestHash);
        if (replay.isPresent()) {
            return new InventoryMovementResultVO(replay.get(), true);
        }

        InventoryItemSnapshotVO itemSnapshot = resolveItemSnapshot(farmId, normalizedRequest.itemId());
        validateTrackLot(itemSnapshot, normalizedRequest);

        LockedBalanceContext lockedBalance = lockItemThenBalance(farmId, normalizedRequest);
        BigDecimal resultingBalance = computeNewBalance(normalizedRequest, lockedBalance.balance().quantity());

        InventoryMovementResponseVO response = persistMovementAndBalance(
                farmId,
                normalizedRequest,
                lockedBalance.effectiveLotId(),
                resultingBalance
        );

        persistIdempotencyResult(farmId, idempotencyKey, requestHash, response);
        return new InventoryMovementResultVO(response, false);
    }

    private Optional<InventoryMovementResponseVO> resolveIdempotencyReplay(
            Long farmId,
            String idempotencyKey,
            String requestHash
    ) {
        return persistencePort.findIdempotency(farmId, idempotencyKey)
                .map(existing -> {
                    if (existing.requestHash().equals(requestHash)) {
                        return existing.response();
                    }
                    throw new DuplicateEntityException(
                            "idempotencyKey",
                            "Idempotency-Key ja foi usada com payload diferente."
                    );
                });
    }

    private InventoryItemSnapshotVO resolveItemSnapshot(Long farmId, Long itemId) {
        return persistencePort.findItemSnapshot(farmId, itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item de estoque nao encontrado."));
    }

    private LockedBalanceContext lockItemThenBalance(
            Long farmId,
            InventoryMovementCreateRequestVO request
    ) {
        InventoryItemSnapshotVO lockedItem = persistencePort.lockItemForUpdate(farmId, request.itemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item de estoque nao encontrado."));
        validateTrackLot(lockedItem, request);
        Long effectiveLotId = resolveEffectiveLotId(farmId, lockedItem, request);

        InventoryBalanceSnapshotVO balance = persistencePort.lockBalanceForUpdate(farmId, request.itemId(), effectiveLotId)
                .orElse(new InventoryBalanceSnapshotVO(farmId, request.itemId(), effectiveLotId, BigDecimal.ZERO));

        return new LockedBalanceContext(effectiveLotId, balance);
    }

    private InventoryMovementResponseVO persistMovementAndBalance(
            Long farmId,
            InventoryMovementCreateRequestVO request,
            Long effectiveLotId,
            BigDecimal resultingBalance
    ) {
        InventoryMovementPersistedVO persistedMovement = persistencePort.saveMovement(new InventoryMovementPersistedVO(
                null,
                farmId,
                request.type(),
                request.adjustDirection(),
                request.quantity(),
                request.itemId(),
                effectiveLotId,
                resolveMovementDate(request),
                normalizeReason(request.reason()),
                resultingBalance,
                request.unitCost(),
                request.totalCost(),
                request.purchaseDate(),
                normalizeSupplierName(request.supplierName()),
                OffsetDateTime.now(clock)
        ));

        InventoryBalanceSnapshotVO persistedBalance = persistencePort.upsertBalance(
                new InventoryBalanceSnapshotVO(farmId, request.itemId(), effectiveLotId, resultingBalance)
        );

        return new InventoryMovementResponseVO(
                persistedMovement.movementId(),
                persistedMovement.type(),
                persistedMovement.quantity(),
                persistedMovement.itemId(),
                persistedMovement.lotId(),
                persistedMovement.movementDate(),
                persistedBalance.quantity(),
                persistedMovement.unitCost(),
                persistedMovement.totalCost(),
                persistedMovement.purchaseDate(),
                persistedMovement.supplierName(),
                persistedMovement.createdAt()
        );
    }

    private void persistIdempotencyResult(
            Long farmId,
            String idempotencyKey,
            String requestHash,
            InventoryMovementResponseVO response
    ) {
        persistencePort.saveIdempotency(new InventoryIdempotencyVO(farmId, idempotencyKey, requestHash, response));
    }

    private void validateTrackLot(InventoryItemSnapshotVO item, InventoryMovementCreateRequestVO request) {
        if (item.trackLot() && request.lotId() == null) {
            throw new InvalidArgumentException(
                    "lotId",
                    "lotId e obrigatorio quando o item possui rastreio por lote (trackLot=true)."
            );
        }

        if (!item.trackLot() && request.lotId() != null) {
            throw new InvalidArgumentException(
                    "lotId",
                    "lotId deve ser nulo quando o item nao possui rastreio por lote (trackLot=false)."
            );
        }
    }

    private Long resolveEffectiveLotId(
            Long farmId,
            InventoryItemSnapshotVO item,
            InventoryMovementCreateRequestVO request
    ) {
        if (!item.trackLot()) {
            return null;
        }

        InventoryLotSnapshotVO lot = persistencePort.findLotSnapshot(farmId, request.lotId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote de estoque nao encontrado."));

        if (!lot.itemId().equals(item.itemId())) {
            throw new InvalidArgumentException(
                    "lotId",
                    "lotId deve referenciar um lote valido para o item informado."
            );
        }

        if (!lot.active()) {
            throw new BusinessRuleException("lotId", "Lote informado esta inativo.");
        }

        return lot.lotId();
    }

    private BigDecimal computeNewBalance(InventoryMovementCreateRequestVO request, BigDecimal currentBalance) {
        if (InventoryMovementType.IN.equals(request.type())) {
            return currentBalance.add(request.quantity());
        }

        if (InventoryMovementType.OUT.equals(request.type())) {
            return decreaseWithNonNegativeCheck(currentBalance, request.quantity(),
                    "Saldo insuficiente para realizar a movimentacao.");
        }

        if (InventoryAdjustDirection.INCREASE.equals(request.adjustDirection())) {
            return currentBalance.add(request.quantity());
        }

        return decreaseWithNonNegativeCheck(currentBalance, request.quantity(),
                "Ajuste (DECREASE) nao permitido: saldo ficaria negativo.");
    }

    private BigDecimal decreaseWithNonNegativeCheck(BigDecimal currentBalance, BigDecimal quantity, String message) {
        BigDecimal resulting = currentBalance.subtract(quantity);
        if (resulting.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException(
                    "quantity",
                    message + " Saldo atual: " + currentBalance + ", solicitado: " + quantity + "."
            );
        }
        return resulting;
    }

    private LocalDate resolveMovementDate(InventoryMovementCreateRequestVO request) {
        return request.movementDate() != null ? request.movementDate() : LocalDate.now(clock);
    }

    private String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }
        String normalized = reason.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeSupplierName(String supplierName) {
        if (supplierName == null) {
            return null;
        }
        String normalized = supplierName.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    private void validateRequest(Long farmId, String idempotencyKey, InventoryMovementCreateRequestVO request) {
        if (farmId == null) {
            throw new InvalidArgumentException("farmId", "farmId e obrigatorio.");
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new InvalidArgumentException("Idempotency-Key", "Idempotency-Key e obrigatorio.");
        }

        if (request == null) {
            throw new InvalidArgumentException("request", "Payload da requisicao e obrigatorio.");
        }

        if (request.type() == null) {
            throw new InvalidArgumentException("type", "Tipo do movimento e obrigatorio.");
        }

        if (request.quantity() == null || request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException("quantity", "Quantidade deve ser maior que zero.");
        }

        if (request.itemId() == null) {
            throw new InvalidArgumentException("itemId", "itemId e obrigatorio.");
        }

        if (InventoryMovementType.ADJUST.equals(request.type()) && request.adjustDirection() == null) {
            throw new InvalidArgumentException(
                    "adjustDirection",
                    "adjustDirection e obrigatorio quando o tipo e ADJUST."
            );
        }

        if ((InventoryMovementType.IN.equals(request.type()) || InventoryMovementType.OUT.equals(request.type()))
                && request.adjustDirection() != null) {
            throw new InvalidArgumentException(
                    "adjustDirection",
                    "adjustDirection deve ser nulo quando o tipo e IN ou OUT."
            );
        }

        validatePurchaseMetadata(request);
    }

    private void validatePurchaseMetadata(InventoryMovementCreateRequestVO request) {
        boolean hasPurchaseMetadata = hasPurchaseMetadata(request);

        if (!hasPurchaseMetadata) {
            return;
        }

        if (!InventoryMovementType.IN.equals(request.type())) {
            throw new InvalidArgumentException(
                    "type",
                    "Custo de compra so pode ser informado em entradas de estoque (type=IN)."
            );
        }

        if (request.purchaseDate() == null) {
            throw new InvalidArgumentException(
                    "purchaseDate",
                    "purchaseDate e obrigatoria quando a entrada representar uma compra."
            );
        }

        if (request.unitCost() == null && request.totalCost() == null) {
            throw new InvalidArgumentException(
                    "unitCost",
                    "Informe unitCost ou totalCost quando a entrada representar uma compra."
            );
        }

        if (request.unitCost() != null && request.unitCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException("unitCost", "unitCost deve ser maior que zero.");
        }

        if (request.totalCost() != null && request.totalCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidArgumentException("totalCost", "totalCost deve ser maior que zero.");
        }

        if (request.unitCost() != null && request.totalCost() != null) {
            BigDecimal expectedTotalCost = request.unitCost()
                    .multiply(request.quantity())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal informedTotalCost = request.totalCost().setScale(2, RoundingMode.HALF_UP);

            if (expectedTotalCost.compareTo(informedTotalCost) != 0) {
                throw new InvalidArgumentException(
                        "totalCost",
                        "totalCost deve ser consistente com quantity x unitCost."
                );
            }
        }
    }

    private InventoryMovementCreateRequestVO normalizePurchaseRequest(InventoryMovementCreateRequestVO request) {
        if (!hasPurchaseMetadata(request)) {
            return request;
        }

        BigDecimal normalizedUnitCost = request.unitCost();
        BigDecimal normalizedTotalCost = request.totalCost();

        if (normalizedUnitCost == null && normalizedTotalCost != null) {
            normalizedUnitCost = normalizedTotalCost.divide(request.quantity(), 4, RoundingMode.HALF_UP);
        }

        if (normalizedTotalCost == null && normalizedUnitCost != null) {
            normalizedTotalCost = normalizedUnitCost.multiply(request.quantity()).setScale(2, RoundingMode.HALF_UP);
        }

        return new InventoryMovementCreateRequestVO(
                request.type(),
                request.quantity(),
                request.itemId(),
                request.lotId(),
                request.adjustDirection(),
                request.movementDate(),
                request.reason(),
                normalizedUnitCost,
                normalizedTotalCost,
                request.purchaseDate(),
                normalizeSupplierName(request.supplierName())
        );
    }

    private boolean hasPurchaseMetadata(InventoryMovementCreateRequestVO request) {
        return request.unitCost() != null
                || request.totalCost() != null
                || request.purchaseDate() != null
                || normalizeSupplierName(request.supplierName()) != null;
    }

    private String hashRequest(InventoryMovementCreateRequestVO request) {
        String canonical = buildCanonicalRequest(request);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 nao disponivel.", e);
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
        String baseCanonical = request.type().name()
                + "|" + quantityNormalized
                + "|" + request.itemId()
                + "|" + lotIdOrEmpty
                + "|" + adjustDirectionOrEmpty
                + "|" + movementDateOrEmpty
                + "|" + reasonTrimOrEmpty;

        if (!hasPurchaseMetadata(request)) {
            return baseCanonical;
        }

        String unitCostOrEmpty = request.unitCost() == null ? "" : request.unitCost().stripTrailingZeros().toPlainString();
        String totalCostOrEmpty = request.totalCost() == null ? "" : request.totalCost().stripTrailingZeros().toPlainString();
        String purchaseDateOrEmpty = request.purchaseDate() == null ? "" : request.purchaseDate().toString();
        String supplierTrimOrEmpty = normalizeSupplierName(request.supplierName()) == null
                ? ""
                : normalizeSupplierName(request.supplierName());

        return baseCanonical
                + "|" + unitCostOrEmpty
                + "|" + totalCostOrEmpty
                + "|" + purchaseDateOrEmpty
                + "|" + supplierTrimOrEmpty;
    }

    private record LockedBalanceContext(Long effectiveLotId, InventoryBalanceSnapshotVO balance) {
    }
}
