package com.devmaster.goatfarm.farm.api.controller;

import com.devmaster.goatfarm.farm.api.dto.GoatFarmFullResponseDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmRequestDTO;
import com.devmaster.goatfarm.farm.api.dto.GoatFarmResponseDTO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.converters.GoatFarmDTOConverter;
import com.devmaster.goatfarm.farm.facade.GoatFarmFacade;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.converter.GoatDTOConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goatfarms")
public class GoatFarmController {

    @Autowired
    private GoatFarmFacade farmFacade;

    //@CrossOrigin(origins = "http://localhost:5500")
    @Operation(summary = "Cadastra um novo capril")
    @PostMapping
    public ResponseEntity<GoatFarmResponseDTO> createGoatFarm(
            @RequestBody(description = "Dados do novo capril")
            @org.springframework.web.bind.annotation.RequestBody GoatFarmRequestDTO requestDTO) {

        GoatFarmResponseVO responseVO = farmFacade.createGoatFarm(GoatFarmDTOConverter.toVO(requestDTO));
        return new ResponseEntity<>(GoatFarmDTOConverter.toDTO(responseVO), HttpStatus.CREATED);
    }

    @Operation(summary = "Atualiza os dados de um capril existente")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<GoatFarmResponseDTO> updateGoatFarm(
            @Parameter(description = "ID do capril a ser atualizado", example = "1") @PathVariable Long id,
            @RequestBody(description = "Novos dados para o capril")
            @org.springframework.web.bind.annotation.RequestBody GoatFarmRequestDTO requestDTO) {

        GoatFarmResponseVO responseVO = farmFacade.updateGoatFarm(id, GoatFarmDTOConverter.toVO(requestDTO));
        return ResponseEntity.ok(GoatFarmDTOConverter.toDTO(responseVO));
    }

    @Operation(summary = "Busca um capril pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<GoatFarmResponseDTO> findGoatFarmById(
            @Parameter(description = "ID do capril", example = "1") @PathVariable Long id) {

        GoatFarmResponseVO responseVO = farmFacade.findGoatFarmById(id);
        return ResponseEntity.ok(GoatFarmDTOConverter.toDTO(responseVO));
    }

    @Operation(summary = "Busca paginada de capris pelo nome")
    @GetMapping("/name")
    public ResponseEntity<Page<GoatFarmResponseDTO>> searchGoatFarmByName(
            @Parameter(description = "Nome ou parte do nome do capril", example = "Capril Vilar")
            @RequestParam(value = "name", defaultValue = "") String name,

            @PageableDefault(size = 12, page = 0) Pageable pageable) {

        return ResponseEntity.ok(farmFacade.searchGoatFarmByName(name, pageable)
                .map(GoatFarmDTOConverter::toDTO));
    }

    @Operation(summary = "Lista todos os capris com paginação")
    @GetMapping
    public ResponseEntity<Page<GoatFarmFullResponseDTO>> findAllGoatFarm(
            @PageableDefault(size = 12, page = 0) Pageable pageable) {

        return ResponseEntity.ok(farmFacade.findAllGoatFarm(pageable)
                .map(GoatFarmDTOConverter::toFullDTO));
    }

    @Operation(summary = "Remove um capril pelo ID")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoatFarm(
            @Parameter(description = "ID do capril a ser removido", example = "1") @PathVariable Long id) {

        farmFacade.deleteGoatFarm(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Busca todas as cabras de um capril por ID")
    @GetMapping("/{farmId}/goats")
    public ResponseEntity<Page<GoatResponseDTO>> findGoatsByFarmId(
            @Parameter(description = "ID do capril", example = "1") @PathVariable Long farmId,

            @Parameter(description = "Filtro opcional por número de registro da cabra", example = "2114517012")
            @RequestParam(value = "registrationNumber", required = false) String registrationNumber,

            Pageable pageable) {

        Page<GoatResponseVO> goatsVO = farmFacade.findGoatsByFarmIdAndRegistrationNumber(farmId, registrationNumber, pageable);
        Page<GoatResponseDTO> goatsDTO = goatsVO.map(GoatDTOConverter::toResponseDTO);
        return ResponseEntity.ok(goatsDTO);
    }
}
