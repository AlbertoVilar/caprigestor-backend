package com.devmaster.goatfarm.inventory.persistence.adapter;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class InventoryFlywayPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void flywayMigrationShouldCreateInventoryLedgerConstraintsAndIndexes() throws SQLException {
        Flyway flyway = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();

        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        )) {
            assertThat(hasUniqueConstraint(connection, "inventory_idempotency", "uk_inventory_idempotency_farm_key"))
                    .isTrue();
            assertThat(hasUniqueConstraint(connection, "inventory_balance", "uk_inventory_balance_farm_item_lot"))
                    .isTrue();
            assertThat(hasPartialNullLotUniqueIndex(connection, "ux_inventory_balance_farm_item_null_lot"))
                    .isTrue();
        }
    }

    private boolean hasUniqueConstraint(Connection connection, String tableName, String constraintName)
            throws SQLException {
        String sql = """
                select 1
                from information_schema.table_constraints tc
                where tc.table_name = ?
                  and tc.constraint_name = ?
                  and tc.constraint_type = 'UNIQUE'
                """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, constraintName);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean hasPartialNullLotUniqueIndex(Connection connection, String indexName) throws SQLException {
        String sql = """
                select indexdef
                from pg_indexes
                where schemaname = 'public'
                  and indexname = ?
                """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, indexName);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                String indexDef = rs.getString("indexdef");
                return indexDef != null
                        && indexDef.toLowerCase().contains("unique")
                        && indexDef.toLowerCase().contains("where")
                        && indexDef.toLowerCase().contains("lot_id is null");
            }
        }
    }
}
