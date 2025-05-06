package com.devmaster.goatfarm.farm.api.controller;

import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.converter.GoatFarmDTOConverter;
import com.devmaster.goatfarm.farm.facade.GoatFarmFacade;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.converter.GoatDTOConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/goatfarms")
public class GoatFarmController {

    @Autowired
    private GoatFarmFacade farmFacade;

   // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<GoatFarmResponseDTO> createGoatFarm(@RequestBody GoatFarmRequestDTO requestDTO) {
        GoatFarmResponseVO responseVO = farmFacade.createGoatFarm(GoatFarmDTOConverter.toVO(requestDTO));
        return new ResponseEntity<>(GoatFarmDTOConverter.toDTO(responseVO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<GoatFarmResponseDTO> updateGoatFarm(@PathVariable Long id,
                                                              @RequestBody GoatFarmRequestDTO requestDTO) {
        GoatFarmResponseVO responseVO = farmFacade.updateGoatFarm(id, GoatFarmDTOConverter.toVO(requestDTO));
        return ResponseEntity.ok(GoatFarmDTOConverter.toDTO(responseVO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoatFarmResponseDTO> findGoatFarmById(@PathVariable Long id) {
        GoatFarmResponseVO responseVO = farmFacade.findGoatFarmById(id);
        return ResponseEntity.ok(GoatFarmDTOConverter.toDTO(responseVO));
    }

    @GetMapping(value = "/name")
    public ResponseEntity<Page<GoatFarmResponseDTO>> searchGoatFarmByName(
            @RequestParam(value = "name", defaultValue = "") String name,
            @PageableDefault(size = 12, page = 0) Pageable pageable ) {

        return ResponseEntity.ok(farmFacade.searchGoatFarmByName(name, pageable)

                .map(GoatFarmDTOConverter::toDTO));

    }

    @GetMapping
    public ResponseEntity<Page<GoatFarmResponseDTO>> findAllGoatFarm(@PageableDefault(size = 12, page = 0)
                                                                         Pageable pageable) {
        return ResponseEntity.ok(farmFacade.findAllGoatFarm(pageable)

                .map(GoatFarmDTOConverter::toDTO));

    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoatFarm(@PathVariable Long id) {
        farmFacade.deleteGoatFarm(id);
        return ResponseEntity.noContent().build();
    }


    // Método para buscar as cabras de uma fazenda
    @GetMapping("/{farmId}/goats")
    public ResponseEntity<Page<GoatResponseDTO>> findGoatsByFarmId(
            @PathVariable Long farmId,
            @RequestParam(value = "registrationNumber", required = false) String registrationNumber, Pageable pageable) {

        // Buscar as cabras usando o facade, que retorna GoatResponseVO
        Page<GoatResponseVO> goatsVO = farmFacade.findGoatsByFarmIdAndRegistrationNumber(farmId,
                registrationNumber,
                pageable);

        // Converter os GoatResponseVO para GoatResponseDTO
        Page<GoatResponseDTO> goatsDTO = goatsVO
                .map(GoatDTOConverter::toResponseDTO); // Aqui você pode fazer a conversão diretamente


        return ResponseEntity.ok(goatsDTO); // Retorna os DTOs
    }
}