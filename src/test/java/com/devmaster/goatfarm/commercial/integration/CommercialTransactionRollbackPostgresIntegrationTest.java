package com.devmaster.goatfarm.commercial.integration;

import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditEntryVO;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditRecordVO;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.commercial.application.model.AnimalSaleCommand;
import com.devmaster.goatfarm.commercial.application.model.AnimalSaleRecord;
import com.devmaster.goatfarm.commercial.application.model.CustomerRecord;
import com.devmaster.goatfarm.commercial.application.ports.in.CommercialUseCase;
import com.devmaster.goatfarm.commercial.application.ports.out.AnimalSalePersistencePort;
import com.devmaster.goatfarm.commercial.application.ports.out.CustomerPersistencePort;
import com.devmaster.goatfarm.commercial.business.bo.AnimalSaleRequestVO;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.authority.persistence.entity.User;
import com.devmaster.goatfarm.authority.persistence.repository.UserRepository;
import com.devmaster.goatfarm.farm.persistence.entity.GoatFarm;
import com.devmaster.goatfarm.farm.persistence.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import com.devmaster.goatfarm.goat.persistence.repository.GoatRepository;
import com.devmaster.goatfarm.goatownership.domain.OwnershipEntryType;
import com.devmaster.goatfarm.goatownership.persistence.entity.GoatOwnershipPeriodEntity;
import com.devmaster.goatfarm.goatownership.persistence.repository.GoatOwnershipPeriodRepository;
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
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Proves the real Spring transaction boundary used by Commercial animal sales.
 * The sale port is the only injected failure; Goat persistence remains real.
 */
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Import(CommercialTransactionRollbackPostgresIntegrationTest.RollbackTestConfiguration.class)
class CommercialTransactionRollbackPostgresIntegrationTest {

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

    @Autowired
    private CommercialUseCase commercialUseCase;

    @Autowired
    private GoatRepository goatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GoatFarmRepository goatFarmRepository;

    @Autowired
    private GoatOwnershipPeriodRepository ownershipPeriodRepository;

    @Autowired
    private GoatFarmPersistencePort farmPort;

    @Autowired
    private CustomerPersistencePort customerPort;

    @Test
    void failedAnimalSaleRollsBackRealGoatExit() {
        GoatFarm farm = new GoatFarm();
        farm.setName("Rollback proof farm");
        farm.setTod("RBP01");
        User owner = new User();
        owner.setName("Rollback owner");
        owner.setEmail("rollback-owner@example.com");
        owner.setCpf("99999999999");
        owner.setPassword("password");
        farm.setUser(owner);

        // The test doubles expose the farm/customer to CommercialBusiness;
        // only Goat persistence is deliberately real in this proof.
        Long farmId = 1L;
        Long customerId = 2L;
        GoatEntity goat = new GoatEntity();
        goat.setRegistrationNumber("RBP010001");
        goat.setName("Rollback goat");
        goat.setGender(Gender.FEMEA);
        goat.setBirthDate(LocalDate.of(2024, 1, 1));
        goat.setStatus(GoatStatus.ATIVO);
        goat.setTod("RBP01");
        goat.setToe("0001");
        goat.setFarm(farm);
        goat.setUser(owner);

        // Persist farm/user through the real repositories indirectly by using
        // the adapter's managed references is not possible in this isolated
        // fixture; the test configuration supplies the farm identity while
        // the Goat adapter persists against the same PostgreSQL transaction.
        // The entity is attached by the repository before the service call.
        // (The fixture's farm/customer ports are technology-neutral.)
        GoatFarm persistedFarm = persistFarm(farm, owner);
        goat.setFarm(persistedFarm);
        GoatEntity persistedGoat = goatRepository.saveAndFlush(goat);
        GoatOwnershipPeriodEntity ownershipPeriod = new GoatOwnershipPeriodEntity();
        ownershipPeriod.setGoatId(persistedGoat.getTechnicalId());
        ownershipPeriod.setFarmId(persistedFarm.getId());
        ownershipPeriod.setStartedAt(Instant.parse("2024-01-01T00:00:00Z"));
        ownershipPeriod.setEntryType(OwnershipEntryType.MANUAL_IMPORT);
        ownershipPeriod.setSource("TEST_FIXTURE");
        ownershipPeriodRepository.saveAndFlush(ownershipPeriod);

        configureFixture(persistedFarm.getId());
        AnimalSaleRequestVO request = new AnimalSaleRequestVO(
                persistedGoat.getRegistrationNumber(), customerId, LocalDate.of(2026, 9, 1),
                new BigDecimal("100.00"), LocalDate.of(2026, 9, 10), null, "rollback proof");

        assertThatThrownBy(() -> commercialUseCase.createAnimalSale(persistedFarm.getId(), request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("injected animal-sale persistence failure");

        GoatEntity reloaded = goatRepository.findById(persistedGoat.getTechnicalId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(GoatStatus.ATIVO);
        assertThat(reloaded.getExitType()).isNull();
        assertThat(reloaded.getExitDate()).isNull();
    }

    private GoatFarm persistFarm(GoatFarm farm, User owner) {
        // Repository access is kept local to this test fixture; production
        // code remains behind ports/adapters.
        User persistedOwner = userRepository.saveAndFlush(owner);
        farm.setUser(persistedOwner);
        return goatFarmRepository.saveAndFlush(farm);
    }

    private void configureFixture(Long farmId) {
        // The primary test beans are deterministic and farm-scoped.
        assertThat(farmId).isPositive();
        assertThat(farmPort).isNotNull();
        assertThat(customerPort).isNotNull();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class RollbackTestConfiguration {
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
        CustomerPersistencePort customerPersistence() {
            return new CustomerPersistencePort() {
                public CustomerRecord save(CustomerRecord customer) { return customer; }
                public List<CustomerRecord> findCustomersByFarmId(Long farmId) { return List.of(); }
                public Optional<CustomerRecord> findCustomerByIdAndFarmId(Long customerId, Long farmId) {
                    return Optional.of(new CustomerRecord(customerId, farmId, "Rollback customer", null,
                            null, null, null, true, null, null));
                }
                public long countCustomersByFarmId(Long farmId) { return 1; }
            };
        }

        @Bean
        @Primary
        AnimalSalePersistencePort failingAnimalSalePersistence() {
            return new AnimalSalePersistencePort() {
                public AnimalSaleRecord save(AnimalSaleCommand sale) {
                    throw new IllegalStateException("injected animal-sale persistence failure");
                }
                public boolean existsByFarmIdAndGoatTechnicalId(Long farmId, Long goatTechnicalId) { return false; }
                public boolean existsByLegacyRegistrationNumber(String registrationNumber) { return false; }
                public Optional<AnimalSaleRecord> findAnimalSaleByIdAndFarmId(Long saleId, Long farmId) { return Optional.empty(); }
                public List<AnimalSaleRecord> findAnimalSalesByFarmId(Long farmId) { return List.of(); }
            };
        }

        @Bean
        @Primary
        GoatFarmPersistencePort farmPersistence() {
            return new GoatFarmPersistencePort() {
                public Optional<FarmRecord> findById(Long id) { return Optional.of(new FarmRecord(id, "Rollback proof farm", "RBP01", null, null, null, List.of(), null, null, 0)); }
                public Optional<FarmRecord> findByIdAndUserId(Long id, Long userId) { return findById(id); }
                public Optional<FarmRecord> findByAddressId(Long addressId) { return Optional.empty(); }
                public Optional<FarmRecord> findByIdWithDetails(Long id) { return findById(id); }
                public com.devmaster.goatfarm.application.pagination.PageResult<FarmRecord> searchByName(String name, com.devmaster.goatfarm.application.pagination.PageQuery pageQuery) { return new com.devmaster.goatfarm.application.pagination.PageResult<>(List.of(), 0, pageQuery.page(), pageQuery.size()); }
                public com.devmaster.goatfarm.application.pagination.PageResult<FarmRecord> findAll(com.devmaster.goatfarm.application.pagination.PageQuery pageQuery) { return new com.devmaster.goatfarm.application.pagination.PageResult<>(List.of(), 0, pageQuery.page(), pageQuery.size()); }
                public boolean existsByName(String name) { return false; }
                public boolean existsByTod(String tod) { return false; }
                public FarmRecord save(com.devmaster.goatfarm.farm.application.model.FarmPersistenceCommand command) { throw new UnsupportedOperationException(); }
                public void deleteById(Long id) { }
                public boolean existsById(Long farmId) { return true; }
                public Optional<com.devmaster.goatfarm.farm.application.model.FarmRegistrationSnapshot> findRegistrationById(Long farmId) { return Optional.empty(); }
                public Optional<Long> findOwnerId(Long farmId) { return Optional.empty(); }
            };
        }

        @Bean
        @Primary
        OperationalAuditUseCase audit() {
            return new OperationalAuditUseCase() {
                public void record(OperationalAuditRecordVO recordVO) { }
                public List<OperationalAuditEntryVO> listEntries(Long farmId, String goatId, int limit) { return List.of(); }
            };
        }
    }
}
