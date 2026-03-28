package com.devmaster.goatfarm.inventory.persistence.adapter;

import com.devmaster.goatfarm.inventory.application.ports.out.InventoryMovementQueryPort;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementFilterVO;
import com.devmaster.goatfarm.inventory.business.bo.InventoryMovementHistoryResponseVO;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryMovementQueryRow;
import com.devmaster.goatfarm.inventory.persistence.repository.InventoryMovementRepository;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InventoryMovementQueryPersistenceAdapter implements InventoryMovementQueryPort {

    private final InventoryMovementRepository movementRepository;
    private final EntityManager entityManager;

    public InventoryMovementQueryPersistenceAdapter(
            InventoryMovementRepository movementRepository,
            EntityManager entityManager
    ) {
        this.movementRepository = movementRepository;
        this.entityManager = entityManager;
    }

    @Override
    public Page<InventoryMovementHistoryResponseVO> listMovements(InventoryMovementFilterVO filter) {
        StringBuilder select = new StringBuilder("""
                select new com.devmaster.goatfarm.inventory.persistence.repository.InventoryMovementQueryRow(
                    m.id,
                    m.type,
                    m.adjustDirection,
                    m.quantity,
                    m.itemId,
                    i.name,
                    m.lotId,
                    m.movementDate,
                    m.reason,
                    m.resultingBalance,
                    m.unitCost,
                    m.totalCost,
                    m.purchaseDate,
                    m.supplierName,
                    m.createdAt
                )
                from InventoryMovementEntity m
                join InventoryItemEntity i
                  on i.id = m.itemId
                 and i.farmId = m.farmId
                where m.farmId = :farmId
                """);
        StringBuilder count = new StringBuilder("""
                select count(m)
                from InventoryMovementEntity m
                join InventoryItemEntity i
                  on i.id = m.itemId
                 and i.farmId = m.farmId
                where m.farmId = :farmId
                """);

        Map<String, Object> params = new HashMap<>();
        params.put("farmId", filter.farmId());

        appendFilters(select, count, params, filter);

        select.append(" order by m.movementDate desc, m.createdAt desc");

        Pageable pageable = filter.pageable();
        var dataQuery = entityManager.createQuery(select.toString(), InventoryMovementQueryRow.class);
        var countQuery = entityManager.createQuery(count.toString(), Long.class);

        params.forEach((key, value) -> {
            dataQuery.setParameter(key, value);
            countQuery.setParameter(key, value);
        });

        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        List<InventoryMovementHistoryResponseVO> content = dataQuery.getResultList().stream().map(row -> new InventoryMovementHistoryResponseVO(
                row.movementId(),
                row.type(),
                row.adjustDirection(),
                row.quantity(),
                row.itemId(),
                row.itemName(),
                row.lotId(),
                row.movementDate(),
                row.reason(),
                row.resultingBalance(),
                row.unitCost(),
                row.totalCost(),
                row.purchaseDate(),
                row.supplierName(),
                row.createdAt()
        )).toList();

        return new PageImpl<>(content, pageable, countQuery.getSingleResult());
    }

    private void appendFilters(
            StringBuilder select,
            StringBuilder count,
            Map<String, Object> params,
            InventoryMovementFilterVO filter
    ) {
        appendFilter(select, count, params, "itemId", filter.itemId(), "m.itemId = :itemId");
        appendFilter(select, count, params, "lotId", filter.lotId(), "m.lotId = :lotId");
        appendFilter(select, count, params, "type", filter.type(), "m.type = :type");
        appendFilter(select, count, params, "fromDate", filter.fromDate(), "m.movementDate >= :fromDate");
        appendFilter(select, count, params, "toDate", filter.toDate(), "m.movementDate <= :toDate");
    }

    private void appendFilter(
            StringBuilder select,
            StringBuilder count,
            Map<String, Object> params,
            String paramName,
            Object value,
            String clause
    ) {
        if (value == null) {
            return;
        }

        select.append(" and ").append(clause);
        count.append(" and ").append(clause);
        params.put(paramName, value);
    }
}
