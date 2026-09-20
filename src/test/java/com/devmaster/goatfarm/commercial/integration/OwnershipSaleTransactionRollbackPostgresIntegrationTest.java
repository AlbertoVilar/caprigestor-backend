package com.devmaster.goatfarm.commercial.integration;

import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.commercial.api.dto.SalePaymentRequestDTO;
import com.devmaster.goatfarm.commercial.application.ports.in.OwnershipSaleUseCase;
import com.devmaster.goatfarm.commercial.business.bo.OwnershipSaleRequestVO;
import com.devmaster.goatfarm.commercial.persistence.entity.Customer;
import com.devmaster.goatfarm.commercial.persistence.repository.AnimalSaleRepository;
import com.devmaster.goatfarm.commercial.persistence.repository.CustomerRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import com.devmaster.goatfarm.goatownership.application.ports.out.GoatCurrentOwnerProjectionPort;
import com.devmaster.goatfarm.goatownership.persistence.entity.GoatOwnershipPeriodEntity;
import com.devmaster.goatfarm.goatownership.persistence.entity.OwnershipTransferEntity;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatOwnershipPeriodRepository;
import com.devmaster.goatfarm.goatownership.persistence.repository.OwnershipTransferRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Proves ownership sale writes roll back together after the projection write point. */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Import(OwnershipSaleTransactionRollbackPostgresIntegrationTest.FailureConfiguration.class)
class OwnershipSaleTransactionRollbackPostgresIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(
            System.getProperty("caprigestor.test.postgres.image", "postgres:16-alpine"));

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired private OwnershipSaleUseCase ownershipSales;
    @Autowired private UserRepository users;
    @Autowired private GoatFarmRepository farms;
    @Autowired private GoatRepository goats;
    @Autowired private CustomerRepository customers;
    @Autowired private AnimalSaleRepository animalSales;
    @Autowired private OwnershipTransferRepository transfers;
    @Autowired private GoatOwnershipPeriodRepository periods;

    @Test
    void projectionFailureRollsBackSalePaymentPeriodsProjectionAndTransfer() {
        User seller = user("sale-rollback-seller");
        User buyer = user("sale-rollback-buyer");
        GoatFarm sourceFarm = farm("Sale rollback source", "SRS01", seller);
        GoatFarm targetFarm = farm("Sale rollback target", "SRT01", buyer);
        GoatEntity goat = new GoatEntity();
        goat.setRegistrationNumber("SRS010001");
        goat.setName("Rollback sale goat");
        goat.setGender(Gender.FEMEA);
        goat.setBirthDate(LocalDate.of(2024, 1, 1));
        goat.setStatus(GoatStatus.ATIVO);
        goat.setTod("SRS01");
        goat.setToe("0001");
        goat.setFarm(sourceFarm);
        goat.setUser(seller);
        goat = goats.saveAndFlush(goat);
        Customer customer = customers.saveAndFlush(Customer.builder().farm(sourceFarm).name("Rollback buyer").active(true).build());
        GoatOwnershipPeriodEntity sourcePeriod = new GoatOwnershipPeriodEntity();
        sourcePeriod.setGoatId(goat.getTechnicalId());
        sourcePeriod.setFarmId(sourceFarm.getId());
        sourcePeriod.setStartedAt(Instant.parse("2024-01-01T00:00:00Z"));
        sourcePeriod.setEntryType(com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType.MANUAL_IMPORT);
        sourcePeriod.setSource("TEST_FIXTURE");
        periods.saveAndFlush(sourcePeriod);

        FailureConfiguration.PRINCIPAL_ID.set(seller.getId());
        var request = new OwnershipSaleRequestVO("technical-" + goat.getTechnicalId(), customer.getId(), targetFarm.getId(),
                LocalDate.of(2026, 9, 19), new BigDecimal("100.00"), LocalDate.of(2026, 9, 25), "rollback", "rollback-sale-1");
        var pending = ownershipSales.requestOwnershipSale(sourceFarm.getId(), request);
        assertThat(pending.ownershipTransferStatus()).isEqualTo(com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.REQUESTED);

        assertThatThrownBy(() -> ownershipSales.acceptOwnershipSale(sourceFarm.getId(), pending.saleId(),
                new com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO(LocalDate.of(2026, 9, 19))))
                .isInstanceOf(RuntimeException.class);

        var sale = animalSales.findById(pending.saleId()).orElseThrow();
        assertThat(sale.getPaymentStatus()).isEqualTo(com.devmaster.goatfarm.commercial.enums.SalePaymentStatus.OPEN);
        OwnershipTransferEntity transfer = transfers.findBySaleId(pending.saleId()).orElseThrow();
        assertThat(transfer.getStatus()).isEqualTo(com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus.REQUESTED);
        assertThat(periods.findByGoatIdOrderByStartedAtAscIdAsc(goat.getTechnicalId())).singleElement()
                .satisfies(period -> assertThat(period.getEndedAt()).isNull());
        assertThat(goats.findById(goat.getTechnicalId()).orElseThrow().getFarm().getId()).isEqualTo(sourceFarm.getId());
    }

    private User user(String suffix) {
        User user = new User();
        user.setName(suffix);
        user.setEmail(suffix + "@example.com");
        user.setCpf(String.format("%011d", Math.abs((long) suffix.hashCode())));
        user.setPassword("password");
        return users.saveAndFlush(user);
    }

    private GoatFarm farm(String name, String tod, User user) {
        GoatFarm farm = new GoatFarm();
        farm.setName(name);
        farm.setTod(tod);
        farm.setUser(user);
        return farms.saveAndFlush(farm);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FailureConfiguration {
        static final AtomicLong PRINCIPAL_ID = new AtomicLong(1L);

        @Bean
        @Primary
        FarmAuthorizationUseCase authorization() {
            return new FarmAuthorizationUseCase() {
                public void verifyFarmOwnership(Long farmId) { }
                public void verifyFarmManagement(Long farmId) { }
                public boolean isFarmOwner(Long farmId) { return true; }
                public boolean canAdministerFarm(Long farmId) { return true; }
                public boolean canManageFarm(Long farmId) { return true; }
            };
        }

        @Bean
        @Primary
        CurrentPrincipalQueryUseCase currentPrincipal() {
            return new CurrentPrincipalQueryUseCase() {
                public java.util.Optional<AuthenticatedPrincipal> findCurrent() { return java.util.Optional.of(requireCurrent()); }
                public AuthenticatedPrincipal requireCurrent() {
                    return new AuthenticatedPrincipal(PRINCIPAL_ID.get(), "rollback@example.com", "Rollback", Set.of("ROLE_FARM_OWNER"));
                }
            };
        }

        @Bean
        @Primary
        GoatCurrentOwnerProjectionPort failingProjection() {
            return (goatId, expectedSourceFarmId, targetFarmId) -> false;
        }
    }
}
