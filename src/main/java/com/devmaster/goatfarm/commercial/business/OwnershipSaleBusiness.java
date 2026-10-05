package com.devmaster.goatfarm.commercial.business;

import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditRecordVO;
import com.devmaster.goatfarm.audit.enums.OperationalAuditActionType;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.commercial.application.model.AnimalSaleCommand;
import com.devmaster.goatfarm.commercial.application.model.AnimalSaleRecord;
import com.devmaster.goatfarm.commercial.application.ports.in.OwnershipSaleUseCase;
import com.devmaster.goatfarm.commercial.application.ports.out.AnimalSalePersistencePort;
import com.devmaster.goatfarm.commercial.business.bo.OwnershipSaleRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.OwnershipSaleResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.InternalOwnershipSaleRequest;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipSaleUseCase;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Commercial process for an internal farm buyer. It never treats animal_sale
 * as ownership authority: only the ledger handoff completes ownership.
 */
@Service
@Transactional(readOnly = true)
public class OwnershipSaleBusiness implements OwnershipSaleUseCase {
    private final AnimalSalePersistencePort sales;
    private final GoatFarmPersistencePort farms;
    private final GoatManagementUseCase goats;
    private final FarmAuthorizationUseCase authorization;
    private final CurrentPrincipalQueryUseCase principalQuery;
    private final GoatOwnershipSaleUseCase ownershipSales;
    private final OperationalAuditUseCase audit;
    private final Clock clock;

    public OwnershipSaleBusiness(AnimalSalePersistencePort sales,
                                 GoatFarmPersistencePort farms,
                                 GoatManagementUseCase goats,
                                 FarmAuthorizationUseCase authorization,
                                 CurrentPrincipalQueryUseCase principalQuery,
                                 GoatOwnershipSaleUseCase ownershipSales,
                                 OperationalAuditUseCase audit,
                                 Clock clock) {
        this.sales = sales;
        this.farms = farms;
        this.goats = goats;
        this.authorization = authorization;
        this.principalQuery = principalQuery;
        this.ownershipSales = ownershipSales;
        this.audit = audit;
        this.clock = clock;
    }

    @Override
    @Transactional
    public OwnershipSaleResponseVO requestOwnershipSale(Long sourceFarmId, OwnershipSaleRequestVO request) {
        requireSeller(sourceFarmId);
        validateRequest(request);
        long requesterId = requirePrincipalId(principalQuery.requireCurrent());
        String idempotencyKey = request.idempotencyKey().trim();
        String technicalToken = technicalToken(request.goatId());
        OwnershipTransfer duplicate = ownershipSales.findByRequesterAndIdempotencyKey(requesterId, idempotencyKey).orElse(null);
        if (duplicate != null) {
            return resolveIdempotentSale(duplicate, sourceFarmId, request);
        }
        requireTargetFarm(sourceFarmId, request.targetFarmId());
        GoatResponseVO goat = goats.findGoatById(sourceFarmId, technicalToken);
        GoatId goatId = GoatId.of(requireTechnicalId(goat));
        ownershipSales.prepareInternalSale(sourceFarmId, goatId);
        OwnershipTransfer afterLockDuplicate = ownershipSales.findByRequesterAndIdempotencyKey(requesterId, idempotencyKey).orElse(null);
        if (afterLockDuplicate != null) {
            return resolveIdempotentSale(afterLockDuplicate, sourceFarmId, request);
        }
        if (ownershipSales.hasActiveInternalSale(sourceFarmId, goatId)) {
            throw new DuplicateEntityException("goatId", "Ja existe uma venda registrada para esta cabra nesta fazenda.");
        }
        LocalDate saleDate = requireSaleDate(request.saleDate());
        LocalDate dueDate = requireDueDate(saleDate, request.dueDate());
        LocalDate paymentDate = request.paymentDate() == null ? null : requirePaymentDate(saleDate, request.paymentDate());
        AnimalSaleRecord sale = sales.save(new AnimalSaleCommand(null, sourceFarmId, null, goatId.value(),
                goat.getRegistrationNumber(), goat.getName(), saleDate, currency(requirePositive(request.amount())), dueDate,
                paymentDate == null ? SalePaymentStatus.OPEN : SalePaymentStatus.PAID, paymentDate, optional(request.notes()), request.targetFarmId()));
        OwnershipTransfer transfer = ownershipSales.requestInternalSale(new InternalOwnershipSaleRequest(
                goatId, sourceFarmId, request.targetFarmId(), sale.id(), "OWNERSHIP_SALE:" + sale.id(), idempotencyKey));
        if (!Objects.equals(transfer.saleId(), sale.id())) {
            sales.deleteById(sale.id());
            return resolveIdempotentSale(transfer, sourceFarmId, request);
        }
        transfer = completeOwnershipIfReady(sale.id(), sale, transfer);
        audit.record(new OperationalAuditRecordVO(sourceFarmId, goatId.value(), goat.getRegistrationNumber(),
                OperationalAuditActionType.ANIMAL_SALE_CREATED, String.valueOf(sale.id()),
                "Venda com transferencia de propriedade solicitada para a fazenda " + request.targetFarmId() + "."));
        return response(sale, transfer);
    }

    @Override
    @Transactional
    public OwnershipSaleResponseVO acceptOwnershipSale(Long sourceFarmId, Long saleId) {
        throw new BusinessRuleException("buyer acceptance is not supported for canonical internal sales; payment by the seller completes the sale");
    }

    @Override
    @Transactional
    public OwnershipSaleResponseVO registerOwnershipSalePayment(Long sourceFarmId, Long saleId, SalePaymentRequestVO payment) {
        requireSeller(sourceFarmId);
        ownershipSales.lockAndReloadInternalSale(saleId);
        AnimalSaleRecord sale = requireSale(sourceFarmId, saleId);
        OwnershipTransfer transfer = requireSaleTransfer(sale);
        if (transfer.status() == com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.REJECTED
                || transfer.status() == com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.CANCELLED) {
            throw new BusinessRuleException("payment cannot be registered after ownership sale termination");
        }
        if (transfer.status() == com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.COMPLETED
                && sale.paymentStatus() != SalePaymentStatus.PAID) {
            throw new BusinessRuleException("completed ownership sale has inconsistent payment state");
        }
        if (sale.paymentStatus() == SalePaymentStatus.PAID) {
            if (payment != null && payment.paymentDate() != null && !Objects.equals(payment.paymentDate(), sale.paymentDate())) {
                throw new BusinessRuleException("payment is already recorded with a different date");
            }
            OwnershipTransfer ready = completeOwnershipIfReady(saleId, sale, transfer);
            return response(requireSale(sourceFarmId, saleId), ready);
        }
        LocalDate paidOn = requirePaymentDate(sale.saleDate(), payment == null ? null : payment.paymentDate());
        AnimalSaleRecord paidSale = sales.save(new AnimalSaleCommand(sale.id(), sale.farmId(), sale.customerId(),
                sale.goatTechnicalId(), sale.goatRegistrationNumber(), sale.goatName(), sale.saleDate(), sale.amount(),
                sale.dueDate(), SalePaymentStatus.PAID, paidOn, sale.notes(), sale.targetFarmId()));
        OwnershipTransfer afterPayment = completeOwnershipIfReady(saleId, paidSale, transfer);
        audit.record(new OperationalAuditRecordVO(sourceFarmId, paidSale.goatTechnicalId(), paidSale.goatRegistrationNumber(),
                OperationalAuditActionType.ANIMAL_SALE_PAYMENT_REGISTERED, String.valueOf(paidSale.id()),
                "Recebimento da venda com transferencia registrado em " + paidOn + "."));
        return response(paidSale, afterPayment);
    }

    private OwnershipTransfer completeOwnershipIfReady(Long saleId, AnimalSaleRecord sale, OwnershipTransfer transfer) {
        if (sale.paymentStatus() != SalePaymentStatus.PAID
                || (transfer.status() != com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.REQUESTED
                && transfer.status() != com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.ACCEPTED)) {
            return transfer;
        }
        return ownershipSales.completeInternalSaleAfterPayment(saleId);
    }

    @Override
    @Transactional
    public OwnershipSaleResponseVO rejectOwnershipSale(Long sourceFarmId, Long saleId) {
        throw new BusinessRuleException("buyer rejection is not supported for canonical internal sales; cancel before payment from the source farm");
    }

    @Override
    @Transactional
    public OwnershipSaleResponseVO cancelOwnershipSale(Long sourceFarmId, Long saleId) {
        requireSeller(sourceFarmId);
        ownershipSales.lockAndReloadInternalSale(saleId);
        AnimalSaleRecord sale = requireSale(sourceFarmId, saleId);
        if (sale.paymentStatus() == SalePaymentStatus.PAID) {
            throw new BusinessRuleException("paid ownership sale cannot be cancelled");
        }
        return response(sale, ownershipSales.cancelInternalSale(saleId));
    }

    @Override
    public List<OwnershipSaleResponseVO> listIncomingOwnershipSales(Long targetFarmId) {
        requireFarmReadAccess(targetFarmId);
        return sales.findOwnershipSalesByTargetFarmId(targetFarmId).stream()
                .map(sale -> response(sale, requireSaleTransfer(sale))).toList();
    }

    @Override
    public List<OwnershipSaleResponseVO> listOutgoingOwnershipSales(Long sourceFarmId) {
        requireFarmReadAccess(sourceFarmId);
        return sales.findAnimalSalesByFarmId(sourceFarmId).stream()
                .filter(sale -> sale.targetFarmId() != null)
                .map(sale -> response(sale, requireSaleTransfer(sale))).toList();
    }

    private OwnershipSaleResponseVO resolveIdempotentSale(OwnershipTransfer transfer, Long sourceFarmId,
                                                           OwnershipSaleRequestVO request) {
        if (transfer.kind() != OwnershipTransferKind.INTERNAL_SALE || !Objects.equals(transfer.sourceFarmId(), sourceFarmId)
                || transfer.targetFarmId() != request.targetFarmId() || !transfer.goatId().equals(GoatId.of(parseTechnicalId(request.goatId())))
                || transfer.saleId() == null) {
            throw new BusinessRuleException("idempotency key already represents a different ownership process");
        }
        AnimalSaleRecord sale = sales.findAnimalSaleById(transfer.saleId())
                .orElseThrow(() -> new BusinessRuleException("ownership sale transfer has no sale record"));
        LocalDate requestedPaymentDate = request.paymentDate() == null ? null : requirePaymentDate(requireSaleDate(request.saleDate()), request.paymentDate());
        if (sale.amount().compareTo(currency(requirePositive(request.amount()))) != 0
                || !Objects.equals(sale.saleDate(), requireSaleDate(request.saleDate()))
                || !Objects.equals(sale.dueDate(), requireDueDate(request.saleDate(), request.dueDate()))
                || !Objects.equals(sale.notes(), optional(request.notes()))
                || !Objects.equals(sale.paymentDate(), requestedPaymentDate)) {
            throw new BusinessRuleException("idempotency key already represents a different ownership sale");
        }
        return response(sale, transfer);
    }

    private AnimalSaleRecord requireSale(Long sourceFarmId, Long saleId) {
        if (saleId == null || saleId <= 0) throw new InvalidArgumentException("saleId must be positive");
        return sales.findAnimalSaleByIdAndFarmId(saleId, sourceFarmId)
                .orElseThrow(() -> new ResourceNotFoundException("Ownership sale not found: " + saleId));
    }

    private OwnershipTransfer requireSaleTransfer(AnimalSaleRecord sale) {
        if (sale.targetFarmId() == null) throw new BusinessRuleException("legacy external sale has no ownership transfer workflow");
        OwnershipTransfer transfer = ownershipSales.findSaleTransfer(sale.id());
        if (transfer.kind() != OwnershipTransferKind.INTERNAL_SALE || !Objects.equals(transfer.sourceFarmId(), sale.farmId())
                || transfer.targetFarmId() != sale.targetFarmId() || transfer.goatId().value() != sale.goatTechnicalId()) {
            throw new BusinessRuleException("ownership sale and canonical transfer are inconsistent");
        }
        return transfer;
    }

    private void requireSeller(Long farmId) {
        if (farmId == null || farmId <= 0) throw new InvalidArgumentException("farmId must be positive");
        authorization.verifyFarmOwnership(farmId);
        if (farms.findById(farmId).isEmpty()) throw new ResourceNotFoundException("Farm not found: " + farmId);
    }

    private void requireTargetAdministrator(long farmId) {
        if (!authorization.canAdministerFarm(farmId)) throw new AuthorizationDeniedException("current principal cannot administer target farm " + farmId);
    }

    private void requireFarmReadAccess(Long farmId) {
        if (farmId == null || farmId <= 0) throw new InvalidArgumentException("farmId must be positive");
        if (!authorization.canManageFarm(farmId)) throw new AuthorizationDeniedException("current principal cannot manage farm " + farmId);
        if (farms.findById(farmId).isEmpty()) throw new ResourceNotFoundException("Farm not found: " + farmId);
    }

    private void requireTargetFarm(long sourceFarmId, Long targetFarmId) {
        if (targetFarmId == null || targetFarmId <= 0) throw new InvalidArgumentException("targetFarmId must be positive");
        if (sourceFarmId == targetFarmId) throw new BusinessRuleException("source and target farms must differ");
        if (farms.findById(targetFarmId).isEmpty()) throw new ResourceNotFoundException("Target farm not found: " + targetFarmId);
    }

    private void validateRequest(OwnershipSaleRequestVO request) {
        if (request == null) throw new InvalidArgumentException("ownership sale request is required");
        if (optional(request.goatId()) == null) throw new InvalidArgumentException("goatId is required");
        if (optional(request.idempotencyKey()) == null) throw new InvalidArgumentException("idempotencyKey is required");
        if (!request.goatId().trim().startsWith("technical-")) {
            throw new InvalidArgumentException("goatId must use the structural technical-{id} format");
        }
        parseTechnicalId(request.goatId());
    }

    private long requireTechnicalId(GoatResponseVO goat) {
        if (goat == null || goat.getTechnicalId() == null || goat.getTechnicalId() <= 0) throw new BusinessRuleException("Goat has no valid technical identity");
        return goat.getTechnicalId();
    }

    private long parseTechnicalId(String value) {
        try {
            String normalized = value.trim();
            if (!normalized.startsWith("technical-")) throw new NumberFormatException();
            String numeric = normalized.substring("technical-".length());
            long id = Long.parseLong(numeric);
            if (id <= 0) throw new NumberFormatException();
            return id;
        }
        catch (RuntimeException exception) { throw new InvalidArgumentException("goatId must be a positive technical identifier"); }
    }

    private String technicalToken(String value) {
        return "technical-" + parseTechnicalId(value);
    }

    private long requirePrincipalId(AuthenticatedPrincipal principal) {
        if (principal == null || principal.id() == null || principal.id() <= 0) throw new AuthorizationDeniedException("current principal has no valid id");
        return principal.id();
    }

    private LocalDate requireSaleDate(LocalDate value) {
        if (value == null) throw new InvalidArgumentException("saleDate is required");
        if (value.isAfter(LocalDate.now(clock))) throw new InvalidArgumentException("saleDate cannot be in the future");
        return value;
    }

    private LocalDate requireDueDate(LocalDate saleDate, LocalDate dueDate) {
        if (dueDate == null) throw new InvalidArgumentException("dueDate is required");
        if (dueDate.isBefore(saleDate)) throw new BusinessRuleException("dueDate cannot precede saleDate");
        return dueDate;
    }

    private LocalDate requirePaymentDate(LocalDate saleDate, LocalDate paymentDate) {
        if (paymentDate == null) throw new InvalidArgumentException("paymentDate is required");
        if (paymentDate.isBefore(saleDate)) throw new BusinessRuleException("paymentDate cannot precede saleDate");
        if (paymentDate.isAfter(LocalDate.now(clock))) throw new InvalidArgumentException("paymentDate cannot be in the future");
        return paymentDate;
    }

    private BigDecimal requirePositive(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessRuleException("amount must be greater than zero");
        return value;
    }

    private BigDecimal currency(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
    private String optional(String value) { if (value == null) return null; String normalized = value.trim(); return normalized.isEmpty() ? null : normalized; }

    private OwnershipSaleResponseVO response(AnimalSaleRecord sale, OwnershipTransfer transfer) {
        var targetFarm = sale.targetFarmId() == null ? null : farms.findById(sale.targetFarmId()).orElse(null);
        return new OwnershipSaleResponseVO(sale.id(), sale.farmId(), sale.targetFarmId(),
                targetFarm == null ? null : targetFarm.name(), targetFarm == null ? null : targetFarm.tod(),
                sale.goatTechnicalId(),
                sale.goatRegistrationNumber(), sale.goatName(), sale.customerId(), sale.customer() == null ? null : sale.customer().name(), sale.saleDate(),
                sale.amount(), sale.dueDate(), sale.paymentStatus(), sale.paymentDate(), sale.notes(), transfer.id(), transfer.status());
    }
}
