package com.devmaster.goatfarm.goatownership.api.controller;

import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.devmaster.goatfarm.config.security.authorization.CanManageFarm;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyIntegrationSnapshot;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyNodeSource;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyTreeNode;
import com.devmaster.goatfarm.genealogy.application.model.GenealogyTreeSnapshot;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatHistoricalGenealogyApiMapper;
import com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatHistoricalHealthApiMapper;
import com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatHistoricalMilkLactationApiMapper;
import com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatHistoricalEventsApiMapper;
import com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatRegistryApiMapper;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalLactationItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalMilkLactationSnapshot;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalMilkProductionItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalHealthSnapshot;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalEventsSnapshot;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryDisposition;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryRole;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalGenealogyQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalHealthQueryUseCase;
import com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatHistoricalReproductionApiMapper;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalReproductionSnapshot;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalReproductionQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalMilkLactationQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatHistoricalEventsQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatRegistryQueryUseCase;
import com.devmaster.goatfarm.milk.enums.LactationStatus;
import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FarmGoatRegistryControllerTest {

    @Mock
    private FarmGoatRegistryQueryUseCase queryUseCase;
    @Mock
    private FarmGoatHistoricalGenealogyQueryUseCase historicalGenealogyQueryUseCase;
    @Mock
    private FarmGoatHistoricalMilkLactationQueryUseCase historicalMilkLactationQueryUseCase;
    @Mock
    private FarmGoatHistoricalReproductionQueryUseCase historicalReproductionQueryUseCase;
    @Mock
    private FarmGoatHistoricalHealthQueryUseCase historicalHealthQueryUseCase;
    @Mock
    private FarmGoatHistoricalEventsQueryUseCase historicalEventsQueryUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(new FarmGoatRegistryController(
                        queryUseCase,
                        new FarmGoatRegistryApiMapper(),
                        historicalGenealogyQueryUseCase,
                        new FarmGoatHistoricalGenealogyApiMapper(),
                        historicalMilkLactationQueryUseCase,
                        new FarmGoatHistoricalMilkLactationApiMapper(),
                        historicalReproductionQueryUseCase,
                        new FarmGoatHistoricalReproductionApiMapper(),
                        historicalHealthQueryUseCase,
                        new FarmGoatHistoricalHealthApiMapper(),
                        historicalEventsQueryUseCase,
                        new FarmGoatHistoricalEventsApiMapper()))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Case 1: Registry with one item returns HTTP 200 with all exact JSON fields")
    void case01_registryWithOneItem_returnsHttp200WithExactJsonFields() throws Exception {
        var item = new FarmGoatRegistryItem(
                new GoatId(42L),
                "RG42",
                "Estrela",
                GoatStatus.ATIVO,
                1L,
                "Capril A",
                Set.of(FarmGoatRegistryRole.CREATOR, FarmGoatRegistryRole.CURRENT_OWNER),
                FarmGoatRegistryDisposition.CURRENT,
                1L
        );
        when(queryUseCase.findForFarm(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].goatId").value(42))
                .andExpect(jsonPath("$[0].registrationNumber").value("RG42"))
                .andExpect(jsonPath("$[0].name").value("Estrela"))
                .andExpect(jsonPath("$[0].globalStatus").value("ATIVO"))
                .andExpect(jsonPath("$[0].creatorFarmId").value(1))
                .andExpect(jsonPath("$[0].creatorNameSnapshot").value("Capril A"))
                .andExpect(jsonPath("$[0].roles", containsInAnyOrder("CREATOR", "CURRENT_OWNER")))
                .andExpect(jsonPath("$[0].disposition").value("CURRENT"))
                .andExpect(jsonPath("$[0].currentOwnerFarmId").value(1));
    }

    @Test
    @DisplayName("Case 2: Multiple items preserve the GoatId ASC order provided by the use case")
    void case02_multipleItems_preservesGoatIdAscOrderFromUseCase() throws Exception {
        var item10 = sampleItem(10L, "Goat10");
        var item20 = sampleItem(20L, "Goat20");
        var item30 = sampleItem(30L, "Goat30");
        when(queryUseCase.findForFarm(1L)).thenReturn(List.of(item10, item20, item30));

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].goatId").value(10))
                .andExpect(jsonPath("$[1].goatId").value(20))
                .andExpect(jsonPath("$[2].goatId").value(30));
    }

    @Test
    @DisplayName("Case 3: Empty Registry returns HTTP 200 and empty JSON array []")
    void case03_emptyRegistry_returnsHttp200AndEmptyArray() throws Exception {
        when(queryUseCase.findForFarm(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("Case 4: Controller delegates exactly findForFarm(farmId) to FarmGoatRegistryQueryUseCase")
    void case04_controllerDelegatesExactlyFindForFarmToUseCase() throws Exception {
        when(queryUseCase.findForFarm(77L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/goatfarms/77/goat-registry")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(queryUseCase).findForFarm(77L);
    }

    @Test
    @DisplayName("Case 5: API mapper converts GoatId -> Long without leaking GoatId object into JSON")
    void case05_apiMapperConvertsGoatIdToLongWithoutLeakingGoatIdObject() throws Exception {
        var item = sampleItem(99L, "Goat99");
        when(queryUseCase.findForFarm(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].goatId").isNumber())
                .andExpect(jsonPath("$[0].goatId").value(99))
                .andExpect(jsonPath("$[0].goatId.value").doesNotExist());
    }

    @Test
    @DisplayName("Case 6: Null optional provenance/current ownership fields remain null where valid")
    void case06_nullOptionalFieldsRemainNullWhereValid() throws Exception {
        var creatorOnly = new FarmGoatRegistryItem(
                new GoatId(1L), "RG1", "Goat1", GoatStatus.ATIVO,
                10L, "Capril Origin", Set.of(FarmGoatRegistryRole.CREATOR),
                FarmGoatRegistryDisposition.NONE, null
        );
        var legacyOwner = new FarmGoatRegistryItem(
                new GoatId(2L), "RG2", "Goat2", GoatStatus.ATIVO,
                null, null, Set.of(FarmGoatRegistryRole.CURRENT_OWNER),
                FarmGoatRegistryDisposition.CURRENT, 10L
        );
        when(queryUseCase.findForFarm(10L)).thenReturn(List.of(creatorOnly, legacyOwner));

        mockMvc.perform(get("/api/v1/goatfarms/10/goat-registry")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currentOwnerFarmId").doesNotExist())
                .andExpect(jsonPath("$[0].creatorFarmId").value(10))
                .andExpect(jsonPath("$[1].creatorFarmId").doesNotExist())
                .andExpect(jsonPath("$[1].creatorNameSnapshot").doesNotExist())
                .andExpect(jsonPath("$[1].currentOwnerFarmId").value(10));
    }

    @Test
    @DisplayName("Case 7: Authorization declaration @CanManageFarm is present on the controller/endpoint")
    void case07_authorizationDeclarationIsPresentOnController() throws Exception {
        boolean classAnnotated = FarmGoatRegistryController.class.isAnnotationPresent(CanManageFarm.class);
        Method method = FarmGoatRegistryController.class.getDeclaredMethod("findForFarm", Long.class);
        boolean methodAnnotated = method.isAnnotationPresent(CanManageFarm.class);

        assertThat(classAnnotated || methodAnnotated)
                .withFailMessage("FarmGoatRegistryController must declare @CanManageFarm")
                .isTrue();

        CanManageFarm annotation = classAnnotated
                ? FarmGoatRegistryController.class.getAnnotation(CanManageFarm.class)
                : method.getAnnotation(CanManageFarm.class);

        PreAuthorize preAuthorize = annotation.annotationType().getAnnotation(PreAuthorize.class);
        assertThat(preAuthorize).isNotNull();
        assertThat(preAuthorize.value()).isEqualTo("@ownershipService.canManageFarm(#farmId)");
    }

    @Test
    @DisplayName("Case 8: OpenAPI contract documents HTTP 200, 400, 401, 403 and excludes 422")
    void case08_openApiContract_declaresExpectedResponseCodes() throws Exception {
        Method method = FarmGoatRegistryController.class.getDeclaredMethod("findForFarm", Long.class);
        ApiResponses apiResponses = method.getAnnotation(ApiResponses.class);
        assertThat(apiResponses).isNotNull();

        List<String> codes = Arrays.stream(apiResponses.value())
                .map(ApiResponse::responseCode)
                .toList();

        assertThat(codes)
                .containsExactlyInAnyOrder("200", "400", "401", "403")
                .doesNotContain("422");
    }

    @Test
    @DisplayName("Case 9: GET /api/v1/goatfarms/{farmId}/goat-registry/technical-{goatId} returns 200 with exact fields")
    void case09_historicalDossierBasic_validToken_returns200WithExactFields() throws Exception {
        var item = new com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalDossierBasicItem(
                new GoatId(42L), "RG42", "Estrela", GoatStatus.ATIVO,
                com.devmaster.goatfarm.goat.enums.Gender.FEMEA, com.devmaster.goatfarm.goat.enums.GoatBreed.SAANEN,
                "Branca", java.time.LocalDate.of(2023, 5, 10), com.devmaster.goatfarm.goat.enums.Category.PA,
                "TOD-A", "TOE-B", "Pai Alpha", "RG-PAI", "Mae Beta", "RG-MAE", 1L, "Capril A",
                Set.of(FarmGoatRegistryRole.CREATOR, FarmGoatRegistryRole.CURRENT_OWNER),
                FarmGoatRegistryDisposition.CURRENT, 1L
        );
        when(queryUseCase.findHistoricalDossierBasic(1L, new GoatId(42L))).thenReturn(Optional.of(item));

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-42")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goatId").value(42))
                .andExpect(jsonPath("$.registrationNumber").value("RG42"))
                .andExpect(jsonPath("$.name").value("Estrela"));
    }

    @Test
    @DisplayName("Case 10: GET /api/v1/goatfarms/{farmId}/goat-registry/technical-{goatId}/genealogy returns 200")
    void case10_historicalGenealogy_validToken_returns200() throws Exception {
        GoatId goatId = GoatId.of(42L);
        GenealogyTreeNode principal = new GenealogyTreeNode("animalPrincipal", "Estrela", "RG42", GenealogyNodeSource.LOCAL, goatId);
        GenealogyTreeSnapshot tree = new GenealogyTreeSnapshot(principal, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        when(historicalGenealogyQueryUseCase.findHistoricalGenealogy(1L, goatId, false)).thenReturn(Optional.of(tree));

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-42/genealogy")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.animalPrincipal.name").value("Estrela"))
                .andExpect(jsonPath("$.animalPrincipal.registrationNumber").value("RG42"))
                .andExpect(jsonPath("$.animalPrincipal.source").value("LOCAL"))
                .andExpect(jsonPath("$.animalPrincipal.localTechnicalGoatId").value(42))
                .andExpect(jsonPath("$.animalPrincipal.localGoatId").doesNotExist());
    }

    @Test
    @DisplayName("Case 11: GET /api/v1/goatfarms/{farmId}/goat-registry/technical-{goatId}/genealogy?complementaryAbcc=true returns 200")
    void case11_historicalGenealogy_complementaryTrue_returns200() throws Exception {
        GoatId goatId = GoatId.of(42L);
        GenealogyTreeNode principal = new GenealogyTreeNode("animalPrincipal", "Estrela", "RG42", GenealogyNodeSource.LOCAL, goatId);
        GenealogyIntegrationSnapshot integration = new GenealogyIntegrationSnapshot("FOUND", "registrationNumber", "Success");
        GenealogyTreeSnapshot tree = new GenealogyTreeSnapshot(principal, null, null, null, null, null, null, null, null, null, null, null, null, null, null, integration);

        when(historicalGenealogyQueryUseCase.findHistoricalGenealogy(1L, goatId, true)).thenReturn(Optional.of(tree));

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-42/genealogy?complementaryAbcc=true")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.integration.status").value("FOUND"))
                .andExpect(jsonPath("$.integration.message").value("Success"));
    }

    @Test
    @DisplayName("Case 12: Invalid tokens for genealogy return HTTP 400 Bad Request")
    void case12_historicalGenealogy_invalidTokens_return400() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/42/genealogy"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/RG-123/genealogy"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-/genealogy"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-abc/genealogy"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-0/genealogy"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Case 13: Unrelated or nonexistent goat genealogy returns HTTP 404 Not Found")
    void case13_historicalGenealogy_unrelated_returns404() throws Exception {
        when(historicalGenealogyQueryUseCase.findHistoricalGenealogy(1L, GoatId.of(999L), false))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-999/genealogy"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Case 14: Controller constructor depends strictly on expected use cases and mappers")
    void case14_controllerConstructorDependencies() {
        var constructors = FarmGoatRegistryController.class.getDeclaredConstructors();
        assertThat(constructors).hasSize(1);
        var paramTypes = constructors[0].getParameterTypes();
        assertThat(paramTypes).containsExactly(
                FarmGoatRegistryQueryUseCase.class,
                FarmGoatRegistryApiMapper.class,
                FarmGoatHistoricalGenealogyQueryUseCase.class,
                FarmGoatHistoricalGenealogyApiMapper.class,
                FarmGoatHistoricalMilkLactationQueryUseCase.class,
                FarmGoatHistoricalMilkLactationApiMapper.class,
                FarmGoatHistoricalReproductionQueryUseCase.class,
                FarmGoatHistoricalReproductionApiMapper.class,
                FarmGoatHistoricalHealthQueryUseCase.class,
                FarmGoatHistoricalHealthApiMapper.class,
                FarmGoatHistoricalEventsQueryUseCase.class,
                FarmGoatHistoricalEventsApiMapper.class
        );
    }

    @Test
    @DisplayName("Case 15: Historical milk-lactation returns HTTP 200 with all exact JSON fields")
    void case15_historicalMilkLactation_returns200WithExactFields() throws Exception {
        GoatId goatId = new GoatId(42L);
        var lactationItem = new FarmGoatHistoricalLactationItem(
                100L,
                goatId,
                1L,
                LactationStatus.ACTIVE,
                LocalDate.of(2024, 1, 15),
                null,
                null,
                null,
                90,
                60,
                true
        );
        var productionItem = new FarmGoatHistoricalMilkProductionItem(
                200L,
                goatId,
                100L,
                1L,
                LocalDate.of(2024, 2, 1),
                MilkingShift.MORNING,
                new BigDecimal("2.50"),
                MilkProductionStatus.ACTIVE,
                "Ordenha normal",
                null,
                null,
                false,
                null,
                null,
                null
        );
        var snapshot = new FarmGoatHistoricalMilkLactationSnapshot(
                goatId,
                List.of(lactationItem),
                List.of(productionItem)
        );
        when(historicalMilkLactationQueryUseCase.findHistoricalMilkLactation(1L, goatId))
                .thenReturn(Optional.of(snapshot));

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-42/milk-lactation")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goatId").value(42))
                .andExpect(jsonPath("$.lactations", hasSize(1)))
                .andExpect(jsonPath("$.lactations[0].id").value(100))
                .andExpect(jsonPath("$.lactations[0].farmId").value(1))
                .andExpect(jsonPath("$.lactations[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.lactations[0].startDate").value("2024-01-15"))
                .andExpect(jsonPath("$.lactations[0].active").value(true))
                .andExpect(jsonPath("$.milkProductions", hasSize(1)))
                .andExpect(jsonPath("$.milkProductions[0].id").value(200))
                .andExpect(jsonPath("$.milkProductions[0].lactationId").value(100))
                .andExpect(jsonPath("$.milkProductions[0].farmId").value(1))
                .andExpect(jsonPath("$.milkProductions[0].date").value("2024-02-01"))
                .andExpect(jsonPath("$.milkProductions[0].shift").value("MORNING"))
                .andExpect(jsonPath("$.milkProductions[0].volumeLiters").value(2.50))
                .andExpect(jsonPath("$.milkProductions[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.milkProductions[0].notes").value("Ordenha normal"));
    }

    @Test
    @DisplayName("Case 16: Invalid tokens for milk-lactation return HTTP 400 Bad Request")
    void case16_historicalMilkLactation_invalidTokens_return400() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/42/milk-lactation"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/RG-123/milk-lactation"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-/milk-lactation"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-abc/milk-lactation"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-0/milk-lactation"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Case 17: Unrelated or nonexistent goat milk-lactation returns HTTP 404 Not Found")
    void case17_historicalMilkLactation_unrelated_returns404() throws Exception {
        when(historicalMilkLactationQueryUseCase.findHistoricalMilkLactation(1L, GoatId.of(999L)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-999/milk-lactation"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Case 18: Empty milk-lactation returns HTTP 200 with empty arrays")
    void case18_historicalMilkLactation_empty_returns200() throws Exception {
        GoatId goatId = new GoatId(42L);
        var snapshot = new FarmGoatHistoricalMilkLactationSnapshot(goatId, List.of(), List.of());
        when(historicalMilkLactationQueryUseCase.findHistoricalMilkLactation(1L, goatId))
                .thenReturn(Optional.of(snapshot));

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-42/milk-lactation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goatId").value(42))
                .andExpect(jsonPath("$.lactations", hasSize(0)))
                .andExpect(jsonPath("$.milkProductions", hasSize(0)));
    }

    @Test
    @DisplayName("Case 19: Invalid tokens for reproduction return HTTP 400 Bad Request")
    void case19_historicalReproduction_invalidTokens_return400() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/42/reproduction"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/RG-123/reproduction"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-/reproduction"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-abc/reproduction"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-0/reproduction"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Case 20: Unrelated or nonexistent goat reproduction returns HTTP 404 Not Found")
    void case20_historicalReproduction_unrelated_returns404() throws Exception {
        when(historicalReproductionQueryUseCase.findHistoricalReproduction(1L, GoatId.of(999L)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-999/reproduction"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Case 21: Empty reproduction returns HTTP 200 with empty arrays")
    void case21_historicalReproduction_empty_returns200() throws Exception {
        GoatId goatId = new GoatId(42L);
        var snapshot = new FarmGoatHistoricalReproductionSnapshot(goatId, List.of(), List.of());
        when(historicalReproductionQueryUseCase.findHistoricalReproduction(1L, goatId))
                .thenReturn(Optional.of(snapshot));

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-42/reproduction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goatId").value(42))
                .andExpect(jsonPath("$.processes", hasSize(0)))
                .andExpect(jsonPath("$.events", hasSize(0)));
    }

    @Test
    @DisplayName("Health endpoint delegates technical identity and returns an empty contract")
    void historicalHealth_endpointReturns200AndDelegatesTechnicalGoatId() throws Exception {
        GoatId goatId = GoatId.of(42L);
        when(historicalHealthQueryUseCase.findHistoricalHealth(1L, goatId))
                .thenReturn(Optional.of(new FarmGoatHistoricalHealthSnapshot(goatId, List.of())));

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-42/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goatId").value(42))
                .andExpect(jsonPath("$.events", hasSize(0)));

        verify(historicalHealthQueryUseCase).findHistoricalHealth(1L, goatId);
    }

    @Test
    @DisplayName("Events endpoint returns 404 for an unrelated or nonexistent goat")
    void historicalEvents_unrelated_returns404() throws Exception {
        when(historicalEventsQueryUseCase.findHistoricalEvents(1L, GoatId.of(999L)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-999/events"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Events endpoint maps a related empty result to HTTP 200")
    void historicalEvents_empty_returns200() throws Exception {
        GoatId goatId = GoatId.of(42L);
        when(historicalEventsQueryUseCase.findHistoricalEvents(1L, goatId))
                .thenReturn(Optional.of(new FarmGoatHistoricalEventsSnapshot(goatId, List.of())));

        mockMvc.perform(get("/api/v1/goatfarms/1/goat-registry/technical-42/events")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goatId").value(42))
                .andExpect(jsonPath("$.events", hasSize(0)));
    }

    private FarmGoatRegistryItem sampleItem(long id, String name) {
        return new FarmGoatRegistryItem(
                new GoatId(id), "RG" + id, name, GoatStatus.ATIVO, 1L, "Capril Test",
                Set.of(FarmGoatRegistryRole.CURRENT_OWNER), FarmGoatRegistryDisposition.CURRENT, 1L
        );
    }
}
