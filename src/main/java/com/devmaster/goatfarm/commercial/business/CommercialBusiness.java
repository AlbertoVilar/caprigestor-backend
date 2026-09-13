package com.devmaster.goatfarm.commercial.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.application.exception.AuthorizationDeniedException;
import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditRecordVO;
import com.devmaster.goatfarm.audit.enums.OperationalAuditActionType;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.commercial.application.model.*;
import com.devmaster.goatfarm.commercial.application.ports.in.CommercialUseCase;
import com.devmaster.goatfarm.commercial.application.ports.out.AnimalSalePersistencePort;
import com.devmaster.goatfarm.commercial.application.ports.out.CustomerPersistencePort;
import com.devmaster.goatfarm.commercial.application.ports.out.MilkSalePersistencePort;
import com.devmaster.goatfarm.commercial.business.bo.*;
import com.devmaster.goatfarm.commercial.enums.ReceivableSourceType;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.business.bo.GoatExitRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.enums.GoatExitType;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommercialBusiness implements CommercialUseCase {
    private static final int CURRENCY_SCALE = 2;
    private static final int MEASURE_SCALE = 2;
    private final CustomerPersistencePort customerPersistencePort;
    private final AnimalSalePersistencePort animalSalePersistencePort;
    private final MilkSalePersistencePort milkSalePersistencePort;
    private final GoatFarmPersistencePort goatFarmPersistencePort;
    private final GoatManagementUseCase goatManagementUseCase;
    private final FarmAuthorizationUseCase authorization;
    private final EntityFinder entityFinder;
    private final OperationalAuditUseCase audit;

    public CommercialBusiness(CustomerPersistencePort customerPersistencePort,
                              AnimalSalePersistencePort animalSalePersistencePort,
                              MilkSalePersistencePort milkSalePersistencePort,
                              GoatFarmPersistencePort goatFarmPersistencePort,
                              GoatManagementUseCase goatManagementUseCase,
                              FarmAuthorizationUseCase authorization,
                              EntityFinder entityFinder,
                              OperationalAuditUseCase audit) {
        this.customerPersistencePort = customerPersistencePort;
        this.animalSalePersistencePort = animalSalePersistencePort;
        this.milkSalePersistencePort = milkSalePersistencePort;
        this.goatFarmPersistencePort = goatFarmPersistencePort;
        this.goatManagementUseCase = goatManagementUseCase;
        this.authorization = authorization;
        this.entityFinder = entityFinder;
        this.audit = audit;
    }

    @Override @Transactional
    public CustomerResponseVO createCustomer(Long farmId, CustomerRequestVO request) {
        FarmRecord farm = requireFarm(farmId);
        String name = required("name", request.name(), "Nome do cliente e obrigatorio");
        CustomerRecord saved = customerPersistencePort.save(new CustomerRecord(null, farm.id(), name, optional(request.document()), optional(request.phone()), optional(request.email()), optional(request.notes()), true, null, null));
        return customerResponse(saved);
    }

    @Override public List<CustomerResponseVO> listCustomers(Long farmId) {
        requireFarm(farmId);
        return customerPersistencePort.findCustomersByFarmId(farmId).stream().map(this::customerResponse).toList();
    }

    @Override @Transactional
    public AnimalSaleResponseVO createAnimalSale(Long farmId, AnimalSaleRequestVO request) {
        authorization.verifyFarmOwnership(farmId);
        requireFarm(farmId);
        CustomerRecord customer = activeCustomer(farmId, request.customerId());
        LocalDate saleDate = saleDate(request.saleDate());
        LocalDate dueDate = dueDate(saleDate, request.dueDate());
        LocalDate paymentDate = paymentDate(saleDate, request.paymentDate());
        BigDecimal amount = positive("amount", request.amount(), "Valor da venda deve ser maior que zero");
        String goatId = required("goatId", request.goatId(), "Cabra e obrigatoria");
        GoatResponseVO goat = ensureGoatReadyForSale(farmId, goatId, saleDate, optional(request.notes()));
        if ((goat.getTechnicalId() != null && animalSalePersistencePort.existsByFarmIdAndGoatTechnicalId(farmId, goat.getTechnicalId()))
                || (goat.getTechnicalId() == null && animalSalePersistencePort.existsByLegacyRegistrationNumber(goat.getRegistrationNumber()))) {
            throw new DuplicateEntityException("goatId", "Ja existe uma venda registrada para esta cabra.");
        }
        AnimalSaleRecord saved = animalSalePersistencePort.save(new AnimalSaleCommand(null, farmId, customer.id(), goat.getTechnicalId(), goat.getRegistrationNumber(), goat.getName(), saleDate, currency(amount), dueDate, status(paymentDate), paymentDate, optional(request.notes())));
        audit.record(new OperationalAuditRecordVO(farmId, saved.goatTechnicalId(), saved.goatRegistrationNumber(), OperationalAuditActionType.ANIMAL_SALE_CREATED, String.valueOf(saved.id()), "Venda do animal " + saved.goatRegistrationNumber() + " registrada para " + saved.customer().name() + " no valor de R$ " + saved.amount().toPlainString() + "."));
        return animalResponse(saved);
    }

    @Override public List<AnimalSaleResponseVO> listAnimalSales(Long farmId) {
        requireFarm(farmId);
        return animalSalePersistencePort.findAnimalSalesByFarmId(farmId).stream().map(this::animalResponse).toList();
    }

    @Override @Transactional
    public AnimalSaleResponseVO registerAnimalSalePayment(Long farmId, Long saleId, SalePaymentRequestVO request) {
        authorization.verifyFarmOwnership(farmId); requireFarm(farmId);
        AnimalSaleRecord sale = entityFinder.findOrThrow(() -> animalSalePersistencePort.findAnimalSaleByIdAndFarmId(saleId, farmId), "Venda de animal nao encontrada.");
        validatePayment(sale.saleDate(), sale.paymentStatus(), request.paymentDate());
        AnimalSaleRecord saved = animalSalePersistencePort.save(new AnimalSaleCommand(sale.id(), sale.farmId(), sale.customerId(), sale.goatTechnicalId(), sale.goatRegistrationNumber(), sale.goatName(), sale.saleDate(), sale.amount(), sale.dueDate(), SalePaymentStatus.PAID, request.paymentDate(), sale.notes()));
        audit.record(new OperationalAuditRecordVO(farmId, saved.goatTechnicalId(), saved.goatRegistrationNumber(), OperationalAuditActionType.ANIMAL_SALE_PAYMENT_REGISTERED, String.valueOf(saved.id()), "Recebimento da venda do animal " + saved.goatRegistrationNumber() + " registrado em " + request.paymentDate() + "."));
        return animalResponse(saved);
    }

    @Override @Transactional
    public MilkSaleResponseVO createMilkSale(Long farmId, MilkSaleRequestVO request) {
        authorization.verifyFarmOwnership(farmId); requireFarm(farmId);
        CustomerRecord customer = activeCustomer(farmId, request.customerId());
        LocalDate saleDate = saleDate(request.saleDate()); LocalDate dueDate = dueDate(saleDate, request.dueDate()); LocalDate paymentDate = paymentDate(saleDate, request.paymentDate());
        BigDecimal quantity = positive("quantityLiters", request.quantityLiters(), "Quantidade deve ser maior que zero");
        BigDecimal unitPrice = positive("unitPrice", request.unitPrice(), "Preco unitario deve ser maior que zero");
        BigDecimal total = currency(measure(quantity).multiply(currency(unitPrice)));
        MilkSaleRecord saved = milkSalePersistencePort.save(new MilkSaleCommand(null, farmId, customer.id(), saleDate, measure(quantity), currency(unitPrice), total, dueDate, status(paymentDate), paymentDate, optional(request.notes())));
        audit.record(new OperationalAuditRecordVO(farmId, null, OperationalAuditActionType.MILK_SALE_CREATED, String.valueOf(saved.id()), "Venda de leite registrada para " + saved.customer().name() + " com total de R$ " + saved.totalAmount().toPlainString() + "."));
        return milkResponse(saved);
    }

    @Override public List<MilkSaleResponseVO> listMilkSales(Long farmId) { requireFarm(farmId); return milkSalePersistencePort.findMilkSalesByFarmId(farmId).stream().map(this::milkResponse).toList(); }

    @Override @Transactional
    public MilkSaleResponseVO registerMilkSalePayment(Long farmId, Long saleId, SalePaymentRequestVO request) {
        authorization.verifyFarmOwnership(farmId); requireFarm(farmId);
        MilkSaleRecord sale = entityFinder.findOrThrow(() -> milkSalePersistencePort.findMilkSaleByIdAndFarmId(saleId, farmId), "Venda de leite nao encontrada.");
        validatePayment(sale.saleDate(), sale.paymentStatus(), request.paymentDate());
        MilkSaleRecord saved = milkSalePersistencePort.save(new MilkSaleCommand(sale.id(), sale.farmId(), sale.customerId(), sale.saleDate(), sale.quantityLiters(), sale.unitPrice(), sale.totalAmount(), sale.dueDate(), SalePaymentStatus.PAID, request.paymentDate(), sale.notes()));
        audit.record(new OperationalAuditRecordVO(farmId, null, OperationalAuditActionType.MILK_SALE_PAYMENT_REGISTERED, String.valueOf(saved.id()), "Recebimento da venda de leite " + saved.id() + " registrado em " + request.paymentDate() + "."));
        return milkResponse(saved);
    }

    @Override public List<ReceivableResponseVO> listReceivables(Long farmId) {
        requireFarm(farmId); List<ReceivableResponseVO> result = new ArrayList<>();
        animalSalePersistencePort.findAnimalSalesByFarmId(farmId).forEach(s -> result.add(new ReceivableResponseVO(ReceivableSourceType.ANIMAL_SALE, s.id(), "Venda do animal " + s.goatRegistrationNumber(), s.customer().id(), s.customer().name(), currency(s.amount()), s.dueDate(), s.paymentStatus(), s.paymentDate(), s.notes())));
        milkSalePersistencePort.findMilkSalesByFarmId(farmId).forEach(s -> result.add(new ReceivableResponseVO(ReceivableSourceType.MILK_SALE, s.id(), "Venda de leite de " + s.saleDate(), s.customer().id(), s.customer().name(), currency(s.totalAmount()), s.dueDate(), s.paymentStatus(), s.paymentDate(), s.notes())));
        return result.stream().sorted(Comparator.comparing(ReceivableResponseVO::paymentStatus).thenComparing(ReceivableResponseVO::dueDate, Comparator.nullsLast(LocalDate::compareTo)).thenComparing(ReceivableResponseVO::sourceId)).toList();
    }

    @Override public CommercialSummaryVO getSummary(Long farmId) {
        requireFarm(farmId); List<AnimalSaleRecord> animals = animalSalePersistencePort.findAnimalSalesByFarmId(farmId); List<MilkSaleRecord> milk = milkSalePersistencePort.findMilkSalesByFarmId(farmId); List<ReceivableResponseVO> rec = listReceivables(farmId);
        BigDecimal animalTotal = animals.stream().map(AnimalSaleRecord::amount).reduce(BigDecimal.ZERO, BigDecimal::add); BigDecimal milkQty = milk.stream().map(MilkSaleRecord::quantityLiters).reduce(BigDecimal.ZERO, BigDecimal::add); BigDecimal milkTotal = milk.stream().map(MilkSaleRecord::totalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CommercialSummaryVO(customerPersistencePort.countCustomersByFarmId(farmId), animals.size(), currency(animalTotal), milk.size(), measure(milkQty), currency(milkTotal), rec.stream().filter(r -> r.paymentStatus() == SalePaymentStatus.OPEN).count(), currency(rec.stream().filter(r -> r.paymentStatus() == SalePaymentStatus.OPEN).map(ReceivableResponseVO::amount).reduce(BigDecimal.ZERO, BigDecimal::add)), rec.stream().filter(r -> r.paymentStatus() == SalePaymentStatus.PAID).count(), currency(rec.stream().filter(r -> r.paymentStatus() == SalePaymentStatus.PAID).map(ReceivableResponseVO::amount).reduce(BigDecimal.ZERO, BigDecimal::add)));
    }

    private FarmRecord requireFarm(Long farmId) { if (!authorization.canManageFarm(farmId)) throw new AuthorizationDeniedException("Usuario nao pode gerenciar esta fazenda."); return goatFarmPersistencePort.findById(farmId).orElseThrow(() -> new com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException("Fazenda nao encontrada.")); }
    private CustomerRecord activeCustomer(Long farmId, Long id) { if (id == null) throw new InvalidArgumentException("customerId", "Cliente e obrigatorio"); CustomerRecord c = entityFinder.findOrThrow(() -> customerPersistencePort.findCustomerByIdAndFarmId(id, farmId), "Cliente nao encontrado."); if (!c.active()) throw new BusinessRuleException("customerId", "Cliente esta inativo e nao pode ser utilizado."); return c; }
    private GoatResponseVO ensureGoatReadyForSale(Long farmId, String id, LocalDate date, String notes) { GoatResponseVO goat = goatManagementUseCase.findGoatById(farmId, id); if (goat.getBirthDate() != null && date.isBefore(goat.getBirthDate())) throw new BusinessRuleException("saleDate", "Data da venda nao pode ser anterior ao nascimento da cabra."); if (goat.getStatus() == GoatStatus.ATIVO) { goatManagementUseCase.exitGoat(farmId, id, GoatExitRequestVO.builder().exitType(GoatExitType.VENDA).exitDate(date).notes(notes).build()); return goat; } if (goat.getStatus() != GoatStatus.VENDIDO) throw new BusinessRuleException("goatId", "A cabra informada nao esta em estado compativel com venda."); if (goat.getExitType() != GoatExitType.VENDA) throw new BusinessRuleException("goatId", "A cabra ja possui saida registrada com tipo diferente de venda."); if (!date.equals(goat.getExitDate())) throw new BusinessRuleException("saleDate", "A data da venda deve coincidir com a saida comercial ja registrada para a cabra."); return goat; }
    private void validatePayment(LocalDate saleDate, SalePaymentStatus status, LocalDate date) { if (status == SalePaymentStatus.PAID) throw new BusinessRuleException("paymentDate", "Esta venda ja esta marcada como paga."); if (date == null) throw new InvalidArgumentException("paymentDate", "Data de pagamento e obrigatoria"); if (date.isBefore(saleDate)) throw new BusinessRuleException("paymentDate", "Data de pagamento nao pode ser anterior a data da venda."); if (date.isAfter(LocalDate.now())) throw new InvalidArgumentException("paymentDate", "Data de pagamento nao pode estar no futuro."); }
    private LocalDate saleDate(LocalDate d) { if (d == null) throw new InvalidArgumentException("saleDate", "Data da venda e obrigatoria"); if (d.isAfter(LocalDate.now())) throw new InvalidArgumentException("saleDate", "Data da venda nao pode estar no futuro."); return d; }
    private LocalDate dueDate(LocalDate sale, LocalDate due) { if (due == null) throw new InvalidArgumentException("dueDate", "Data de vencimento e obrigatoria"); if (due.isBefore(sale)) throw new BusinessRuleException("dueDate", "Data de vencimento nao pode ser anterior a data da venda."); return due; }
    private LocalDate paymentDate(LocalDate sale, LocalDate payment) { if (payment == null) return null; if (payment.isBefore(sale)) throw new BusinessRuleException("paymentDate", "Data de pagamento nao pode ser anterior a data da venda."); if (payment.isAfter(LocalDate.now())) throw new InvalidArgumentException("paymentDate", "Data de pagamento nao pode estar no futuro."); return payment; }
    private String required(String field, String value, String msg) { String v = optional(value); if (v == null) throw new InvalidArgumentException(field, msg); return v; }
    private String optional(String value) { if (value == null) return null; String v = value.trim(); return v.isEmpty() ? null : v; }
    private BigDecimal positive(String field, BigDecimal value, String msg) { if (value == null) throw new InvalidArgumentException(field, msg); if (value.compareTo(BigDecimal.ZERO) <= 0) throw new BusinessRuleException(field, msg); return value; }
    private SalePaymentStatus status(LocalDate date) { return date == null ? SalePaymentStatus.OPEN : SalePaymentStatus.PAID; }
    private BigDecimal currency(BigDecimal v) { return v.setScale(CURRENCY_SCALE, RoundingMode.HALF_UP); }
    private BigDecimal measure(BigDecimal v) { return v.setScale(MEASURE_SCALE, RoundingMode.HALF_UP); }
    private CustomerResponseVO customerResponse(CustomerRecord c) { return new CustomerResponseVO(c.id(), c.name(), c.document(), c.phone(), c.email(), c.notes(), c.active()); }
    private AnimalSaleResponseVO animalResponse(AnimalSaleRecord s) { return new AnimalSaleResponseVO(s.id(), s.goatTechnicalId(), s.goatRegistrationNumber(), s.goatName(), s.customer().id(), s.customer().name(), s.saleDate(), currency(s.amount()), s.dueDate(), s.paymentStatus(), s.paymentDate(), s.notes()); }
    private MilkSaleResponseVO milkResponse(MilkSaleRecord s) { return new MilkSaleResponseVO(s.id(), s.customer().id(), s.customer().name(), s.saleDate(), measure(s.quantityLiters()), currency(s.unitPrice()), currency(s.totalAmount()), s.dueDate(), s.paymentStatus(), s.paymentDate(), s.notes()); }
}
