package com.devmaster.goatfarm.authority.persistence.adapter;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class RefreshSessionFlywayPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void flywayMigrationShouldCreateRefreshSessionIntegrityConstraintsAndIndexes() throws SQLException {
        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load()
                .migrate();

        try (Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            assertThat(columnExists(connection, "refresh_session", "token_hash")).isTrue();
            assertThat(columnExists(connection, "refresh_session", "family_id")).isTrue();
            assertThat(hasConstraint(connection, "uk_refresh_session_token_hash", "UNIQUE")).isTrue();
            assertThat(hasConstraint(connection, "uk_refresh_session_token_id", "UNIQUE")).isTrue();
            assertThat(hasCascadeDeleteForeignKey(connection, "fk_refresh_session_user")).isTrue();
            assertThat(indexExists(connection, "idx_refresh_session_family_id")).isTrue();
            assertThat(indexExists(connection, "idx_refresh_session_user_id")).isTrue();
            assertThat(indexExists(connection, "idx_refresh_session_expires_at")).isTrue();
        }
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = "select 1 from information_schema.columns where table_name = ? and column_name = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, columnName);
            try (var result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private boolean hasConstraint(Connection connection, String constraintName, String constraintType) throws SQLException {
        String sql = "select 1 from information_schema.table_constraints where constraint_name = ? and constraint_type = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, constraintName);
            statement.setString(2, constraintType);
            try (var result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private boolean hasCascadeDeleteForeignKey(Connection connection, String constraintName) throws SQLException {
        String sql = "select delete_rule from information_schema.referential_constraints where constraint_name = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, constraintName);
            try (var result = statement.executeQuery()) {
                return result.next() && "CASCADE".equals(result.getString("delete_rule"));
            }
        }
    }

    private boolean indexExists(Connection connection, String indexName) throws SQLException {
        String sql = "select 1 from pg_indexes where schemaname = 'public' and indexname = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, indexName);
            try (var result = statement.executeQuery()) {
                return result.next();
            }
        }
    }
}
