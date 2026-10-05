package com.devmaster.goatfarm.goat.integration;

import com.devmaster.goatfarm.application.core.business.common.EntityFinder;
import com.devmaster.goatfarm.authority.application.ports.in.CurrentPrincipalQueryUseCase;
import com.devmaster.goatfarm.authority.application.ports.in.FarmAuthorizationUseCase;
import com.devmaster.goatfarm.authority.business.bo.AuthenticatedPrincipal;
import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.in.GoatAbccImportUseCase;
import com.devmaster.goatfarm.goat.application.ports.in.GoatAbccQueryUseCase;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatAbccPublicQueryPort;
import com.devmaster.goatfarm.goat.application.ports.out.GoatReferenceQueryPort;
import com.devmaster.goatfarm.goat.business.GoatAbccImportBusiness;
import com.devmaster.goatfarm.goat.business.GoatAbccQueryBusiness;
import com.devmaster.goatfarm.goat.business.abcc.AbccAnimalTranslator;
import com.devmaster.goatfarm.goat.business.abcc.AbccImportEligibilityPolicy;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRaceOptionVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawPreviewVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRawSearchResultVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRegistrationLookupRequestVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchRequestVO;
import com.devmaster.goatfarm.goat.enums.Category;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(GoatAbccTransactionBoundaryIntegrationTest.SpringConfig.class)
class GoatAbccTransactionBoundaryIntegrationTest {

    private static final long FARM_ID = 1L;
    private static final FarmRecord FARM = new FarmRecord(
            FARM_ID, "Transaction Probe Farm", "12345", null, null, null, List.of(), null, null, null);

    @Autowired private GoatAbccQueryUseCase queryUseCase;
    @Autowired private GoatAbccImportUseCase importUseCase;

    @Autowired private GoatAbccPublicQueryPort abccPublicQueryPort;
    @Autowired private GoatFarmPersistencePort goatFarmPersistencePort;
    @Autowired private FarmAuthorizationUseCase farmAuthorizationUseCase;
    @Autowired private CurrentPrincipalQueryUseCase currentPrincipalQueryUseCase;
    @Autowired private GoatManagementUseCase goatManagementUseCase;
    @Test
    void characterizesTransactionStateAtEveryAbccOutboundCall() {
        Map<String, Boolean> activeByOperation = new ConcurrentHashMap<>();
        when(goatFarmPersistencePort.findById(FARM_ID)).thenReturn(Optional.of(FARM));
        when(currentPrincipalQueryUseCase.requireCurrent()).thenReturn(
                new AuthenticatedPrincipal(10L, "owner@example.com", "Farm Owner", Set.of("ROLE_FARM_OWNER")));
        when(abccPublicQueryPort.listRaces()).thenAnswer(invocation -> {
            capture(activeByOperation, "listRaces");
            return List.of(GoatAbccRaceOptionVO.builder().id(9).name("SAANEN").build());
        });
        when(abccPublicQueryPort.search(any())).thenAnswer(invocation -> {
            capture(activeByOperation, "search");
            return emptySearchResult();
        });
        when(abccPublicQueryPort.searchByRegistration(9, "1234500001")).thenAnswer(invocation -> {
            capture(activeByOperation, "lookup.registrationSearch");
            return GoatAbccRawSearchResultVO.builder().items(List.of(
                    GoatAbccRawSearchItemVO.builder().externalId("lookup-preview").tod("12345")
                            .toe("00001").raca("SAANEN").build())).build();
        });
        when(abccPublicQueryPort.preview(any())).thenAnswer(invocation -> {
            String externalId = invocation.getArgument(0);
            capture(activeByOperation, "preview." + externalId);
            return rawPreview(externalId);
        });
        when(goatManagementUseCase.createGoat(eq(FARM_ID), any(GoatRequestVO.class), any()))
                .thenReturn(new GoatResponseVO());

        queryUseCase.listRaces(FARM_ID);
        queryUseCase.search(FARM_ID, GoatAbccSearchRequestVO.builder().raceId(9).affix("CRS").page(1).build());
        var preview = queryUseCase.preview(
                FARM_ID, GoatAbccPreviewRequestVO.builder().externalId("query-preview").build());
        queryUseCase.lookupByRegistration(FARM_ID, GoatAbccRegistrationLookupRequestVO.builder()
                .raceId(9).registrationNumber("1234500001").build());
        importUseCase.confirm(FARM_ID, "confirm-preview", goatRequest());

        assertThat(preview.getFarmName()).isEqualTo(FARM.name());
        assertThat(activeByOperation).containsOnlyKeys(
                "listRaces", "search", "preview.query-preview", "lookup.registrationSearch",
                "preview.lookup-preview", "preview.confirm-preview");
        assertThat(activeByOperation).containsEntry("listRaces", false)
                .containsEntry("search", false)
                .containsEntry("preview.query-preview", false)
                .containsEntry("lookup.registrationSearch", false)
                .containsEntry("preview.lookup-preview", false)
                .containsEntry("preview.confirm-preview", false);
    }

    private void capture(Map<String, Boolean> activeByOperation, String operation) {
        activeByOperation.put(operation, TransactionSynchronizationManager.isActualTransactionActive());
    }

    private GoatAbccRawSearchResultVO emptySearchResult() {
        return GoatAbccRawSearchResultVO.builder().currentPage(1).totalPages(1).items(List.of()).build();
    }

    private GoatAbccRawPreviewVO rawPreview(String externalId) {
        return GoatAbccRawPreviewVO.builder().externalId(externalId).registro("1234500001")
                .nome("Transaction Probe Goat").sexo("Fêmea").raca("SAANEN").pelagem("Branca")
                .situacao("RGD").categoria("PA").dataNascimento("01/01/2025").tod("12345")
                .toe("00001").build();
    }

    private GoatRequestVO goatRequest() {
        return GoatRequestVO.builder().registrationNumber("1234500001").tod("12345").toe("00001")
                .name("Transaction Probe Goat").gender(Gender.FEMEA).breed(GoatBreed.SAANEN)
                .color("Branca").birthDate(LocalDate.of(2025, 1, 1)).status(GoatStatus.ATIVO)
                .category(Category.PA).build();
    }

    @Configuration
    @EnableTransactionManagement
    static class SpringConfig {

        @Bean
        DataSource dataSource() {
            return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        GoatAbccPublicQueryPort goatAbccPublicQueryPort() {
            return mock(GoatAbccPublicQueryPort.class);
        }

        @Bean
        GoatFarmPersistencePort goatFarmPersistencePort() {
            return mock(GoatFarmPersistencePort.class);
        }

        @Bean
        FarmAuthorizationUseCase farmAuthorizationUseCase() {
            return mock(FarmAuthorizationUseCase.class);
        }

        @Bean
        CurrentPrincipalQueryUseCase currentPrincipalQueryUseCase() {
            return mock(CurrentPrincipalQueryUseCase.class);
        }

        @Bean
        GoatManagementUseCase goatManagementUseCase() {
            return mock(GoatManagementUseCase.class);
        }

        @Bean
        GoatReferenceQueryPort goatReferenceQueryPort() {
            return mock(GoatReferenceQueryPort.class);
        }

        @Bean
        EntityFinder entityFinder() {
            return new EntityFinder();
        }

        @Bean
        AbccAnimalTranslator abccAnimalTranslator() {
            return new AbccAnimalTranslator();
        }

        @Bean
        AbccImportEligibilityPolicy abccImportEligibilityPolicy() {
            return new AbccImportEligibilityPolicy();
        }

        @Bean
        GoatAbccQueryBusiness goatAbccQueryBusiness(
                GoatAbccPublicQueryPort abccPort,
                GoatFarmPersistencePort farmPort,
                EntityFinder entityFinder,
                AbccAnimalTranslator translator
        ) {
            return new GoatAbccQueryBusiness(abccPort, farmPort, entityFinder, translator);
        }

        @Bean
        GoatAbccImportBusiness goatAbccImportBusiness(
                FarmAuthorizationUseCase authorization,
                GoatFarmPersistencePort farmPort,
                GoatAbccQueryUseCase queryUseCase,
                GoatManagementUseCase goatManagement,
                GoatReferenceQueryPort goatReferenceQueryPort,
                EntityFinder entityFinder,
                CurrentPrincipalQueryUseCase principalQuery,
                AbccAnimalTranslator translator,
                AbccImportEligibilityPolicy eligibilityPolicy
        ) {
            return new GoatAbccImportBusiness(authorization, farmPort, queryUseCase, goatManagement,
                    goatReferenceQueryPort, entityFinder, principalQuery, translator, eligibilityPolicy);
        }
    }
}
