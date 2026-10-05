package com.devmaster.goatfarm.integration;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
class EventRecordingFarmProvenanceFlywayPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Test
    void migrationCreatesNullableRecordingFarmProvenanceWithForeignKeyAndIndex() throws SQLException {
        flyway().migrate();

        try (Connection connection = openConnection()) {
            seedUsersFarmsAndGoat(connection);
            long goatId = queryLong(connection,
                    "select id from cabras where num_registro = 'EVENT-GOAT'");

            execute(connection, """
                    insert into eventos
                        (id, goat_registration_number, goat_technical_id, tipo_evento, data, descricao)
                    values (1, 'EVENT-GOAT', %d, 'PESAGEM', date '2026-09-10', 'legacy')
                    """.formatted(goatId));
            execute(connection, """
                    insert into eventos
                        (id, goat_registration_number, goat_technical_id, recording_farm_id, tipo_evento, data, descricao)
                    values (2, 'EVENT-GOAT', %d, 101, 'PESAGEM', date '2026-09-10', 'provenance')
                    """.formatted(goatId));

            assertThat(queryString(connection, """
                    select is_nullable from information_schema.columns
                    where table_schema = 'public' and table_name = 'eventos'
                      and column_name = 'recording_farm_id'
                    """)).isEqualTo("YES");
            assertThat(queryLong(connection,
                    "select count(*) from eventos where id = 1 and recording_farm_id is null")).isEqualTo(1L);
            assertThat(queryLong(connection,
                    "select recording_farm_id from eventos where id = 2")).isEqualTo(101L);
            assertThat(foreignKeyExists(connection, "fk_eventos_recording_farm")).isTrue();
            assertThat(indexExists(connection, "idx_eventos_recording_farm_goat_technical")).isTrue();

            assertThatThrownBy(() -> execute(connection, """
                    insert into eventos
                        (id, goat_registration_number, goat_technical_id, recording_farm_id, tipo_evento, data, descricao)
                    values (3, 'EVENT-GOAT', %d, 999999, 'PESAGEM', date '2026-09-10', 'invalid farm')
                    """.formatted(goatId)))
                    .isInstanceOf(SQLException.class);
        }
    }

    private Flyway flyway() {
        return Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    private void seedUsersFarmsAndGoat(Connection connection) throws SQLException {
        execute(connection, """
                insert into users (id, name, email, password, cpf)
                values (1, 'Event User', 'event@example.test', 'password', '00000000001')
                """);
        execute(connection, """
                insert into capril (id, name, user_id, version)
                values (101, 'Event Farm', 1, 0)
                """);
        execute(connection, """
                insert into cabras
                    (num_registro, nome, sexo, data_nascimento, status, usuario_id, capril_id)
                values ('EVENT-GOAT', 'Event Goat', 'FEMEA', date '2020-01-01', 'ATIVO', 1, 101)
                """);
    }

    private boolean foreignKeyExists(Connection connection, String constraintName) throws SQLException {
        try (var statement = connection.prepareStatement("""
                select 1 from information_schema.table_constraints
                where table_schema = 'public' and constraint_name = ? and constraint_type = 'FOREIGN KEY'
                """)) {
            statement.setString(1, constraintName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean indexExists(Connection connection, String indexName) throws SQLException {
        try (var statement = connection.prepareStatement("""
                select 1 from pg_indexes
                where schemaname = 'public' and indexname = ?
                """)) {
            statement.setString(1, indexName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private long queryLong(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            assertThat(resultSet.next()).isTrue();
            return resultSet.getLong(1);
        }
    }

    private String queryString(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement(); ResultSet resultSet = statement.executeQuery(sql)) {
            assertThat(resultSet.next()).isTrue();
            return resultSet.getString(1);
        }
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
