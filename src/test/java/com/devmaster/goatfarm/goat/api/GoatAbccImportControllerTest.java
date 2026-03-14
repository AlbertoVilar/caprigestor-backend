package com.devmaster.goatfarm.goat.api;

import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.devmaster.goatfarm.config.security.OwnershipService;
import com.devmaster.goatfarm.goat.application.ports.in.GoatAbccImportUseCase;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmItemResultVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccBatchConfirmResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccPreviewResponseVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccRaceOptionVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchItemVO;
import com.devmaster.goatfarm.goat.business.bo.abcc.GoatAbccSearchResponseVO;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@Import({GlobalExceptionHandler.class})
@ActiveProfiles("test")
class GoatAbccImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private GoatAbccImportUseCase goatAbccImportUseCase;

    @MockBean
    private com.devmaster.goatfarm.authority.business.AdminMaintenanceBusiness adminMaintenanceBusiness;

    @MockBean
    private OwnershipService ownershipService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        when(ownershipService.isFarmOwner(eq(1L))).thenReturn(true);
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void shouldListAbccRacesSuccessfully() throws Exception {
        when(goatAbccImportUseCase.listRaces(eq(1L))).thenReturn(List.of(
                GoatAbccRaceOptionVO.builder().id(9).name("SAANEN").normalizedBreed(GoatBreed.SAANEN).build(),
                GoatAbccRaceOptionVO.builder().id(2).name("BOER").normalizedBreed(GoatBreed.BOER).build()
        ));

        mockMvc.perform(get("/api/v1/goatfarms/1/goats/imports/abcc/races"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(9))
                .andExpect(jsonPath("$.items[0].name").value("SAANEN"))
                .andExpect(jsonPath("$.items[0].normalizedBreed").value("SAANEN"));

        verify(goatAbccImportUseCase).listRaces(eq(1L));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void shouldSearchAbccSuccessfully() throws Exception {
        when(goatAbccImportUseCase.search(eq(1L), any())).thenReturn(GoatAbccSearchResponseVO.builder()
                .currentPage(1)
                .totalPages(5)
                .pageSize(1)
                .items(List.of(
                        GoatAbccSearchItemVO.builder()
                                .externalSource("ABCC_PUBLIC")
                                .externalId("4044")
                                .nome("FALCÃO DO CAPRIL DA PRATA")
                                .normalizedGender(Gender.MACHO)
                                .normalizedBreed(GoatBreed.SAANEN)
                                .normalizedStatus(GoatStatus.ATIVO)
                                .build()
                ))
                .build());

        String payload = """
                {
                  "raceName": "SAANEN",
                  "affix": "CRS",
                  "page": 1
                }
                """;

        mockMvc.perform(post("/api/v1/goatfarms/1/goats/imports/abcc/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(1))
                .andExpect(jsonPath("$.items[0].externalId").value("4044"))
                .andExpect(jsonPath("$.items[0].normalizedGender").value("MACHO"))
                .andExpect(jsonPath("$.items[0].normalizedBreed").value("SAANEN"));

        verify(goatAbccImportUseCase).search(eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void shouldPreviewAbccSuccessfully() throws Exception {
        when(goatAbccImportUseCase.preview(eq(1L), any())).thenReturn(GoatAbccPreviewResponseVO.builder()
                .externalSource("ABCC_PUBLIC")
                .externalId("4044")
                .registrationNumber("1433214017")
                .name("FALCÃO DO CAPRIL DA PRATA")
                .gender(Gender.MACHO)
                .breed(GoatBreed.SAANEN)
                .status(GoatStatus.ATIVO)
                .birthDate(LocalDate.of(2014, 5, 16))
                .farmId(1L)
                .farmName("Capril Vilar")
                .build());

        String payload = """
                {
                  "externalId": "4044"
                }
                """;

        mockMvc.perform(post("/api/v1/goatfarms/1/goats/imports/abcc/preview")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalSource").value("ABCC_PUBLIC"))
                .andExpect(jsonPath("$.registrationNumber").value("1433214017"))
                .andExpect(jsonPath("$.gender").value("MACHO"))
                .andExpect(jsonPath("$.breed").value("SAANEN"));

        verify(goatAbccImportUseCase).preview(eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void shouldConfirmAbccImportSuccessfully() throws Exception {
        GoatResponseVO created = new GoatResponseVO();
        created.setRegistrationNumber("1643218012");
        created.setName("XEQUE V DO CAPRIL VILAR");
        created.setGender(Gender.MACHO);
        created.setBreed(GoatBreed.ALPINA);
        created.setStatus(GoatStatus.ATIVO);
        when(goatAbccImportUseCase.confirm(eq(1L), eq("4044"), any(GoatRequestVO.class))).thenReturn(created);

        String payload = """
                {
                  "externalId": "4044",
                  "goat": {
                    "registrationNumber": "1643218012",
                    "name": "XEQUE V DO CAPRIL VILAR",
                    "gender": "MACHO",
                    "breed": "ALPINA",
                    "color": "CHAMOISÉE",
                    "birthDate": "2018-06-27",
                    "status": "ATIVO",
                    "tod": "16432",
                    "toe": "18012",
                    "category": "PO",
                    "fatherRegistrationNumber": "1635717065",
                    "motherRegistrationNumber": "2114517012"
                  }
                }
                """;

        mockMvc.perform(post("/api/v1/goatfarms/1/goats/imports/abcc/confirm")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber").value("1643218012"))
                .andExpect(jsonPath("$.name").value("XEQUE V DO CAPRIL VILAR"))
                .andExpect(jsonPath("$.breed").value("ALPINA"));

        verify(goatAbccImportUseCase).confirm(eq(1L), eq("4044"), any(GoatRequestVO.class));
    }

    @Test
    @WithMockUser(roles = "OPERATOR")
    void shouldConfirmBatchAbccImportSuccessfully() throws Exception {
        when(goatAbccImportUseCase.confirmBatch(eq(1L), any())).thenReturn(
                GoatAbccBatchConfirmResponseVO.builder()
                        .totalSelected(4)
                        .totalImported(1)
                        .totalSkippedDuplicate(1)
                        .totalSkippedTodMismatch(1)
                        .totalError(1)
                        .results(List.of(
                                GoatAbccBatchConfirmItemResultVO.builder()
                                        .externalId("A-001")
                                        .registrationNumber("1111111111")
                                        .name("IMPORTAVEL")
                                        .status("IMPORTED")
                                        .message("Animal importado com sucesso.")
                                        .build(),
                                GoatAbccBatchConfirmItemResultVO.builder()
                                        .externalId("A-002")
                                        .registrationNumber("2222222222")
                                        .name("DUPLICADA")
                                        .status("SKIPPED_DUPLICATE")
                                        .message("Registro já existente nesta fazenda. Item ignorado por duplicidade.")
                                        .build(),
                                GoatAbccBatchConfirmItemResultVO.builder()
                                        .externalId("A-003")
                                        .status("SKIPPED_TOD_MISMATCH")
                                        .message("O animal selecionado possui TOD diferente do TOD da fazenda.")
                                        .build(),
                                GoatAbccBatchConfirmItemResultVO.builder()
                                        .externalId("A-004")
                                        .status("ERROR")
                                        .message("Registro ABCC ausente para importar este item.")
                                        .build()
                        ))
                        .build()
        );

        String payload = """
                {
                  "items": [
                    { "externalId": "A-001" },
                    { "externalId": "A-002" },
                    { "externalId": "A-003" },
                    { "externalId": "A-004" }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/goatfarms/1/goats/imports/abcc/confirm-batch")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSelected").value(4))
                .andExpect(jsonPath("$.totalImported").value(1))
                .andExpect(jsonPath("$.totalSkippedDuplicate").value(1))
                .andExpect(jsonPath("$.totalSkippedTodMismatch").value(1))
                .andExpect(jsonPath("$.totalError").value(1))
                .andExpect(jsonPath("$.results[2].status").value("SKIPPED_TOD_MISMATCH"));

        verify(goatAbccImportUseCase).confirmBatch(eq(1L), any());
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void shouldForbidViewerFromAbccImportEndpoints() throws Exception {
        String payload = """
                {
                  "raceName": "SAANEN",
                  "affix": "CRS",
                  "page": 1
                }
                """;

        mockMvc.perform(post("/api/v1/goatfarms/1/goats/imports/abcc/search")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());

        verify(goatAbccImportUseCase, never()).search(eq(1L), any());
    }
}
