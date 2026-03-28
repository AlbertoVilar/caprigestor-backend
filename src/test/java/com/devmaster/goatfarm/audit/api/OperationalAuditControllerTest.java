package com.devmaster.goatfarm.audit.api;

import com.devmaster.goatfarm.audit.api.controller.OperationalAuditController;
import com.devmaster.goatfarm.audit.api.mapper.OperationalAuditApiMapper;
import com.devmaster.goatfarm.audit.application.ports.in.OperationalAuditUseCase;
import com.devmaster.goatfarm.audit.business.bo.OperationalAuditEntryVO;
import com.devmaster.goatfarm.audit.enums.OperationalAuditActionType;
import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class OperationalAuditControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OperationalAuditUseCase operationalAuditUseCase;

    @BeforeEach
    void setUp() {
        OperationalAuditController controller = new OperationalAuditController(operationalAuditUseCase, new OperationalAuditApiMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldListAuditEntries() throws Exception {
        when(operationalAuditUseCase.listEntries(1L, "G-001", 10)).thenReturn(List.of(
                new OperationalAuditEntryVO(
                        5L,
                        "G-001",
                        OperationalAuditActionType.GOAT_EXIT,
                        "Saida do rebanho",
                        "G-001",
                        "Saida auditada",
                        7L,
                        "Operador QA",
                        "operator@example.com",
                        LocalDateTime.of(2026, 3, 28, 10, 15)
                )
        ));

        mockMvc.perform(get("/api/v1/goatfarms/1/audit/entries")
                        .param("goatId", "G-001")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].actionType").value("GOAT_EXIT"))
                .andExpect(jsonPath("$[0].actionLabel").value("Saida do rebanho"))
                .andExpect(jsonPath("$[0].actorName").value("Operador QA"));
    }
}
