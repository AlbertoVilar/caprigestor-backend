package com.devmaster.goatfarm.goatownership.api.controller;

import com.devmaster.goatfarm.application.pagination.PageResult;
import com.devmaster.goatfarm.goatownership.api.dto.InternalOwnershipTransferRequestDTO;
import com.devmaster.goatfarm.goatownership.api.dto.OwnershipTransferResponseDTO;
import com.devmaster.goatfarm.goatownership.api.mapper.OwnershipTransferApiMapper;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferDirection;
import com.devmaster.goatfarm.goatownership.application.model.OwnershipTransferPageQuery;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipTransferQueryUseCase;
import com.devmaster.goatfarm.goatownership.application.ports.in.GoatOwnershipTransferUseCase;
import com.devmaster.goatfarm.goatownership.domain.OwnershipTransferStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Locale;

/** HTTP adapter for the approved INTERNAL_TRANSFER workflow. */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Ownership Transfer API", description = "Transferências internas entre fazendas.")
public class OwnershipTransferController {
    private final GoatOwnershipTransferUseCase transferUseCase;
    private final GoatOwnershipTransferQueryUseCase queryUseCase;
    private final OwnershipTransferApiMapper mapper;

    public OwnershipTransferController(GoatOwnershipTransferUseCase transferUseCase,
                                       GoatOwnershipTransferQueryUseCase queryUseCase,
                                       OwnershipTransferApiMapper mapper) {
        this.transferUseCase = transferUseCase;
        this.queryUseCase = queryUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/ownership-transfers")
    @Operation(summary = "Solicita transferência interna de uma cabra")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transferência solicitada."),
            @ApiResponse(responseCode = "401", description = "Autenticação obrigatória."),
            @ApiResponse(responseCode = "403", description = "Sem permissão para administrar a fazenda de origem."),
            @ApiResponse(responseCode = "404", description = "Cabra ou fazenda de destino não encontrada."),
            @ApiResponse(responseCode = "422", description = "Payload ou regra de negócio inválidos.")
    })
    public ResponseEntity<OwnershipTransferResponseDTO> request(
            @Valid @RequestBody InternalOwnershipTransferRequestDTO request) {
        var transfer = transferUseCase.requestInternalTransfer(mapper.toApplication(request));
        URI location = URI.create("/api/v1/ownership-transfers/" + transfer.id());
        return ResponseEntity.created(location).body(mapper.toResponse(transfer));
    }

    @GetMapping("/ownership-transfers/{transferId}")
    @Operation(summary = "Consulta uma transferência interna autorizada")
    public ResponseEntity<OwnershipTransferResponseDTO> findById(@PathVariable Long transferId) {
        return ResponseEntity.ok(mapper.toResponse(queryUseCase.findAuthorizedById(transferId)));
    }

    @PostMapping("/ownership-transfers/{transferId}/accept")
    @Operation(summary = "Aceita uma transferência interna")
    public ResponseEntity<OwnershipTransferResponseDTO> accept(@PathVariable Long transferId) {
        return ResponseEntity.ok(mapper.toResponse(transferUseCase.acceptTransfer(transferId)));
    }

    @PostMapping("/ownership-transfers/{transferId}/reject")
    @Operation(summary = "Rejeita uma transferência interna")
    public ResponseEntity<OwnershipTransferResponseDTO> reject(@PathVariable Long transferId) {
        return ResponseEntity.ok(mapper.toResponse(transferUseCase.rejectTransfer(transferId)));
    }

    @PostMapping("/ownership-transfers/{transferId}/cancel")
    @Operation(summary = "Cancela uma transferência interna")
    public ResponseEntity<OwnershipTransferResponseDTO> cancel(@PathVariable Long transferId) {
        return ResponseEntity.ok(mapper.toResponse(transferUseCase.cancelTransfer(transferId)));
    }

    @GetMapping("/goatfarms/{farmId}/ownership-transfers")
    @Operation(summary = "Lista a caixa de entrada ou saída de uma fazenda")
    public ResponseEntity<Page<OwnershipTransferResponseDTO>> listForFarm(
            @PathVariable Long farmId,
            @RequestParam(required = false) String direction,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        OwnershipTransferDirection parsedDirection = parseDirection(direction);
        OwnershipTransferStatus parsedStatus = parseStatus(status);
        PageResult<OwnershipTransferResponseDTO> result = queryUseCase
                .listForFarm(farmId, parsedDirection, parsedStatus, new OwnershipTransferPageQuery(page, size))
                .map(mapper::toResponse);
        return ResponseEntity.ok(new PageImpl<>(result.content(), PageRequest.of(result.page(), result.size()), result.totalElements()));
    }

    private OwnershipTransferDirection parseDirection(String value) {
        if (value == null || value.isBlank()) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException("direction is required");
        }
        try {
            return OwnershipTransferDirection.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException("direction must be INCOMING or OUTGOING");
        }
    }

    private OwnershipTransferStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OwnershipTransferStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException("status is invalid");
        }
    }
}
