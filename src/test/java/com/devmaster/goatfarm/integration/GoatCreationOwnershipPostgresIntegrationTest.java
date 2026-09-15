package com.devmaster.goatfarm.integration;

import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.goat.application.model.GoatCreationOrigin;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.bo.GoatExitRequestVO;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatExitType;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatOwnershipQueryPort;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class GoatCreationOwnershipPostgresIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired GoatManagementUseCase goatManagementUseCase;
    @Autowired GoatOwnershipQueryPort ownershipQuery;

    @MockBean FarmAuthorizationUseCase farmAuthorization;
    @MockBean CurrentPrincipalQueryUseCase currentPrincipalQuery;

    @Test
    void manualRuntimeCreationPersistsOneCanonicalOpenOwnershipPeriod() {
        long farmId = createFarm();
        doNothing().when(farmAuthorization).verifyFarmManagement(farmId);
        when(farmAuthorization.canAdministerFarm(farmId)).thenReturn(true);
        when(currentPrincipalQuery.requireCurrent()).thenReturn(
                new AuthenticatedPrincipal(1L, "creation@example.com", "Creation", Set.of("ROLE_OPERATOR")));

        GoatRequestVO request = GoatRequestVO.builder()
                .registrationNumber("1234500001")
                .tod("12345").toe("00001")
                .name("Manual Integration Goat")
                .gender(Gender.FEMEA).breed(GoatBreed.SAANEN).color("Branca")
                .birthDate(java.time.LocalDate.of(2025, 1, 1))
                .status(GoatStatus.ATIVO).category(Category.PA).farmId(farmId).build();

        GoatResponseVO created = goatManagementUseCase.createGoat(farmId, request, GoatCreationOrigin.MANUAL);

        assertThat(created.getTechnicalId()).isPositive();
        Integer periodCount = jdbcTemplate.queryForObject(
                "select count(*) from goat_ownership_period where goat_id = ?", Integer.class, created.getTechnicalId());
        assertThat(periodCount).isEqualTo(1);
        Long ownerFarm = jdbcTemplate.queryForObject(
                "select farm_id from goat_ownership_period where goat_id = ? and ended_at is null",
                Long.class, created.getTechnicalId());
        assertThat(ownerFarm).isEqualTo(farmId);
        String entryType = jdbcTemplate.queryForObject(
                "select entry_type from goat_ownership_period where goat_id = ?", String.class, created.getTechnicalId());
        assertThat(entryType).isEqualTo(OwnershipEntryType.MANUAL_IMPORT.name());
        assertThat(jdbcTemplate.queryForObject(
                "select source from goat_ownership_period where goat_id = ?", String.class, created.getTechnicalId()))
                .isEqualTo("GOAT_CREATE:MANUAL");
        assertThat(ownershipQuery.findCurrentOwnerFarmId(com.devmaster.goatfarm.goat.domain.GoatId.of(created.getTechnicalId())))
                .contains(farmId);

        goatManagementUseCase.exitGoat(farmId, request.getRegistrationNumber(),
                GoatExitRequestVO.builder().exitType(GoatExitType.MORTE)
                        .exitDate(java.time.LocalDate.of(2026, 9, 14)).build());

        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from goat_ownership_period where goat_id = ?", Integer.class,
                created.getTechnicalId())).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from goat_ownership_period where goat_id = ? and ended_at is not null and exit_type = ?",
                Integer.class, created.getTechnicalId(), "DEATH")).isEqualTo(1);
    }

    private long createFarm() {
        long userId = jdbcTemplate.queryForObject(
                "insert into users (name, email, password, cpf) values ('Creation User', ?, 'password', ?) returning id",
                Long.class, "creation-" + System.nanoTime() + "@example.com", String.valueOf(Math.abs(System.nanoTime())).substring(0, 11));
        return jdbcTemplate.queryForObject("insert into capril (name, user_id, tod) values (?, ?, '12345') returning id",
                Long.class, "Creation Farm " + System.nanoTime(), userId);
    }
}
