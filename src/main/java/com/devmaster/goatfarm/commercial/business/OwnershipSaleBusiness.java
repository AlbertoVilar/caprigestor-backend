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
import com.devmaster.goatfarm.commercial.application.model.CustomerRecord;
import com.devmaster.goatfarm.commercial.application.ports.in.OwnershipSaleUseCase;
import com.devmaster.goatfarm.commercial.application.ports.out.AnimalSalePersistencePort;
import com.devmaster.goatfarm.commercial.application.ports.out.CustomerPersistencePort;
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
import com.devmaster.goatfarm.goatownership.application.model.GoatOwnershipLockState;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatCurrentOwnerProjectionPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipLockPort;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipPeriodPersistencePort;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipTransferPersistencePort;
import com.devmaster.goatfarm.goatownership.domain.GoatOwnershipPeriod;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipExitType;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Commercial process for an internal farm buyer. It never treats animal_sale
 * as ownership authority: only the ledger handoff completes ownership.
 */
@Service
@Transactional(readOnly = true)
public class OwnershipSaleBusiness implements OwnershipSaleUseCase {
    private final CustomerPersistencePort customers;
    private final AnimalSalePersistencePort sales;
    private final GoatFarmPersistencePort farms;
    private final GoatManagementUseCase goats;
    private final FarmAuthorizationUseCase authorization;
    private final CurrentPrincipalQueryUseCase principalQuery;
    private final GoatOwnershipLockPort ownershipLock;
    private final GoatOwnershipPeriodPersistencePort periods;
    private final OwnershipTransferPersistencePort transfers;
    private final GoatCurrentOwnerProjectionPort projection;
    private final OperationalAuditUseCase audit;
    private final Clock clock;

    public OwnershipSaleBusiness(CustomerPersistencePort customers,
                                 AnimalSalePersistencePort sales,
                                 GoatFarmPersistencePort farms,
                                 GoatManagementUseCase goats,
                                 FarmAuthorizationUseCase authorization,
                                 CurrentPrincipalQueryUseCase principalQuery,
                                 GoatOwnershipLockPort ownershipLock,
                                 GoatOwnershipPeriodPersistencePort periods,
                                 OwnershipTransferPersistencePort transfers,
                                 GoatCurrentOwnerProjectionPort projection,
                                 OperationalAuditUseCase audit,
                                 Clock clock) {
        this.customers = customers;
        this.sales = sales;
        this.farms = farms;
        this.goats = goats;
        this.authorization = authorization;
        this.principalQuery = principalQuery;
        this.ownershipLock = ownershipLock;
        this.periods = periods;
        this.transfers = transfers;
        this.projection = projection;
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
        OwnershipTransfer duplicate = transfers.findByRequesterAndIdempotencyKey(requesterId, idempotencyKey).orElse(null);
        if (duplicate != null) {
            return resolveIdempotentSale(duplicate, sourceFarmId, request, technicalToken);
        }
        requireTargetFarm(sourceFarmId, request.targetFarmId());
        CustomerRecord customer = activeCustomer(sourceFarmId, request.customerId());
        GoatResponseVO goat = goats.findGoatById(sourceFarmId, technicalToken);
        GoatId goatId = GoatId.of(requireTechnicalId(goat));
        GoatOwnershipLockState lock = lock(goatId);
        duplicate = transfers.findByRequesterAndIdempotencyKey(requesterId, idempotencyKey).orElse(null);
        if (duplicate != null) {
            return resolveIdempotentSale(duplicate, sourceFarmId, request, technicalToken);
        }
        GoatOwnershipPeriod source = requireSource(lock, sourceFarmId);
        transfers.findPendingByGoatId(goatId).ifPresent(pending -> {
            throw new BusinessRuleException("another ownership process is already pending for this GoatId");
        });
        if (sales.existsByFarmIdAndGoatTechnicalId(sourceFarmId, goatId.value())) {
            throw new DuplicateEntityException("goatId", "Ja existe uma venda registrada para esta cabra nesta fazenda.");
        }
        LocalDate saleDate = requireSaleDate(request.saleDate());
        LocalDate dueDate = requireDueDate(saleDate, request.dueDate());
        AnimalSaleRecord sale = sales.save(new AnimalSaleCommand(null, sourceFarmId, customer.id(), goatId.value(),
                goat.getRegistrationNumber(), goat.getName(), saleDate, currency(requirePositive(request.amount())), dueDate,
                SalePaymentStatus.OPEN, null, optional(request.notes()), request.targetFarmId()));
        OwnershipTransfer transfer = OwnershipTransfer.request(goatId, source.farmId(), request.targetFarmId(),
                OwnershipTransferKind.INTERNAL_SALE, "OWNERSHIP_SALE:" + sale.id(), idempotencyKey,
                Instant.now(clock), requesterId, sale.id());
        transfer = transfers.save(transfer);
        audit.record(new OperationalAuditRecordVO(sourceFarmId, goatId.value(), goat.getRegistrationNumber(),
                OperationalAuditActionType.ANIMAL_SALE_CREATED, String.valueOf(sale.id()),
                "Venda com transferencia de propriedade solicitada para a fazenda " + request.targetFarmId() + "."));
        return response(sale, transfer);
    }

    @Override
    @Transactional
    public OwnershipSaleResponseVO acceptOwnershipSale(Long sourceFarmId, Long saleId, SalePaymentRequestVO payment) {
        AnimalSaleRecord sale = requireSale(sourceFarmId, saleId);
        OwnershipTransfer transfer = requireSaleTransfer(sale);
        GoatOwnershipLockState lock = lock(transfer.goatId());
        // The first lookup only identifies the GoatId to lock. Every mutable
        // record is reloaded after that canonical lock so a stale snapshot can
        // never regress a terminal or concurrently completed workflow.
        sale = requireSale(sourceFarmId, saleId);
        transfer = requireSaleTransfer(sale);
        requireTargetAdministrator(transfer.targetFarmId());
        if (transfer.status() == OwnershipTransferStatus.COMPLETED) {
            validateCompleted(sale, transfer, lock);
            return response(sale, transfer);
        }
        if (transfer.status() != OwnershipTransferStatus.REQUESTED || sale.paymentStatus() != SalePaymentStatus.OPEN) {
            throw new BusinessRuleException("only an open requested ownership sale can be accepted");
        }
        GoatOwnershipPeriod source = requireSource(lock, sourceFarmId);
        LocalDate paidOn = requirePaymentDate(sale.saleDate(), payment == null ? null : payment.paymentDate());
        long actorId = requirePrincipalId(principalQuery.requireCurrent());
        AnimalSaleRecord paidSale = sales.save(new AnimalSaleCommand(sale.id(), sale.farmId(), sale.customerId(),
                sale.goatTechnicalId(), sale.goatRegistrationNumber(), sale.goatName(), sale.saleDate(), sale.amount(),
                sale.dueDate(), SalePaymentStatus.PAID, paidOn, sale.notes(), sale.targetFarmId()));
        Instant effectiveAt = Instant.now(clock);
        source.close(effectiveAt, OwnershipExitType.EXTERNAL_SALE);
        GoatOwnershipPeriod target = GoatOwnershipPeriod.open(transfer.goatId(), transfer.targetFarmId(), effectiveAt,
                OwnershipEntryType.PURCHASE, "OWNERSHIP_SALE:" + sale.id());
        List<GoatOwnershipPeriod> history = new ArrayList<>(periods.findByGoatIdOrderByStartedAt(transfer.goatId()));
        replacePeriod(history, source);
        history.add(target);
        GoatOwnershipPeriod.ensureConsistent(history);
        periods.handoff(source, target);
        if (!projection.moveFromTo(transfer.goatId(), source.farmId(), transfer.targetFarmId())) {
            throw new BusinessRuleException("current-owner projection drift detected; ownership sale rolled back");
        }
        transfer.acceptAndComplete(effectiveAt, effectiveAt, actorId, effectiveAt);
        transfer = transfers.save(transfer);
        audit.record(new OperationalAuditRecordVO(sourceFarmId, paidSale.goatTechnicalId(), paidSale.goatRegistrationNumber(),
                OperationalAuditActionType.ANIMAL_SALE_PAYMENT_REGISTERED, String.valueOf(paidSale.id()),
                "Venda com transferencia de propriedade aceita e recebida em " + paidOn + "."));
        return response(paidSale, transfer);
    }

    @Override
    @Transactional
    public OwnershipSaleResponseVO rejectOwnershipSale(Long sourceFarmId, Long saleId) {
        AnimalSaleRecord sale = requireSale(sourceFarmId, saleId);
        OwnershipTransfer transfer = requireSaleTransfer(sale);
        lock(transfer.goatId());
        sale = requireSale(sourceFarmId, saleId);
        transfer = requireSaleTransfer(sale);
        requireTargetAdministrator(transfer.targetFarmId());
        if (transfer.status() == OwnershipTransferStatus.REJECTED) return response(sale, transfer);
        if (transfer.status() != OwnershipTransferStatus.REQUESTED) throw new BusinessRuleException("only a requested ownership sale can be rejected");
        transfer.reject();
        return response(sale, transfers.save(transfer));
    }

    @Override
    @Transactional
    public OwnershipSaleResponseVO cancelOwnershipSale(Long sourceFarmId, Long saleId) {
        requireSeller(sourceFarmId);
        AnimalSaleRecord sale = requireSale(sourceFarmId, saleId);
        OwnershipTransfer transfer = requireSaleTransfer(sale);
        lock(transfer.goatId());
        sale = requireSale(sourceFarmId, saleId);
        transfer = requireSaleTransfer(sale);
        if (transfer.status() == OwnershipTransferStatus.CANCELLED) return response(sale, transfer);
        if (transfer.status() != OwnershipTransferStatus.REQUESTED) throw new BusinessRuleException("only a requested ownership sale can be cancelled");
        transfer.cancel(Instant.now(clock));
        return response(sale, transfers.save(transfer));
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
                                                           OwnershipSaleRequestVO request, String technicalToken) {
        if (transfer.kind() != OwnershipTransferKind.INTERNAL_SALE || !Objects.equals(transfer.sourceFarmId(), sourceFarmId)
                || transfer.targetFarmId() != request.targetFarmId() || !transfer.goatId().equals(GoatId.of(parseTechnicalId(technicalToken)))
                || transfer.saleId() == null) {
            throw new BusinessRuleException("idempotency key already represents a different ownership process");
        }
        AnimalSaleRecord sale = sales.findAnimalSaleById(transfer.saleId())
                .orElseThrow(() -> new BusinessRuleException("ownership sale transfer has no sale record"));
        if (!Objects.equals(sale.customerId(), request.customerId())
                || sale.amount().compareTo(currency(requirePositive(request.amount()))) != 0
                || !Objects.equals(sale.saleDate(), requireSaleDate(request.saleDate()))
                || !Objects.equals(sale.dueDate(), requireDueDate(request.saleDate(), request.dueDate()))
                || !Objects.equals(sale.notes(), optional(request.notes()))) {
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
        OwnershipTransfer transfer = transfers.findBySaleId(sale.id())
                .orElseThrow(() -> new BusinessRuleException("ownership sale has no canonical transfer"));
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

    private CustomerRecord activeCustomer(Long farmId, Long customerId) {
        if (customerId == null) throw new InvalidArgumentException("customerId is required");
        CustomerRecord customer = customers.findCustomerByIdAndFarmId(customerId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
        if (!customer.active()) throw new BusinessRuleException("customer is inactive");
        return customer;
    }

    private GoatOwnershipLockState lock(GoatId goatId) {
        return ownershipLock.lockGoatOwnership(goatId).orElseThrow(() -> new ResourceNotFoundException("Goat not found: " + goatId));
    }

    private GoatOwnershipPeriod requireSource(GoatOwnershipLockState lock, long sourceFarmId) {
        GoatOwnershipPeriod source = lock.openPeriod().orElseThrow(() -> new BusinessRuleException("Goat has no canonical open ownership period"));
        if (source.farmId() != sourceFarmId) throw new BusinessRuleException("seller is not the canonical current owner");
        return source;
    }

    private void validateCompleted(AnimalSaleRecord sale, OwnershipTransfer transfer, GoatOwnershipLockState lock) {
        if (sale.paymentStatus() != SalePaymentStatus.PAID || sale.paymentDate() == null) throw new BusinessRuleException("completed ownership sale has incomplete payment state");
        GoatOwnershipPeriod current = lock.openPeriod().orElseThrow(() -> new BusinessRuleException("Goat has no canonical open ownership period"));
        if (current.farmId() != transfer.targetFarmId()) throw new BusinessRuleException("completed ownership sale does not match canonical owner");
    }

    private void replacePeriod(List<GoatOwnershipPeriod> history, GoatOwnershipPeriod replacement) {
        for (int index = 0; index < history.size(); index++) {
            GoatOwnershipPeriod candidate = history.get(index);
            if (candidate.id() != null && candidate.id().equals(replacement.id())) { history.set(index, replacement); return; }
        }
        throw new BusinessRuleException("locked open ownership period is absent from ownership history");
    }

    private void validateRequest(OwnershipSaleRequestVO request) {
        if (request == null) throw new InvalidArgumentException("ownership sale request is required");
        if (optional(request.goatId()) == null) throw new InvalidArgumentException("goatId is required");
        if (optional(request.idempotencyKey()) == null) throw new InvalidArgumentException("idempotencyKey is required");
        parseTechnicalId(request.goatId());
    }

    private long requireTechnicalId(GoatResponseVO goat) {
        if (goat == null || goat.getTechnicalId() == null || goat.getTechnicalId() <= 0) throw new BusinessRuleException("Goat has no valid technical identity");
        return goat.getTechnicalId();
    }

    private long parseTechnicalId(String value) {
        try {
            String normalized = value.trim();
            String numeric = normalized.startsWith("technical-") ? normalized.substring("technical-".length()) : normalized;
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
        return new OwnershipSaleResponseVO(sale.id(), sale.farmId(), sale.targetFarmId(), sale.goatTechnicalId(),
                sale.goatRegistrationNumber(), sale.goatName(), sale.customerId(), sale.customer().name(), sale.saleDate(),
                sale.amount(), sale.dueDate(), sale.paymentStatus(), sale.paymentDate(), sale.notes(), transfer.id(), transfer.status());
    }
}
