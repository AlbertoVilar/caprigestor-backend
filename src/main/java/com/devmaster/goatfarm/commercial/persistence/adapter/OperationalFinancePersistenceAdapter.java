package com.devmaster.goatfarm.commercial.persistence.adapter;

import com.devmaster.goatfarm.commercial.application.model.OperationalExpenseCommand;
import com.devmaster.goatfarm.commercial.application.model.OperationalExpenseRecord;
import com.devmaster.goatfarm.commercial.application.ports.out.OperationalFinancePersistencePort;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import com.devmaster.goatfarm.commercial.persistence.entity.OperationalExpense;
import com.devmaster.goatfarm.commercial.persistence.repository.AnimalSaleRepository;
import com.devmaster.goatfarm.commercial.persistence.repository.MilkSaleRepository;
import com.devmaster.goatfarm.commercial.persistence.repository.OperationalExpenseRepository;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class OperationalFinancePersistenceAdapter implements OperationalFinancePersistencePort {

    private final OperationalExpenseRepository operationalExpenseRepository;
    private final AnimalSaleRepository animalSaleRepository;
    private final MilkSaleRepository milkSaleRepository;
    private final GoatFarmRepository goatFarmRepository;

    public OperationalFinancePersistenceAdapter(
            OperationalExpenseRepository operationalExpenseRepository,
            AnimalSaleRepository animalSaleRepository,
            MilkSaleRepository milkSaleRepository,
            GoatFarmRepository goatFarmRepository
    ) {
        this.operationalExpenseRepository = operationalExpenseRepository;
        this.animalSaleRepository = animalSaleRepository;
        this.milkSaleRepository = milkSaleRepository;
        this.goatFarmRepository = goatFarmRepository;
    }

    @Override
    public OperationalExpenseRecord saveOperationalExpense(OperationalExpenseCommand command) {
        OperationalExpense entity = command.id() == null
                ? new OperationalExpense()
                : operationalExpenseRepository.findById(command.id()).orElseGet(OperationalExpense::new);
        entity.setFarm(goatFarmRepository.getReferenceById(command.farmId()));
        entity.setCategory(command.category());
        entity.setDescription(command.description());
        entity.setAmount(command.amount());
        entity.setExpenseDate(command.expenseDate());
        entity.setNotes(command.notes());
        return toRecord(operationalExpenseRepository.save(entity));
    }

    @Override
    public List<OperationalExpenseRecord> findOperationalExpensesByFarmId(Long farmId) {
        return operationalExpenseRepository.findByFarm_IdOrderByExpenseDateDescIdDesc(farmId).stream()
                .map(this::toRecord)
                .toList();
    }

    @Override
    public BigDecimal sumOperationalExpensesByFarmIdAndPeriod(Long farmId, LocalDate fromDate, LocalDate toDate) {
        return operationalExpenseRepository.sumAmountByFarmIdAndExpenseDateBetween(farmId, fromDate, toDate);
    }

    @Override
    public BigDecimal sumPaidAnimalSalesByFarmIdAndPeriod(Long farmId, LocalDate fromDate, LocalDate toDate) {
        return animalSaleRepository.sumPaidAmountByFarmIdAndPaymentDateBetween(farmId, SalePaymentStatus.PAID, fromDate, toDate);
    }

    @Override
    public BigDecimal sumPaidMilkSalesByFarmIdAndPeriod(Long farmId, LocalDate fromDate, LocalDate toDate) {
        return milkSaleRepository.sumPaidAmountByFarmIdAndPaymentDateBetween(farmId, SalePaymentStatus.PAID, fromDate, toDate);
    }

    private OperationalExpenseRecord toRecord(OperationalExpense entity) {
        return new OperationalExpenseRecord(
                entity.getId(),
                entity.getFarm() == null ? null : entity.getFarm().getId(),
                entity.getCategory(),
                entity.getDescription(),
                entity.getAmount(),
                entity.getExpenseDate(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
