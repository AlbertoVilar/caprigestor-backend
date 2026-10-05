package com.devmaster.goatfarm.goatownership.persistence.adapter;

import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementDirection;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementItem;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementKind;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipMovementPageQuery;
import com.devmaster.goatfarm.goatownership.application.ports.out.OwnershipMovementQueryPort;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** SQL projection adapter; commercial data enriches canonical ownership rows only. */
@Component
public class OwnershipMovementQueryPersistenceAdapter implements OwnershipMovementQueryPort {
    private static final String SELECT = """
            select movement.id as movement_id,
                   movement.goat_id as movement_goat_id,
                   goat.nome as goat_name,
                   goat.num_registro as goat_registration_number,
                   movement.source_farm_id,
                   source_farm.name as source_farm_name,
                   movement.target_farm_id,
                   target_farm.name as target_farm_name,
                   movement.kind,
                   movement.state,
                   movement.reason,
                   movement.requested_at,
                   movement.accepted_at,
                   movement.effective_at,
                   movement.completed_at,
                   movement.cancelled_at,
                   movement.sale_id as movement_sale_id,
                   sale.id as joined_sale_id,
                   sale.goat_technical_id as sale_goat_id,
                   sale.farm_id as sale_source_farm_id,
                   sale.target_farm_id as sale_target_farm_id,
                   sale.sale_date,
                   sale.amount,
                   sale.payment_status,
                   sale.payment_date
              from ownership_transfer movement
              left join cabras goat on goat.id = movement.goat_id
              left join capril source_farm on source_farm.id = movement.source_farm_id
              left join capril target_farm on target_farm.id = movement.target_farm_id
              left join animal_sale sale on sale.id = movement.sale_id
             where movement.kind in ('INTERNAL_TRANSFER', 'INTERNAL_SALE')
            """;
    private static final String COUNT = """
            select count(*)
              from ownership_transfer movement
             where movement.kind in ('INTERNAL_TRANSFER', 'INTERNAL_SALE')
            """;

    private final JdbcTemplate jdbcTemplate;

    public OwnershipMovementQueryPersistenceAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PageResult<OwnershipMovementItem> findForFarm(Long farmId,
                                                         OwnershipMovementDirection direction,
                                                         OwnershipTransferStatus status,
                                                         OwnershipMovementKind kind,
                                                         OwnershipMovementPageQuery pageQuery) {
        StringBuilder where = new StringBuilder(SELECT);
        StringBuilder countWhere = new StringBuilder(COUNT);
        List<Object> parameters = new ArrayList<>();
        String farmFilter = direction == OwnershipMovementDirection.INCOMING
                ? " and movement.target_farm_id = ?"
                : " and movement.source_farm_id = ?";
        where.append(farmFilter);
        countWhere.append(farmFilter);
        parameters.add(farmId);
        if (status != null) {
            where.append(" and movement.state = ?");
            countWhere.append(" and movement.state = ?");
            parameters.add(status.name());
        }
        if (kind != null) {
            where.append(" and movement.kind = ?");
            countWhere.append(" and movement.kind = ?");
            parameters.add(kind.name());
        }

        Long totalElements = jdbcTemplate.queryForObject(countWhere.toString(), Long.class, parameters.toArray());
        String pageSql = where + " order by movement.requested_at desc, movement.id desc limit ? offset ?";
        List<Object> pageParameters = new ArrayList<>(parameters);
        pageParameters.add(pageQuery.size());
        pageParameters.add((long) pageQuery.page() * pageQuery.size());
        List<OwnershipMovementItem> content = jdbcTemplate.query(pageSql,
                (resultSet, rowNumber) -> mapRow(resultSet, direction), pageParameters.toArray());
        return new PageResult<>(content, totalElements == null ? 0 : totalElements,
                pageQuery.page(), pageQuery.size());
    }

    private static OwnershipMovementItem mapRow(ResultSet resultSet,
                                               OwnershipMovementDirection direction) throws SQLException {
        var status = OwnershipTransferStatus.valueOf(resultSet.getString("state"));
        var kind = OwnershipMovementKind.valueOf(resultSet.getString("kind"));
        Long movementSaleId = nullableLong(resultSet, "movement_sale_id");
        Long joinedSaleId = nullableLong(resultSet, "joined_sale_id");
        validateSaleLink(resultSet, kind, movementSaleId, joinedSaleId);
        return new OwnershipMovementItem(
                resultSet.getLong("movement_id"),
                nullableLong(resultSet, "movement_goat_id"),
                resultSet.getString("goat_name"),
                resultSet.getString("goat_registration_number"),
                nullableLong(resultSet, "source_farm_id"),
                resultSet.getString("source_farm_name"),
                resultSet.getLong("target_farm_id"),
                resultSet.getString("target_farm_name"),
                kind,
                status,
                direction,
                resultSet.getString("reason"),
                instant(resultSet, "requested_at"),
                instant(resultSet, "accepted_at"),
                instant(resultSet, "effective_at"),
                instant(resultSet, "completed_at"),
                instant(resultSet, "cancelled_at"),
                status == OwnershipTransferStatus.COMPLETED,
                joinedSaleId,
                resultSet.getDate("sale_date") == null ? null : resultSet.getDate("sale_date").toLocalDate(),
                resultSet.getBigDecimal("amount"),
                resultSet.getString("payment_status"),
                resultSet.getDate("payment_date") == null ? null : resultSet.getDate("payment_date").toLocalDate());
    }

    private static void validateSaleLink(ResultSet resultSet,
                                         OwnershipMovementKind kind,
                                         Long movementSaleId,
                                         Long joinedSaleId) throws SQLException {
        if (kind == OwnershipMovementKind.INTERNAL_TRANSFER) {
            if (movementSaleId != null) {
                throw new IllegalStateException("Ownership transfer unexpectedly references a sale");
            }
            return;
        }

        boolean validSaleLink = movementSaleId != null
                && Objects.equals(movementSaleId, joinedSaleId)
                && Objects.equals(nullableLong(resultSet, "movement_goat_id"), nullableLong(resultSet, "sale_goat_id"))
                && Objects.equals(nullableLong(resultSet, "source_farm_id"), nullableLong(resultSet, "sale_source_farm_id"))
                && Objects.equals(nullableLong(resultSet, "target_farm_id"), nullableLong(resultSet, "sale_target_farm_id"));
        if (!validSaleLink) {
            throw new IllegalStateException("Ownership sale link violates movement identity integrity");
        }
    }

    private static Long nullableLong(ResultSet resultSet, String column) throws SQLException {
        long value = resultSet.getLong(column);
        return resultSet.wasNull() ? null : value;
    }

    private static java.time.Instant instant(ResultSet resultSet, String column) throws SQLException {
        Timestamp value = resultSet.getTimestamp(column);
        return value == null ? null : value.toInstant();
    }
}
