package com.devmaster.goatfarm.goatownership.api.controller;

import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.devmaster.goatfarm.config.security.authorization.CanManageFarm;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goatownership.api.mapper.FarmGoatRegistryApiMapper;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryDisposition;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryItem;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatRegistryRole;
import com.devmaster.goatfarm.goatownership.application.ports.in.FarmGoatRegistryQueryUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
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

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(new FarmGoatRegistryController(
                        queryUseCase, new FarmGoatRegistryApiMapper()))
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
        // Creator-only: currentOwnerFarmId is null
        var creatorOnly = new FarmGoatRegistryItem(
                new GoatId(1L), "RG1", "Goat1", GoatStatus.ATIVO,
                10L, "Capril Origin", Set.of(FarmGoatRegistryRole.CREATOR),
                FarmGoatRegistryDisposition.NONE, null
        );
        // Legacy/current owner without creator reference: creatorFarmId and creatorNameSnapshot are null
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

    private FarmGoatRegistryItem sampleItem(long id, String name) {
        return new FarmGoatRegistryItem(
                new GoatId(id),
                "RG" + id,
                name,
                GoatStatus.ATIVO,
                1L,
                "Capril Test",
                Set.of(FarmGoatRegistryRole.CURRENT_OWNER),
                FarmGoatRegistryDisposition.CURRENT,
                1L
        );
    }
}
