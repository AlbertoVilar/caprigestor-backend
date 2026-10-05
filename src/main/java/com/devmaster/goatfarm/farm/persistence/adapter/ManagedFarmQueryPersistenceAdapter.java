package com.devmaster.goatfarm.farm.persistence.adapter;

import com.devmaster.goatfarm.application.pagination.PageQuery;
import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.farm.application.model.ManagedFarmRecord;
import com.devmaster.goatfarm.farm.application.ports.out.ManagedFarmQueryPort;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/** Persistence adapter for the minimal managed-farm projection. */
@Component
public class ManagedFarmQueryPersistenceAdapter implements ManagedFarmQueryPort {
    private final GoatFarmRepository repository;

    public ManagedFarmQueryPersistenceAdapter(GoatFarmRepository repository) {
        this.repository = repository;
    }

    @Override
    public PageResult<ManagedFarmRecord> findAll(PageQuery pageQuery, String query) {
        return toPage(repository.findAllManaged(query, toPageable(pageQuery)));
    }

    @Override
    public PageResult<ManagedFarmRecord> findByUserId(Long userId, PageQuery pageQuery, String query) {
        return toPage(repository.findAllManagedByUserId(userId, query, toPageable(pageQuery)));
    }

    private PageResult<ManagedFarmRecord> toPage(Page<GoatFarm> page) {
        return new PageResult<>(page.getContent().stream()
                .map(farm -> new ManagedFarmRecord(farm.getId(), farm.getName(), farm.getTod(), farm.getLogoUrl()))
                .toList(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

    private Pageable toPageable(PageQuery query) {
        java.util.List<Sort.Order> orders = query.sort().stream()
                .map(spec -> new Sort.Order(
                        spec.direction() == com.devmaster.goatfarm.application.pagination.SortDirection.ASC
                                ? Sort.Direction.ASC : Sort.Direction.DESC,
                        spec.field()))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
        if (orders.isEmpty()) {
            orders.add(Sort.Order.asc("name"));
        }
        if (orders.stream().noneMatch(order -> order.getProperty().equals("id"))) {
            orders.add(Sort.Order.asc("id"));
        }
        Sort sort = Sort.by(orders);
        return PageRequest.of(query.page(), query.size(), sort);
    }
}
