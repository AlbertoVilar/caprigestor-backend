package com.devmaster.goatfarm.commercial.api;

import com.devmaster.goatfarm.commercial.api.controller.CommercialController;
import com.devmaster.goatfarm.commercial.api.dto.AnimalSaleRequestDTO;
import com.devmaster.goatfarm.commercial.api.dto.CustomerRequestDTO;
import com.devmaster.goatfarm.commercial.api.dto.SalePaymentRequestDTO;
import com.devmaster.goatfarm.commercial.api.mapper.CommercialApiMapper;
import com.devmaster.goatfarm.commercial.application.ports.in.CommercialUseCase;
import com.devmaster.goatfarm.commercial.business.bo.AnimalSaleResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.CommercialSummaryVO;
import com.devmaster.goatfarm.commercial.business.bo.CustomerResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.MilkSaleResponseVO;
import com.devmaster.goatfarm.commercial.enums.SalePaymentStatus;
import com.devmaster.goatfarm.config.exceptions.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CommercialControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CommercialUseCase commercialUseCase;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        CommercialController controller = new CommercialController(commercialUseCase, new CommercialApiMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateCustomer() throws Exception {
        when(commercialUseCase.createCustomer(eq(1L), any())).thenReturn(
                new CustomerResponseVO(9L, "Cliente Stage 2", "123", "31999999999", "cliente@teste.com", "Observacao", true)
        );

        mockMvc.perform(post("/api/v1/goatfarms/1/commercial/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CustomerRequestDTO("Cliente Stage 2", "123", "31999999999", "cliente@teste.com", "Observacao"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.name").value("Cliente Stage 2"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldRejectInvalidAnimalSalePayload() throws Exception {
        mockMvc.perform(post("/api/v1/goatfarms/1/commercial/animal-sales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AnimalSaleRequestDTO("", null, null, null, null, null, null))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[?(@.fieldName=='goatId')]").exists())
                .andExpect(jsonPath("$.errors[?(@.fieldName=='customerId')]").exists());
    }

    @Test
    void shouldReturnSummary() throws Exception {
        when(commercialUseCase.getSummary(1L)).thenReturn(
                new CommercialSummaryVO(
                        2L,
                        1L,
                        new BigDecimal("1500.00"),
                        1L,
                        new BigDecimal("20.00"),
                        new BigDecimal("100.00"),
                        1L,
                        new BigDecimal("1500.00"),
                        1L,
                        new BigDecimal("100.00")
                )
        );

        mockMvc.perform(get("/api/v1/goatfarms/1/commercial/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerCount").value(2))
                .andExpect(jsonPath("$.animalSalesTotal").value(1500.00))
                .andExpect(jsonPath("$.milkSalesQuantityLiters").value(20.00));
    }

    @Test
    void shouldRegisterMilkSalePayment() throws Exception {
        when(commercialUseCase.registerMilkSalePayment(eq(1L), eq(7L), any())).thenReturn(
                new MilkSaleResponseVO(
                        7L,
                        9L,
                        "Cliente Stage 2",
                        LocalDate.of(2026, 3, 20),
                        new BigDecimal("30.00"),
                        new BigDecimal("4.50"),
                        new BigDecimal("135.00"),
                        LocalDate.of(2026, 3, 25),
                        SalePaymentStatus.PAID,
                        LocalDate.of(2026, 3, 26),
                        "Pago"
                )
        );

        mockMvc.perform(patch("/api/v1/goatfarms/1/commercial/milk-sales/7/payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SalePaymentRequestDTO(LocalDate.of(2026, 3, 26)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.paymentDate[0]").value(2026))
                .andExpect(jsonPath("$.paymentDate[1]").value(3))
                .andExpect(jsonPath("$.paymentDate[2]").value(26));
    }

    @Test
    void shouldListAnimalSales() throws Exception {
        when(commercialUseCase.listAnimalSales(1L)).thenReturn(List.of(
                new AnimalSaleResponseVO(
                        5L,
                        "G100",
                        "Cabra 100",
                        3L,
                        "Comprador A",
                        LocalDate.of(2026, 3, 20),
                        new BigDecimal("900.00"),
                        LocalDate.of(2026, 3, 25),
                        SalePaymentStatus.OPEN,
                        null,
                        "Teste"
                )
        ));

        mockMvc.perform(get("/api/v1/goatfarms/1/commercial/animal-sales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].goatRegistrationNumber").value("G100"))
                .andExpect(jsonPath("$[0].customerName").value("Comprador A"));
    }
}
