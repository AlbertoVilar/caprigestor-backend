package com.devmaster.goatfarm.goat.api.controller;

import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.converter.GoatFarmDTOConverter;
import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.goat.converter.GoatDTOConverter;
import com.devmaster.goatfarm.goat.facade.GoatFacade;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;  // Corrigido para OwnerResponseVO
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/goats")
public class GoatController {

    @Autowired
    private GoatFacade goatFacade;

    // CREATE
    @PostMapping
    public GoatResponseDTO createGoat(@Valid @RequestBody GoatRequestDTO goatRequestDTO) {
        GoatRequestVO requestVO = GoatDTOConverter.toRequestVO(goatRequestDTO);
        Long farmId = goatRequestDTO.getFarmId(); // Extrai o ID da fazenda do DTO
        Long ownerId = goatRequestDTO.getOwnerId(); // Extrai o ID do proprietário do DTO

        return GoatDTOConverter.toResponseDTO(goatFacade.createGoat(requestVO, ownerId, farmId));
    }


    // UPDATE
    @PutMapping(value = "/{registrationNumber}")
    public GoatResponseDTO updateGoat(@PathVariable String registrationNumber,
                                      @RequestBody GoatRequestDTO goatRequestDTO) {
        GoatRequestVO requestVO = GoatDTOConverter.toRequestVO(goatRequestDTO);
        return GoatDTOConverter.toResponseDTO(goatFacade.updateGoat(registrationNumber, requestVO));
    }

    // READ
    @GetMapping
    public List<GoatResponseDTO> findAllGoats() {
        return goatFacade.findAllGoats().stream()
                .map(GoatDTOConverter::toResponseDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{registrationNumber}")
    public ResponseEntity<GoatResponseDTO> findByRegistrationNumber(@PathVariable String registrationNumber) {
        return ResponseEntity.ok(GoatDTOConverter.toResponseDTO(
                goatFacade.findGoatByRegistrationNumber(registrationNumber)));
    }

    // DELETE
    @DeleteMapping("/{registrationNumber}")
    public void deleteGoat(@PathVariable String registrationNumber) {
        goatFacade.deleteGoatByRegistrationNumber(registrationNumber);
    }

    // Método utilitário privado para extrair o VO da fazenda a partir do GoatRequestDTO
    private GoatFarmRequestVO extractFarmRequestVO(GoatRequestDTO goatRequestDTO) {
        GoatFarmRequestDTO farmRequestDTO = GoatFarmDTOConverter.fromGoatRequestDTO(goatRequestDTO);
        return GoatFarmDTOConverter.toVO(farmRequestDTO);
    }

    private OwnerResponseVO extractOwnerResponseVO(GoatRequestDTO goatRequestDTO) {
        return OwnerResponseVO.builder()
                .id(goatRequestDTO.getOwnerId())
                .build();
    }
}
