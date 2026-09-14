package com.devmaster.goatfarm.goatownership.api;

import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.api.controller.OwnershipTransferController;
import com.devmaster.goatfarm.goatownership.api.mapper.OwnershipTransferApiMapper;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferDirection;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferPageQuery;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipTransferQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipTransferUseCase;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransfer;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferKind;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import com.devmaster.goatfarm.application.pagination.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OwnershipTransferControllerTest {
    @Mock GoatOwnershipTransferUseCase transferUseCase;
    @Mock GoatOwnershipTransferQueryUseCase queryUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new OwnershipTransferController(
                        transferUseCase, queryUseCase, new OwnershipTransferApiMapper()))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void validRequestReturnsCreatedLocationAndMapsOnlyAuthorizedFields() throws Exception {
        when(transferUseCase.requestInternalTransfer(any())).thenReturn(transfer(99L));

        mockMvc.perform(post("/api/v1/ownership-transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goatId\":42,\"targetFarmId\":20,\"reason\":\"move\",\"idempotencyKey\":\"key-1\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/ownership-transfers/99"))
                .andExpect(jsonPath("$.id").value(99))
                .andExpect(jsonPath("$.goatId").value(42))
                .andExpect(jsonPath("$.sourceFarmId").value(10));

        var captor = ArgumentCaptor.forClass(com.devmaster.goatfarm.goatownership.application.model.InternalOwnershipTransferRequest.class);
        verify(transferUseCase).requestInternalTransfer(captor.capture());
        assertThat(captor.getValue().goatId()).isEqualTo(GoatId.of(42L));
        assertThat(captor.getValue().targetFarmId()).isEqualTo(20L);
        assertThat(captor.getValue().reason()).isEqualTo("move");
    }

    @Test
    void validationFailuresReturnUnprocessableEntity() throws Exception {
        mockMvc.perform(post("/api/v1/ownership-transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"goatId\":0,\"targetFarmId\":-1,\"reason\":\" \",\"idempotencyKey\":\"\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void commandsDelegateToExistingUseCase() throws Exception {
        when(transferUseCase.acceptTransfer(99L)).thenReturn(transfer(99L));
        when(transferUseCase.rejectTransfer(99L)).thenReturn(transfer(99L));
        when(transferUseCase.cancelTransfer(99L)).thenReturn(transfer(99L));

        mockMvc.perform(post("/api/v1/ownership-transfers/99/accept")).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/ownership-transfers/99/reject")).andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/ownership-transfers/99/cancel")).andExpect(status().isOk());

        verify(transferUseCase).acceptTransfer(99L);
        verify(transferUseCase).rejectTransfer(99L);
        verify(transferUseCase).cancelTransfer(99L);
    }

    @Test
    void authorizedReadAndFarmInboxMapApplicationPage() throws Exception {
        when(queryUseCase.findAuthorizedById(99L)).thenReturn(transfer(99L));
        when(queryUseCase.listForFarm(10L, OwnershipTransferDirection.INCOMING,
                OwnershipTransferStatus.REQUESTED, new OwnershipTransferPageQuery(0, 20)))
                .thenReturn(new PageResult<>(List.of(transfer(99L)), 1, 0, 20));

        mockMvc.perform(get("/api/v1/ownership-transfers/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99));
        mockMvc.perform(get("/api/v1/goatfarms/10/ownership-transfers")
                        .param("direction", "INCOMING")
                        .param("status", "REQUESTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(99))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void missingDirectionIsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/goatfarms/10/ownership-transfers"))
                .andExpect(status().isBadRequest());
    }

    private OwnershipTransfer transfer(long id) {
        return OwnershipTransfer.rehydrate(id, GoatId.of(42L), 10L, 20L,
                OwnershipTransferKind.INTERNAL_TRANSFER, OwnershipTransferStatus.REQUESTED,
                "move", "key-" + id, Instant.parse("2026-01-01T00:00:00Z"),
                null, null, null, null, 7L, null, null, null);
    }
}
