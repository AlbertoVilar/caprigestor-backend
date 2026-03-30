package com.devmaster.goatfarm.milk.persistence.adapter;

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
class FarmMilkProductionFlywayPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void flywayMigrationShouldCreateFarmMilkProductionConstraints() throws SQLException {
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
            assertThat(hasUniqueConstraint(connection, "farm_milk_production", "uk_farm_milk_production_farm_date"))
                    .isTrue();
            assertThat(hasCheckConstraint(connection, "farm_milk_production", "ck_farm_milk_production_total_non_negative"))
                    .isTrue();
            assertThat(hasCheckConstraint(connection, "farm_milk_production", "ck_farm_milk_production_withdrawal_non_negative"))
                    .isTrue();
            assertThat(hasCheckConstraint(connection, "farm_milk_production", "ck_farm_milk_production_marketable_non_negative"))
                    .isTrue();
            assertThat(hasCheckConstraint(connection, "farm_milk_production", "ck_farm_milk_production_volume_balance"))
                    .isTrue();
            assertThat(hasIndex(connection, "idx_farm_milk_production_farm_date")).isTrue();
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

    private boolean hasCheckConstraint(Connection connection, String tableName, String constraintName)
            throws SQLException {
        String sql = """
                select 1
                from information_schema.table_constraints tc
                where tc.table_name = ?
                  and tc.constraint_name = ?
                  and tc.constraint_type = 'CHECK'
                """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, constraintName);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean hasIndex(Connection connection, String indexName) throws SQLException {
        String sql = """
                select 1
                from pg_indexes
                where schemaname = 'public'
                  and indexname = ?
                """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, indexName);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next();
            }
        }
    }
}
