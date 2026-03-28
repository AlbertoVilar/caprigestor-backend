package com.devmaster.goatfarm.commercial.api.controller;

import com.devmaster.goatfarm.commercial.api.dto.AnimalSaleRequestDTO;
import com.devmaster.goatfarm.commercial.api.dto.AnimalSaleResponseDTO;
import com.devmaster.goatfarm.commercial.api.dto.CommercialSummaryDTO;
import com.devmaster.goatfarm.commercial.api.dto.CustomerRequestDTO;
import com.devmaster.goatfarm.commercial.api.dto.CustomerResponseDTO;
import com.devmaster.goatfarm.commercial.api.dto.MilkSaleRequestDTO;
import com.devmaster.goatfarm.commercial.api.dto.MilkSaleResponseDTO;
import com.devmaster.goatfarm.commercial.api.dto.ReceivableResponseDTO;
import com.devmaster.goatfarm.commercial.api.dto.SalePaymentRequestDTO;
import com.devmaster.goatfarm.commercial.api.mapper.CommercialApiMapper;
import com.devmaster.goatfarm.commercial.application.ports.in.CommercialUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/goatfarms/{farmId}/commercial", "/api/goatfarms/{farmId}/commercial"})
@Tag(name = "Commercial API", description = "Camada comercial minima por fazenda. Caminho canonico /api/v1; legado /api em descontinuacao.")
public class CommercialController {

    private final CommercialUseCase commercialUseCase;
    private final CommercialApiMapper commercialApiMapper;

    public CommercialController(CommercialUseCase commercialUseCase, CommercialApiMapper commercialApiMapper) {
        this.commercialUseCase = commercialUseCase;
        this.commercialApiMapper = commercialApiMapper;
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @PostMapping("/customers")
    @Operation(summary = "Cadastrar cliente/comprador")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado com sucesso."),
            @ApiResponse(responseCode = "422", description = "Falha de validacao.")
    })
    public ResponseEntity<CustomerResponseDTO> createCustomer(@PathVariable Long farmId, @Valid @RequestBody CustomerRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commercialApiMapper.toDTO(commercialUseCase.createCustomer(farmId, commercialApiMapper.toVO(requestDTO))));
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @GetMapping("/customers")
    @Operation(summary = "Listar clientes/compradores")
    public ResponseEntity<List<CustomerResponseDTO>> listCustomers(@PathVariable Long farmId) {
        return ResponseEntity.ok(commercialUseCase.listCustomers(farmId).stream().map(commercialApiMapper::toDTO).toList());
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @PostMapping("/animal-sales")
    @Operation(summary = "Registrar venda de animal")
    public ResponseEntity<AnimalSaleResponseDTO> createAnimalSale(@PathVariable Long farmId, @Valid @RequestBody AnimalSaleRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commercialApiMapper.toDTO(commercialUseCase.createAnimalSale(farmId, commercialApiMapper.toVO(requestDTO))));
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @GetMapping("/animal-sales")
    @Operation(summary = "Listar vendas de animais")
    public ResponseEntity<List<AnimalSaleResponseDTO>> listAnimalSales(@PathVariable Long farmId) {
        return ResponseEntity.ok(commercialUseCase.listAnimalSales(farmId).stream().map(commercialApiMapper::toDTO).toList());
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @PatchMapping("/animal-sales/{saleId}/payment")
    @Operation(summary = "Marcar venda de animal como paga")
    public ResponseEntity<AnimalSaleResponseDTO> registerAnimalSalePayment(@PathVariable Long farmId, @PathVariable Long saleId, @Valid @RequestBody SalePaymentRequestDTO requestDTO) {
        return ResponseEntity.ok(commercialApiMapper.toDTO(commercialUseCase.registerAnimalSalePayment(farmId, saleId, commercialApiMapper.toVO(requestDTO))));
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @PostMapping("/milk-sales")
    @Operation(summary = "Registrar venda de leite")
    public ResponseEntity<MilkSaleResponseDTO> createMilkSale(@PathVariable Long farmId, @Valid @RequestBody MilkSaleRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commercialApiMapper.toDTO(commercialUseCase.createMilkSale(farmId, commercialApiMapper.toVO(requestDTO))));
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @GetMapping("/milk-sales")
    @Operation(summary = "Listar vendas de leite")
    public ResponseEntity<List<MilkSaleResponseDTO>> listMilkSales(@PathVariable Long farmId) {
        return ResponseEntity.ok(commercialUseCase.listMilkSales(farmId).stream().map(commercialApiMapper::toDTO).toList());
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @PatchMapping("/milk-sales/{saleId}/payment")
    @Operation(summary = "Marcar venda de leite como paga")
    public ResponseEntity<MilkSaleResponseDTO> registerMilkSalePayment(@PathVariable Long farmId, @PathVariable Long saleId, @Valid @RequestBody SalePaymentRequestDTO requestDTO) {
        return ResponseEntity.ok(commercialApiMapper.toDTO(commercialUseCase.registerMilkSalePayment(farmId, saleId, commercialApiMapper.toVO(requestDTO))));
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @GetMapping("/receivables")
    @Operation(summary = "Listar recebiveis abertos e pagos")
    public ResponseEntity<List<ReceivableResponseDTO>> listReceivables(@PathVariable Long farmId) {
        return ResponseEntity.ok(commercialUseCase.listReceivables(farmId).stream().map(commercialApiMapper::toDTO).toList());
    }

    @PreAuthorize("@ownershipService.canManageFarm(#farmId)")
    @GetMapping("/summary")
    @Operation(summary = "Resumo comercial minimo da fazenda")
    public ResponseEntity<CommercialSummaryDTO> getSummary(@PathVariable Long farmId) {
        return ResponseEntity.ok(commercialApiMapper.toDTO(commercialUseCase.getSummary(farmId)));
    }
}
