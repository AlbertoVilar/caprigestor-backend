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
class GoatTechnicalReferencesFlywayPostgresIntegrationTest {

    @Container
    final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            System.getProperty("caprigestor.test.postgres.image", "postgres:16-alpine"));

    @Test
    void cleanInstallCreatesAdditiveTechnicalReferenceGraph() throws SQLException {
        flyway().migrate();

        try (Connection connection = openConnection()) {
            assertThat(queryLong(connection, "select count(*) from pg_constraint where contype = 'f' and confrelid = 'public.cabras'::regclass and conname in (" + oldGoatConstraintNames() + ")"))
                    // V43 removes every direct structural FK to the mutable
                    // RG. All goat-dependent structural references target id.
                    .isZero();
            assertThat(queryLong(connection, "select count(*) from pg_constraint where contype = 'f' and confrelid = 'public.cabras'::regclass and conname like '%technical%'"))
                    .isEqualTo(7L);
            assertThat(queryLong(connection, "select count(*) from pg_constraint where contype = 'f' and confrelid = 'public.cabras'::regclass and conname in ('fk_cabras_pai_goat_id','fk_cabras_mae_goat_id')"))
                    .isEqualTo(2L);
            assertThat(queryLong(connection, "select count(*) from pg_constraint where contype = 'f' and conrelid = 'public.milk_production'::regclass and conname = 'fk_milk_production_farm_goat_technical_lactation'"))
                    .isEqualTo(1L);
            assertThat(hasConstraint(connection, "uk_cabras_farm_id", "UNIQUE")).isTrue();
            assertThat(hasConstraint(connection, "uk_lactation_farm_goat_technical_id", "UNIQUE")).isTrue();
            assertThat(hasConstraint(connection, "fk_goat_registration_history_goat", "FOREIGN KEY")).isTrue();
            assertThat(hasConstraint(connection, "fk_goat_registration_history_farm_goat", "FOREIGN KEY")).isTrue();
            assertThat(queryLong(connection, "select count(*) from information_schema.tables where table_schema = 'public' and table_name = 'goat_registration_history'")).isEqualTo(1L);
            assertNullable(connection, "cabras", "pai_goat_id");
            assertNullable(connection, "cabras", "mae_goat_id");
            assertNotNullable(connection, "eventos", "goat_technical_id");
            assertNotNullable(connection, "pregnancy", "goat_technical_id");
            assertNotNullable(connection, "reproductive_event", "goat_technical_id");
            assertNotNullable(connection, "health_events", "goat_technical_id");
            assertNotNullable(connection, "lactation", "goat_technical_id");
            assertNotNullable(connection, "milk_production", "goat_technical_id");
            assertNotNullable(connection, "animal_sale", "goat_technical_id");
            assertNullable(connection, "operational_audit_entry", "goat_technical_id");
        }
    }

    @Test
    void upgradeFromV39BackfillsAllTechnicalRelationshipsWithoutChangingRgGraph() throws SQLException {
        flyway("39").migrate();

        try (Connection connection = openConnection()) {
            seedRepresentativeData(connection);
        }

        flyway().migrate();

        try (Connection connection = openConnection()) {
            long fatherId = queryLong(connection, "select id from cabras where num_registro = 'G-FATHER'");
            long motherId = queryLong(connection, "select id from cabras where num_registro = 'G-MOTHER'");
            long goatId = queryLong(connection, "select id from cabras where num_registro = 'G-101'");

            assertThat(queryLong(connection, "select pai_goat_id from cabras where num_registro = 'G-CHILD'"))
                    .isEqualTo(fatherId);
            assertThat(queryLong(connection, "select mae_goat_id from cabras where num_registro = 'G-CHILD'"))
                    .isEqualTo(motherId);
            assertThat(queryLong(connection, "select goat_technical_id from eventos where id = 1"))
                    .isEqualTo(goatId);
            assertThat(queryLong(connection, "select goat_technical_id from pregnancy where id = 10"))
                    .isEqualTo(goatId);
            assertThat(queryLong(connection, "select goat_technical_id from reproductive_event where id = 20"))
                    .isEqualTo(goatId);
            assertThat(queryLong(connection, "select goat_technical_id from health_events where id = 30"))
                    .isEqualTo(goatId);
            assertThat(queryLong(connection, "select goat_technical_id from lactation where id = 40"))
                    .isEqualTo(goatId);
            assertThat(queryLong(connection, "select goat_technical_id from milk_production where id = 50"))
                    .isEqualTo(goatId);
            assertThat(queryLong(connection, "select goat_technical_id from animal_sale where id = 70"))
                    .isEqualTo(goatId);
            assertThat(queryLong(connection, "select goat_technical_id from operational_audit_entry where id = 80"))
                    .isEqualTo(goatId);
            assertThat(queryLong(connection, "select count(*) from operational_audit_entry where id = 81 and goat_technical_id is null"))
                    .isEqualTo(1L);

            assertThat(queryString(connection, "select goat_registration_number from animal_sale where id = 70"))
                    .isEqualTo("G-101");
            assertThat(queryString(connection, "select goat_registration_number from operational_audit_entry where id = 80"))
                    .isEqualTo("G-101");
            assertThat(queryLong(connection, "select count(*) from pg_constraint where contype = 'f' and confrelid = 'public.cabras'::regclass and conname in (" + oldGoatConstraintNames() + ")"))
                    .isZero();
            assertThat(queryLong(connection, "select count(*) from pg_constraint where contype = 'f' and confrelid = 'public.cabras'::regclass and conname like '%technical%'"))
                    .isEqualTo(7L);
        }
    }

    @Test
    void oldStyleWritesAreRejectedAfterTechnicalWave() throws SQLException {
        flyway().migrate();

        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "G-OLD", "Legacy Goat", "FEMEA", 101);
            assertThatThrownBy(() -> execute(connection, "insert into eventos (id, goat_registration_number, tipo_evento, data, descricao) values (101, 'G-OLD', 'TEST', date '2026-01-01', 'legacy')"))
                    .isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> execute(connection, "insert into pregnancy (id, farm_id, goat_id, status, created_at, updated_at) values (101, 101, 'G-OLD', 'ACTIVE', now(), now())"))
                    .isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> execute(connection, "insert into operational_audit_entry (id, farm_id, goat_registration_number, action_type, actor_user_id, actor_name, actor_email, description) values (101, 101, 'G-OLD', 'TEST', 1, 'Legacy', 'legacy@example.test', 'legacy')"))
                    .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void technicalReferencesPreserveFarmAndDeleteSemantics() throws SQLException {
        flyway().migrate();

        try (Connection connection = openConnection()) {
            seedUsersAndFarms(connection);
            insertGoat(connection, "G-101", "Farm 101 Goat", "FEMEA", 101);
            insertGoat(connection, "G-102", "Farm 102 Goat", "FEMEA", 102);
            insertGoat(connection, "G-PARENT", "Parent", "MACHO", 102);
            insertGoat(connection, "G-CHILD", "Child", "FEMEA", 101);
            execute(connection, "update cabras set pai_num_registro = 'G-PARENT', pai_goat_id = (select id from cabras where num_registro = 'G-PARENT') where num_registro = 'G-CHILD'");

            assertThatThrownBy(() -> execute(connection, "insert into pregnancy (id, farm_id, goat_id, goat_technical_id, status, created_at, updated_at) values (201, 101, 'G-101', (select id from cabras where num_registro = 'G-102'), 'ACTIVE', now(), now())"))
                    .isInstanceOf(SQLException.class);
            assertThatThrownBy(() -> execute(connection, "insert into health_events (id, farm_id, goat_id, goat_technical_id, type, status, title, scheduled_date) values (201, 101, 'G-101', 999999, 'VACCINE', 'SCHEDULED', 'Invalid', date '2026-01-01')"))
                    .isInstanceOf(SQLException.class);

            execute(connection, "insert into eventos (id, goat_registration_number, goat_technical_id, tipo_evento, data, descricao) select 201, 'G-101', id, 'TEST', date '2026-01-01', 'cascade' from cabras where num_registro = 'G-101'");
            execute(connection, "delete from cabras where num_registro = 'G-101'");
            assertThat(queryLong(connection, "select count(*) from eventos where id = 201")).isZero();

            execute(connection, "delete from cabras where num_registro = 'G-PARENT'");
            // The technical local link is nulled by ON DELETE SET NULL, while
            // pai_num_registro remains an immutable historical snapshot.
            assertThat(queryLong(connection, "select count(*) from cabras where num_registro = 'G-CHILD' and pai_num_registro = 'G-PARENT' and pai_goat_id is null")).isEqualTo(1L);

            assertThatThrownBy(() -> execute(connection, "insert into health_events (id, farm_id, goat_id, type, status, title, scheduled_date) values (202, 101, 'G-NOPE', 'VACCINE', 'SCHEDULED', 'Invalid', date '2026-01-01')"))
                    .isInstanceOf(SQLException.class);
        }
    }

    private void seedRepresentativeData(Connection connection) throws SQLException {
        seedUsersAndFarms(connection);
        insertGoat(connection, "G-FATHER", "Father", "MACHO", 102);
        insertGoat(connection, "G-MOTHER", "Mother", "FEMEA", 101);
        execute(connection, "insert into cabras (num_registro, nome, sexo, data_nascimento, status, usuario_id, capril_id, pai_num_registro, mae_num_registro) values ('G-CHILD', 'Child', 'FEMEA', date '2022-01-01', 'ATIVO', 1, 101, 'G-FATHER', 'G-MOTHER')");
        insertGoat(connection, "G-101", "Farm 101 Goat", "FEMEA", 101);
        insertGoat(connection, "G-102", "Farm 102 Goat", "FEMEA", 102);
        execute(connection, "insert into eventos (id, goat_registration_number, tipo_evento, data, descricao) values (1, 'G-101', 'PESAGEM', date '2026-01-01', 'event')");
        execute(connection, "insert into pregnancy (id, farm_id, goat_id, status, created_at, updated_at) values (10, 101, 'G-101', 'ACTIVE', now(), now())");
        execute(connection, "insert into reproductive_event (id, farm_id, goat_id, event_type, event_date, created_at, updated_at) values (20, 101, 'G-101', 'COVERAGE', date '2026-01-01', now(), now())");
        execute(connection, "insert into health_events (id, farm_id, goat_id, type, status, title, scheduled_date) values (30, 101, 'G-101', 'VACCINE', 'SCHEDULED', 'Health', date '2026-01-01')");
        execute(connection, "insert into lactation (id, farm_id, goat_id, status, start_date) values (40, 101, 'G-101', 'ACTIVE', date '2026-01-01')");
        execute(connection, "insert into milk_production (id, farm_id, goat_id, lactation_id, date, shift, volume_liters) values (50, 101, 'G-101', 40, date '2026-01-01', 'MORNING', 1)");
        execute(connection, "insert into commercial_customer (id, farm_id, name) values (60, 101, 'Customer')");
        execute(connection, "insert into animal_sale (id, farm_id, customer_id, goat_registration_number, goat_name, sale_date, amount, due_date, payment_status) values (70, 101, 60, 'G-101', 'Farm 101 Goat', date '2026-01-01', 1, date '2026-01-01', 'PENDING')");
        execute(connection, "insert into operational_audit_entry (id, farm_id, goat_registration_number, action_type, actor_user_id, actor_name, actor_email, description) values (80, 101, 'G-101', 'TEST', 1, 'Test User', 'test@example.com', 'audit'), (81, 101, null, 'TEST', 1, 'Test User', 'test@example.com', 'audit without goat')");
    }

    private void seedUsersAndFarms(Connection connection) throws SQLException {
        execute(connection, "insert into users (id, name, email, password, cpf) values (1, 'Test User', 'test@example.com', 'password', '00000000000')");
        execute(connection, "insert into capril (id, name, user_id, version) values (101, 'Farm 101', 1, 0), (102, 'Farm 102', 1, 0)");
    }

    private void insertGoat(Connection connection, String registrationNumber, String name, String gender, int farmId) throws SQLException {
        execute(connection, "insert into cabras (num_registro, nome, sexo, data_nascimento, status, usuario_id, capril_id) values ('%s', '%s', '%s', date '2020-01-01', 'ATIVO', 1, %d)".formatted(registrationNumber, name, gender, farmId));
    }

    private Flyway flyway() {
        return flyway(null);
    }

    private Flyway flyway(String target) {
        var configuration = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .baselineOnMigrate(true);
        if (target != null) {
            configuration.target(target);
        }
        return configuration.load();
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }

    private void assertNullable(Connection connection, String table, String column) throws SQLException {
        String sql = "select is_nullable from information_schema.columns where table_schema = 'public' and table_name = ? and column_name = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString(1)).isEqualTo("YES");
            }
        }
    }

    private void assertNotNullable(Connection connection, String table, String column) throws SQLException {
        String sql = "select is_nullable from information_schema.columns where table_schema = 'public' and table_name = ? and column_name = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, table);
            statement.setString(2, column);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString(1)).isEqualTo("NO");
            }
        }
    }

    private boolean hasConstraint(Connection connection, String name, String type) throws SQLException {
        String sql = "select 1 from information_schema.table_constraints where table_schema = 'public' and constraint_name = ? and constraint_type = ?";
        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, type);
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

    private String oldGoatConstraintNames() {
        return "'animal_sale_goat_registration_number_fkey','cabras_mae_num_registro_fkey','cabras_pai_num_registro_fkey','eventos_goat_registration_number_fkey','fk_animal_sale_farm_goat','fk_health_events_farm_goat','fk_health_events_goat','fk_lactation_farm_goat','fk_operational_audit_entry_farm_goat','fk_pregnancy_farm_goat','fk_reproductive_event_farm_goat'";
    }
}
