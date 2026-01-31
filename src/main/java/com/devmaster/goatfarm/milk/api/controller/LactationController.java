package com.devmaster.goatfarm.milk.api.controller;

import com.devmaster.goatfarm.milk.application.ports.in.LactationCommandUseCase;
import com.devmaster.goatfarm.milk.application.ports.in.LactationQueryUseCase;
import com.devmaster.goatfarm.milk.api.dto.LactationRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationDryRequestDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationResponseDTO;
import com.devmaster.goatfarm.milk.api.dto.LactationSummaryResponseDTO;
import com.devmaster.goatfarm.milk.business.bo.LactationRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationDryRequestVO;
import com.devmaster.goatfarm.milk.business.bo.LactationResponseVO;
import com.devmaster.goatfarm.milk.business.bo.LactationSummaryResponseVO;
import com.devmaster.goatfarm.milk.api.mapper.LactationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goatfarms/{farmId}/goats/{goatId}/lactations")
@Tag(name = "Lactation API", description = "Gestão de lactações de cabras na fazenda")
public class LactationController {

    private final LactationQueryUseCase lactationQueryUseCase;
    private final LactationCommandUseCase lactationCommandUseCase;
    private final LactationMapper lactationMapper;

    public LactationController(LactationQueryUseCase lactationQueryUseCase, LactationCommandUseCase lactationCommandUseCase, LactationMapper lactationMapper) {
        this.lactationQueryUseCase = lactationQueryUseCase;
        this.lactationCommandUseCase = lactationCommandUseCase;
        this.lactationMapper = lactationMapper;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PostMapping
    @Operation(summary = "Abre uma nova lactação para uma cabra")
    public ResponseEntity<LactationResponseDTO> openLactation(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Valid @RequestBody LactationRequestDTO request) {
        LactationRequestVO requestVO = lactationMapper.toRequestVO(request);
        LactationResponseVO responseVO = lactationCommandUseCase.openLactation(farmId, goatId, requestVO);
        return ResponseEntity.status(HttpStatus.CREATED).body(lactationMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping("/active")
    @Operation(summary = "Busca lactação ativa de uma cabra")
    public ResponseEntity<LactationResponseDTO> getActiveLactation(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId) {
        LactationResponseVO responseVO = lactationQueryUseCase.getActiveLactation(farmId, goatId);
        return ResponseEntity.ok(lactationMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping("/active/summary")
    @Operation(summary = "Busca sumário da lactação ativa da cabra")
    public ResponseEntity<LactationSummaryResponseDTO> getActiveLactationSummary(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId) {
        LactationSummaryResponseVO responseVO = lactationQueryUseCase.getActiveLactationSummary(farmId, goatId);
        return ResponseEntity.ok(lactationMapper.toSummaryResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @PatchMapping("/{lactationId}/dry")
    @Operation(summary = "Marca uma lactação como seca")
    public ResponseEntity<LactationResponseDTO> dryLactation(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Identificador da lactação") @PathVariable Long lactationId,
            @Valid @RequestBody LactationDryRequestDTO request) {
        LactationDryRequestVO requestVO = lactationMapper.toDryRequestVO(request);
        LactationResponseVO responseVO = lactationCommandUseCase.dryLactation(farmId, goatId, lactationId, requestVO);
        return ResponseEntity.ok(lactationMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping("/{lactationId}")
    @Operation(summary = "Busca lactação por ID")
    public ResponseEntity<LactationResponseDTO> getLactationById(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Identificador da lactação") @PathVariable Long lactationId) {
        LactationResponseVO responseVO = lactationQueryUseCase.getLactationById(farmId, goatId, lactationId);
        return ResponseEntity.ok(lactationMapper.toResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping("/{lactationId}/summary")
    @Operation(summary = "Busca sumário da lactação por ID")
    public ResponseEntity<LactationSummaryResponseDTO> getLactationSummary(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @Parameter(description = "Identificador da lactação") @PathVariable Long lactationId) {
        LactationSummaryResponseVO responseVO = lactationQueryUseCase.getLactationSummary(farmId, goatId, lactationId);
        return ResponseEntity.ok(lactationMapper.toSummaryResponseDTO(responseVO));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN') or ((hasAuthority('ROLE_OPERATOR') or hasAuthority('ROLE_FARM_OWNER')) and @ownershipService.isFarmOwner(#farmId))")
    @GetMapping
    @Operation(summary = "Lista histórico de lactações de uma cabra")
    public ResponseEntity<Page<LactationResponseDTO>> getAllLactations(
            @Parameter(description = "Identificador da fazenda") @PathVariable Long farmId,
            @Parameter(description = "Identificador da cabra") @PathVariable String goatId,
            @PageableDefault(sort = "startDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<LactationResponseVO> pageVO = lactationQueryUseCase.getAllLactations(farmId, goatId, pageable);
        // Map Page<VO> to Page<DTO> manually since MapStruct doesn't support Page mapping out of the box easily without wrapper
        Page<LactationResponseDTO> pageDTO = pageVO.map(lactationMapper::toResponseDTO);
        return ResponseEntity.ok(pageDTO);
    }
}
