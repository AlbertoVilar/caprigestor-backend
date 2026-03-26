package com.devmaster.goatfarm.commercial.business;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.commercial.application.ports.in.CommercialUseCase;
import com.devmaster.goatfarm.commercial.application.ports.out.CommercialPersistencePort;
import com.devmaster.goatfarm.commercial.business.bo.AnimalSaleRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.AnimalSaleResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.CommercialSummaryVO;
import com.devmaster.goatfarm.commercial.business.bo.CustomerRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.CustomerResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.MilkSaleRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.MilkSaleResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.ReceivableResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO;
import com.devmaster.goatfarm.commercial.enums.ReceivableSourceType;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import com.devmaster.goatfarm.commercial.persistence.entity.AnimalSale;
import com.devmaster.goatfarm.commercial.persistence.entity.Customer;
import com.devmaster.goatfarm.commercial.persistence.entity.MilkSale;
import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
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
import java.util.Objects;

@Service
@Transactional(readOnly = true)
public class CommercialBusiness implements CommercialUseCase {

    private static final int CURRENCY_SCALE = 2;
    private static final int MEASURE_SCALE = 2;

    private final CommercialPersistencePort commercialPersistencePort;
    private final GoatFarmPersistencePort goatFarmPersistencePort;
    private final GoatManagementUseCase goatManagementUseCase;
    private final OwnershipService ownershipService;
    private final EntityFinder entityFinder;

    public CommercialBusiness(
            CommercialPersistencePort commercialPersistencePort,
            GoatFarmPersistencePort goatFarmPersistencePort,
            GoatManagementUseCase goatManagementUseCase,
            OwnershipService ownershipService,
            EntityFinder entityFinder
    ) {
        this.commercialPersistencePort = commercialPersistencePort;
        this.goatFarmPersistencePort = goatFarmPersistencePort;
        this.goatManagementUseCase = goatManagementUseCase;
        this.ownershipService = ownershipService;
        this.entityFinder = entityFinder;
    }

    @Override
    @Transactional
    public CustomerResponseVO createCustomer(Long farmId, CustomerRequestVO requestVO) {
        GoatFarm farm = requireFarm(farmId);

        Customer customer = Customer.builder()
                .farm(farm)
                .name(normalizeRequiredText("name", requestVO.name(), "Nome do cliente e obrigatorio"))
                .document(normalizeOptionalText(requestVO.document()))
                .phone(normalizeOptionalText(requestVO.phone()))
                .email(normalizeOptionalText(requestVO.email()))
                .notes(normalizeOptionalText(requestVO.notes()))
                .active(true)
                .build();

        return toCustomerResponse(commercialPersistencePort.saveCustomer(customer));
    }

    @Override
    public List<CustomerResponseVO> listCustomers(Long farmId) {
        requireFarm(farmId);
        return commercialPersistencePort.findCustomersByFarmId(farmId).stream()
                .map(this::toCustomerResponse)
                .toList();
    }

    @Override
    @Transactional
    public AnimalSaleResponseVO createAnimalSale(Long farmId, AnimalSaleRequestVO requestVO) {
        GoatFarm farm = requireFarm(farmId);
        Customer customer = requireActiveCustomer(farmId, requestVO.customerId());
        LocalDate saleDate = requireSaleDate("saleDate", requestVO.saleDate());
        LocalDate dueDate = requireDueDate(saleDate, requestVO.dueDate());
        LocalDate paymentDate = normalizePaymentDate(saleDate, requestVO.paymentDate());
        BigDecimal amount = requirePositiveAmount("amount", requestVO.amount(), "Valor da venda deve ser maior que zero");

        String goatId = normalizeRequiredText("goatId", requestVO.goatId(), "Cabra e obrigatoria");
        GoatResponseVO goat = ensureGoatReadyForSale(farmId, goatId, saleDate, normalizeOptionalText(requestVO.notes()));

        if (commercialPersistencePort.existsAnimalSaleByGoatRegistrationNumber(goat.getRegistrationNumber())) {
            throw new DuplicateEntityException("goatId", "Ja existe uma venda registrada para esta cabra.");
        }

        AnimalSale animalSale = AnimalSale.builder()
                .farm(farm)
                .customer(customer)
                .goatRegistrationNumber(goat.getRegistrationNumber())
                .goatName(goat.getName())
                .saleDate(saleDate)
                .amount(scaleCurrency(amount))
                .dueDate(dueDate)
                .paymentStatus(resolvePaymentStatus(paymentDate))
                .paymentDate(paymentDate)
                .notes(normalizeOptionalText(requestVO.notes()))
                .build();

        return toAnimalSaleResponse(commercialPersistencePort.saveAnimalSale(animalSale));
    }

    @Override
    public List<AnimalSaleResponseVO> listAnimalSales(Long farmId) {
        requireFarm(farmId);
        return commercialPersistencePort.findAnimalSalesByFarmId(farmId).stream()
                .map(this::toAnimalSaleResponse)
                .toList();
    }

    @Override
    @Transactional
    public AnimalSaleResponseVO registerAnimalSalePayment(Long farmId, Long saleId, SalePaymentRequestVO requestVO) {
        requireFarm(farmId);
        AnimalSale animalSale = entityFinder.findOrThrow(
                () -> commercialPersistencePort.findAnimalSaleByIdAndFarmId(saleId, farmId),
                "Venda de animal nao encontrada."
        );

        applyPayment("paymentDate", animalSale.getSaleDate(), animalSale.getPaymentStatus(), requestVO.paymentDate());
        animalSale.setPaymentStatus(SalePaymentStatus.PAID);
        animalSale.setPaymentDate(requestVO.paymentDate());

        return toAnimalSaleResponse(commercialPersistencePort.saveAnimalSale(animalSale));
    }

    @Override
    @Transactional
    public MilkSaleResponseVO createMilkSale(Long farmId, MilkSaleRequestVO requestVO) {
        GoatFarm farm = requireFarm(farmId);
        Customer customer = requireActiveCustomer(farmId, requestVO.customerId());
        LocalDate saleDate = requireSaleDate("saleDate", requestVO.saleDate());
        LocalDate dueDate = requireDueDate(saleDate, requestVO.dueDate());
        LocalDate paymentDate = normalizePaymentDate(saleDate, requestVO.paymentDate());
        BigDecimal quantityLiters = requirePositiveAmount("quantityLiters", requestVO.quantityLiters(), "Quantidade deve ser maior que zero");
        BigDecimal unitPrice = requirePositiveAmount("unitPrice", requestVO.unitPrice(), "Preco unitario deve ser maior que zero");
        BigDecimal totalAmount = scaleCurrency(scaleMeasure(quantityLiters).multiply(scaleCurrency(unitPrice)));

        MilkSale milkSale = MilkSale.builder()
                .farm(farm)
                .customer(customer)
                .saleDate(saleDate)
                .quantityLiters(scaleMeasure(quantityLiters))
                .unitPrice(scaleCurrency(unitPrice))
                .totalAmount(totalAmount)
                .dueDate(dueDate)
                .paymentStatus(resolvePaymentStatus(paymentDate))
                .paymentDate(paymentDate)
                .notes(normalizeOptionalText(requestVO.notes()))
                .build();

        return toMilkSaleResponse(commercialPersistencePort.saveMilkSale(milkSale));
    }

    @Override
    public List<MilkSaleResponseVO> listMilkSales(Long farmId) {
        requireFarm(farmId);
        return commercialPersistencePort.findMilkSalesByFarmId(farmId).stream()
                .map(this::toMilkSaleResponse)
                .toList();
    }

    @Override
    @Transactional
    public MilkSaleResponseVO registerMilkSalePayment(Long farmId, Long saleId, SalePaymentRequestVO requestVO) {
        requireFarm(farmId);
        MilkSale milkSale = entityFinder.findOrThrow(
                () -> commercialPersistencePort.findMilkSaleByIdAndFarmId(saleId, farmId),
                "Venda de leite nao encontrada."
        );

        applyPayment("paymentDate", milkSale.getSaleDate(), milkSale.getPaymentStatus(), requestVO.paymentDate());
        milkSale.setPaymentStatus(SalePaymentStatus.PAID);
        milkSale.setPaymentDate(requestVO.paymentDate());

        return toMilkSaleResponse(commercialPersistencePort.saveMilkSale(milkSale));
    }

    @Override
    public List<ReceivableResponseVO> listReceivables(Long farmId) {
        requireFarm(farmId);

        List<ReceivableResponseVO> receivables = new ArrayList<>();
        commercialPersistencePort.findAnimalSalesByFarmId(farmId).forEach(sale ->
                receivables.add(new ReceivableResponseVO(
                        ReceivableSourceType.ANIMAL_SALE,
                        sale.getId(),
                        "Venda do animal " + sale.getGoatRegistrationNumber(),
                        sale.getCustomer().getId(),
                        sale.getCustomer().getName(),
                        scaleCurrency(sale.getAmount()),
                        sale.getDueDate(),
                        sale.getPaymentStatus(),
                        sale.getPaymentDate(),
                        sale.getNotes()
                )));

        commercialPersistencePort.findMilkSalesByFarmId(farmId).forEach(sale ->
                receivables.add(new ReceivableResponseVO(
                        ReceivableSourceType.MILK_SALE,
                        sale.getId(),
                        "Venda de leite de " + sale.getSaleDate(),
                        sale.getCustomer().getId(),
                        sale.getCustomer().getName(),
                        scaleCurrency(sale.getTotalAmount()),
                        sale.getDueDate(),
                        sale.getPaymentStatus(),
                        sale.getPaymentDate(),
                        sale.getNotes()
                )));

        return receivables.stream()
                .sorted(Comparator
                        .comparing(ReceivableResponseVO::paymentStatus)
                        .thenComparing(ReceivableResponseVO::dueDate, Comparator.nullsLast(LocalDate::compareTo))
                        .thenComparing(ReceivableResponseVO::sourceId))
                .toList();
    }

    @Override
    public CommercialSummaryVO getSummary(Long farmId) {
        requireFarm(farmId);
        List<AnimalSale> animalSales = commercialPersistencePort.findAnimalSalesByFarmId(farmId);
        List<MilkSale> milkSales = commercialPersistencePort.findMilkSalesByFarmId(farmId);

        BigDecimal animalSalesTotal = animalSales.stream()
                .map(AnimalSale::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal milkSalesQuantity = milkSales.stream()
                .map(MilkSale::getQuantityLiters)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal milkSalesTotal = milkSales.stream()
                .map(MilkSale::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ReceivableResponseVO> receivables = listReceivables(farmId);

        long openReceivablesCount = receivables.stream().filter(item -> item.paymentStatus() == SalePaymentStatus.OPEN).count();
        BigDecimal openReceivablesTotal = receivables.stream()
                .filter(item -> item.paymentStatus() == SalePaymentStatus.OPEN)
                .map(ReceivableResponseVO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long paidReceivablesCount = receivables.stream().filter(item -> item.paymentStatus() == SalePaymentStatus.PAID).count();
        BigDecimal paidReceivablesTotal = receivables.stream()
                .filter(item -> item.paymentStatus() == SalePaymentStatus.PAID)
                .map(ReceivableResponseVO::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CommercialSummaryVO(
                commercialPersistencePort.countCustomersByFarmId(farmId),
                animalSales.size(),
                scaleCurrency(animalSalesTotal),
                milkSales.size(),
                scaleMeasure(milkSalesQuantity),
                scaleCurrency(milkSalesTotal),
                openReceivablesCount,
                scaleCurrency(openReceivablesTotal),
                paidReceivablesCount,
                scaleCurrency(paidReceivablesTotal)
        );
    }

    private GoatFarm requireFarm(Long farmId) {
        ownershipService.verifyFarmOwnership(farmId);
        return entityFinder.findOrThrow(
                () -> goatFarmPersistencePort.findById(farmId),
                "Fazenda nao encontrada."
        );
    }

    private Customer requireActiveCustomer(Long farmId, Long customerId) {
        if (customerId == null) {
            throw new InvalidArgumentException("customerId", "Cliente e obrigatorio");
        }

        Customer customer = entityFinder.findOrThrow(
                () -> commercialPersistencePort.findCustomerByIdAndFarmId(customerId, farmId),
                "Cliente nao encontrado."
        );

        if (!customer.isActive()) {
            throw new BusinessRuleException("customerId", "Cliente esta inativo e nao pode ser utilizado.");
        }

        return customer;
    }

    private GoatResponseVO ensureGoatReadyForSale(Long farmId, String goatId, LocalDate saleDate, String notes) {
        GoatResponseVO goat = goatManagementUseCase.findGoatById(farmId, goatId);

        if (goat.getBirthDate() != null && saleDate.isBefore(goat.getBirthDate())) {
            throw new BusinessRuleException("saleDate", "Data da venda nao pode ser anterior ao nascimento da cabra.");
        }

        if (goat.getStatus() == GoatStatus.ATIVO) {
            goatManagementUseCase.exitGoat(
                    farmId,
                    goatId,
                    GoatExitRequestVO.builder()
                            .exitType(GoatExitType.VENDA)
                            .exitDate(saleDate)
                            .notes(notes)
                            .build()
            );
            return goat;
        }

        if (goat.getStatus() != GoatStatus.VENDIDO) {
            throw new BusinessRuleException("goatId", "A cabra informada nao esta em estado compativel com venda.");
        }

        if (goat.getExitType() != GoatExitType.VENDA) {
            throw new BusinessRuleException("goatId", "A cabra ja possui saida registrada com tipo diferente de venda.");
        }

        if (!saleDate.equals(goat.getExitDate())) {
            throw new BusinessRuleException("saleDate", "A data da venda deve coincidir com a saida comercial ja registrada para a cabra.");
        }

        return goat;
    }

    private LocalDate requireSaleDate(String fieldName, LocalDate saleDate) {
        if (saleDate == null) {
            throw new InvalidArgumentException(fieldName, "Data da venda e obrigatoria");
        }
        if (saleDate.isAfter(LocalDate.now())) {
            throw new InvalidArgumentException(fieldName, "Data da venda nao pode estar no futuro.");
        }
        return saleDate;
    }

    private LocalDate requireDueDate(LocalDate saleDate, LocalDate dueDate) {
        if (dueDate == null) {
            throw new InvalidArgumentException("dueDate", "Data de vencimento e obrigatoria");
        }
        if (dueDate.isBefore(saleDate)) {
            throw new BusinessRuleException("dueDate", "Data de vencimento nao pode ser anterior a data da venda.");
        }
        return dueDate;
    }

    private LocalDate normalizePaymentDate(LocalDate saleDate, LocalDate paymentDate) {
        if (paymentDate == null) {
            return null;
        }
        if (paymentDate.isBefore(saleDate)) {
            throw new BusinessRuleException("paymentDate", "Data de pagamento nao pode ser anterior a data da venda.");
        }
        if (paymentDate.isAfter(LocalDate.now())) {
            throw new InvalidArgumentException("paymentDate", "Data de pagamento nao pode estar no futuro.");
        }
        return paymentDate;
    }

    private void applyPayment(String fieldName, LocalDate saleDate, SalePaymentStatus currentStatus, LocalDate paymentDate) {
        if (currentStatus == SalePaymentStatus.PAID) {
            throw new BusinessRuleException(fieldName, "Esta venda ja esta marcada como paga.");
        }
        if (paymentDate == null) {
            throw new InvalidArgumentException(fieldName, "Data de pagamento e obrigatoria");
        }
        if (paymentDate.isBefore(saleDate)) {
            throw new BusinessRuleException(fieldName, "Data de pagamento nao pode ser anterior a data da venda.");
        }
        if (paymentDate.isAfter(LocalDate.now())) {
            throw new InvalidArgumentException(fieldName, "Data de pagamento nao pode estar no futuro.");
        }
    }

    private String normalizeRequiredText(String fieldName, String value, String message) {
        String normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new InvalidArgumentException(fieldName, message);
        }
        return normalized;
    }

    private String normalizeOptionalText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private BigDecimal requirePositiveAmount(String fieldName, BigDecimal amount, String message) {
        if (amount == null) {
            throw new InvalidArgumentException(fieldName, message);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException(fieldName, message);
        }
        return amount;
    }

    private SalePaymentStatus resolvePaymentStatus(LocalDate paymentDate) {
        return paymentDate == null ? SalePaymentStatus.OPEN : SalePaymentStatus.PAID;
    }

    private BigDecimal scaleCurrency(BigDecimal amount) {
        return amount.setScale(CURRENCY_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleMeasure(BigDecimal amount) {
        return amount.setScale(MEASURE_SCALE, RoundingMode.HALF_UP);
    }

    private CustomerResponseVO toCustomerResponse(Customer customer) {
        return new CustomerResponseVO(
                customer.getId(),
                customer.getName(),
                customer.getDocument(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getNotes(),
                customer.isActive()
        );
    }

    private AnimalSaleResponseVO toAnimalSaleResponse(AnimalSale animalSale) {
        return new AnimalSaleResponseVO(
                animalSale.getId(),
                animalSale.getGoatRegistrationNumber(),
                animalSale.getGoatName(),
                animalSale.getCustomer().getId(),
                animalSale.getCustomer().getName(),
                animalSale.getSaleDate(),
                scaleCurrency(animalSale.getAmount()),
                animalSale.getDueDate(),
                animalSale.getPaymentStatus(),
                animalSale.getPaymentDate(),
                animalSale.getNotes()
        );
    }

    private MilkSaleResponseVO toMilkSaleResponse(MilkSale milkSale) {
        return new MilkSaleResponseVO(
                milkSale.getId(),
                milkSale.getCustomer().getId(),
                milkSale.getCustomer().getName(),
                milkSale.getSaleDate(),
                scaleMeasure(milkSale.getQuantityLiters()),
                scaleCurrency(milkSale.getUnitPrice()),
                scaleCurrency(milkSale.getTotalAmount()),
                milkSale.getDueDate(),
                milkSale.getPaymentStatus(),
                milkSale.getPaymentDate(),
                milkSale.getNotes()
        );
    }
}
