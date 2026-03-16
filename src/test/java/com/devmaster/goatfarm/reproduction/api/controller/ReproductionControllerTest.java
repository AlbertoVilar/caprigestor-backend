package com.devmaster.goatfarm.reproduction.api.controller;

import com.devmaster.goatfarm.reproduction.application.ports.in.ReproductionCommandUseCase;
import com.devmaster.goatfarm.reproduction.application.ports.in.ReproductionQueryUseCase;
import com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.reproduction.api.dto.BirthKidResponseDTO;
import com.devmaster.goatfarm.reproduction.api.dto.BirthRequestDTO;
import com.devmaster.goatfarm.reproduction.api.dto.BirthResponseDTO;
import com.devmaster.goatfarm.reproduction.api.dto.PregnancyResponseDTO;
import com.devmaster.goatfarm.reproduction.business.bo.BirthRequestVO;
import com.devmaster.goatfarm.reproduction.business.bo.BirthResponseVO;
import com.devmaster.goatfarm.reproduction.business.bo.PregnancyResponseVO;
import com.devmaster.goatfarm.reproduction.enums.PregnancyStatus;
import com.devmaster.goatfarm.reproduction.api.mapper.ReproductionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReproductionController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReproductionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReproductionQueryUseCase queryUseCase;

    @MockBean
    private ReproductionCommandUseCase commandUseCase;

    @MockBean
    private ReproductionMapper mapper;

    @Test
    void getPregnancyById_shouldReturn200_whenPregnancyExists() throws Exception {
        Long farmId = 1L;
        String goatId = "GOAT-001";
        Long pregnancyId = 10L;

        PregnancyResponseVO responseVO = PregnancyResponseVO.builder()
                .id(pregnancyId)
                .farmId(farmId)
                .goatId(goatId)
                .status(PregnancyStatus.ACTIVE)
                .breedingDate(LocalDate.of(2026, 1, 1))
                .confirmDate(LocalDate.of(2026, 2, 1))
                .notes("Gestação confirmada por ultrassom")
                .build();

        PregnancyResponseDTO responseDTO = PregnancyResponseDTO.builder()
                .id(pregnancyId)
                .farmId(farmId)
                .goatId(goatId)
                .status(PregnancyStatus.ACTIVE)
                .breedingDate(responseVO.getBreedingDate())
                .confirmDate(responseVO.getConfirmDate())
                .notes(responseVO.getNotes())
                .build();

        when(queryUseCase.getPregnancyById(farmId, goatId, pregnancyId)).thenReturn(responseVO);
        when(mapper.toPregnancyResponseDTO(responseVO)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}",
                        farmId, goatId, pregnancyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pregnancyId))
                .andExpect(jsonPath("$.goatId").value(goatId))
                .andExpect(jsonPath("$.status").value(PregnancyStatus.ACTIVE.name()));
    }

    @Test
    void getPregnancyById_shouldReturn404_whenPregnancyDoesNotExist() throws Exception {
        Long farmId = 1L;
        String goatId = "GOAT-001";
        Long pregnancyId = 999L;

        when(queryUseCase.getPregnancyById(farmId, goatId, pregnancyId))
                .thenThrow(new ResourceNotFoundException("Pregnancy not found with id: " + pregnancyId));

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}",
                        farmId, goatId, pregnancyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPregnancyById_shouldReturn400_whenIdIsInvalid() throws Exception {
        Long farmId = 1L;
        String goatId = "GOAT-001";
        Long invalidId = 0L;

        when(queryUseCase.getPregnancyById(eq(farmId), eq(goatId), anyLong()))
                .thenThrow(new InvalidArgumentException("Invalid pregnancy ID"));

        mockMvc.perform(get("/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}",
                        farmId, goatId, invalidId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerBirth_shouldReturn201_whenPayloadIsValid() throws Exception {
        Long farmId = 1L;
        String goatId = "GOAT-001";
        Long pregnancyId = 10L;

        BirthRequestVO requestVO = BirthRequestVO.builder()
                .birthDate(LocalDate.of(2026, 3, 10))
                .kids(List.of())
                .build();
        BirthResponseVO responseVO = BirthResponseVO.builder().build();
        BirthResponseDTO responseDTO = BirthResponseDTO.builder()
                .pregnancy(PregnancyResponseDTO.builder().id(pregnancyId).build())
                .kids(List.of(BirthKidResponseDTO.builder().registrationNumber("KID-001").build()))
                .build();

        when(mapper.toBirthRequestVO(any(BirthRequestDTO.class))).thenReturn(requestVO);
        when(commandUseCase.registerBirth(farmId, goatId, pregnancyId, requestVO)).thenReturn(responseVO);
        when(mapper.toBirthResponseDTO(responseVO)).thenReturn(responseDTO);

        String payload = """
                {
                  "birthDate": "2026-03-10",
                  "kids": [
                    {
                      "registrationNumber": "KID-001",
                      "name": "Cria 1",
                      "gender": "FEMEA",
                      "breed": "SAANEN"
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}/births",
                        farmId, goatId, pregnancyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pregnancy.id").value(pregnancyId))
                .andExpect(jsonPath("$.kids[0].registrationNumber").value("KID-001"));
    }
}


