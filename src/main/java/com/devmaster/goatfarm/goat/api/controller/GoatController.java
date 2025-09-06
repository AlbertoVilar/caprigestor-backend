package com.devmaster.goatfarm.goat.api.controller;

import com.devmaster.goatfarm.goat.api.dto.GoatRequestDTO;
import com.devmaster.goatfarm.goat.api.dto.GoatResponseDTO;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.converter.GoatDTOConverter;
import com.devmaster.goatfarm.goat.facade.GoatFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/goatfarms")
@Tag(name = "Goat API", description = "Gerenciamento de cabras na fazenda") // Adicionado Tag para o Swagger
public class GoatController {

    private final GoatFacade goatFacade;

    @Autowired
    public GoatController(GoatFacade goatFacade) {
        this.goatFacade = goatFacade;
    }

    /**
     * Endpoint de depuração para verificar as permissões do usuário logado.
     * @return ResponseEntity com as autoridades reconhecidas ou status 403.
     */
    @Operation(summary = "Verifica as permissões do usuário logado",
            description = "Endpoint interno para depuração de autoridades de segurança.",
            hidden = true) // hidden = true para não aparecer na documentação principal
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autoridades reconhecidas com sucesso."),
            @ApiResponse(responseCode = "403", description = "Nenhuma autenticação ativa.")
    })
    @GetMapping("/debug")
    public ResponseEntity<String> debugAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Nenhuma autenticação ativa.");
        }
        return ResponseEntity.ok("Authorities reconhecidas: " + authentication.getAuthorities());
    }

    /**
     * Cadastra uma nova cabra no sistema.
     * @param goatRequestDTO Objeto DTO com os dados da cabra a ser criada.
     * @return ResponseEntity com o GoatResponseDTO da cabra criada.
     */
    @Operation(summary = "Cadastra uma nova cabra no sistema",
            description = "Cria um novo registro de cabra, associando-a a um proprietário e fazenda existentes.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cabra cadastrada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou número de registro já existe."),
            @ApiResponse(responseCode = "404", description = "Proprietário ou fazenda não encontrados.")
    })
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @PostMapping("/goats") // POST /goatfarms/goats
    public ResponseEntity<GoatResponseDTO> createGoat(
            @Parameter(description = "Dados da cabra para cadastro", required = true)
            @Valid @RequestBody GoatRequestDTO goatRequestDTO) {
        GoatRequestVO requestVO = GoatDTOConverter.toRequestVO(goatRequestDTO);
        Long farmId = goatRequestDTO.getFarmId();
        Long ownerId = goatRequestDTO.getOwnerId();
        return ResponseEntity.ok(GoatDTOConverter.toResponseDTO(
                goatFacade.createGoat(requestVO, ownerId, farmId)));
    }

    /**
     * Atualiza os dados de uma cabra existente.
     * @param registrationNumber Número de registro da cabra a ser atualizada.
     * @param goatRequestDTO Objeto DTO com os novos dados da cabra.
     * @return ResponseEntity com o GoatResponseDTO da cabra atualizada.
     */
    @Operation(summary = "Atualiza os dados de uma cabra existente",
            description = "Modifica os dados de uma cabra específica pelo seu número de registro.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cabra atualizada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos."),
            @ApiResponse(responseCode = "404", description = "Cabra não encontrada.")
    })
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_OPERATOR')")
    @PutMapping("/goats/{registrationNumber}") // PUT /goatfarms/goats/{registrationNumber}
    public ResponseEntity<GoatResponseDTO> updateGoat(
            @Parameter(description = "Número de registro da cabra", example = "2114517012", required = true)
            @PathVariable String registrationNumber,
            @Parameter(description = "Novos dados da cabra", required = true)
            @Valid @RequestBody GoatRequestDTO goatRequestDTO) {
        GoatRequestVO requestVO = GoatDTOConverter.toRequestVO(goatRequestDTO);
        return ResponseEntity.ok(GoatDTOConverter.toResponseDTO(
                goatFacade.updateGoat(registrationNumber, requestVO)));
    }

    /**
     * Busca paginada por cabras usando parte do nome (sem especificar fazenda).
     * @param name Nome ou parte do nome da cabra.
     * @param pageable Objeto Pageable para controle de paginação.
     * @return ResponseEntity com uma página de GoatResponseDTOs.
     */
    @Operation(summary = "Busca paginada por cabras usando parte do nome",
            description = "Retorna uma lista paginada de cabras que contêm o nome especificado, sem filtro por fazenda.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Nenhuma cabra encontrada com o nome especificado.")
    })
    @GetMapping("/goats/name") // GET /goatfarms/goats/name
    public ResponseEntity<Page<GoatResponseDTO>> searchGoatByName(
            @Parameter(description = "Nome ou parte do nome da cabra", example = "NAIDE")
            @RequestParam(defaultValue = "") String name,
            @Parameter(hidden = true)
            @PageableDefault(size = 12) Pageable pageable) { // Tamanho padrão da página definido para 12
        return ResponseEntity.ok(goatFacade.searchGoatByName(name, pageable)
                .map(GoatDTOConverter::toResponseDTO));
    }

    /**
     * Busca todas as cabras de um capril por ID, com filtro opcional por nome ou número de registro.
     * @param farmId ID do capril.
     * @param registrationNumber Número de registro da cabra (opcional).
     * @param name Nome ou parte do nome da cabra (opcional).
     * @param pageable Objeto Pageable para controle de paginação.
     * @return ResponseEntity com uma página de GoatResponseDTOs.
     */
    @Operation(summary = "Busca cabras por fazenda e filtros opcionais",
            description = "Retorna uma lista paginada de cabras de uma fazenda específica, " +
                    "com a opção de filtrar por número de registro ou nome.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Fazenda não encontrada ou nenhuma cabra encontrada para os critérios.")
    })
    @GetMapping("/{farmId}/goats")
    public ResponseEntity<Page<GoatResponseDTO>> findGoatsByFarmId(
            @Parameter(description = "ID do capril", example = "1", required = true)
            @PathVariable Long farmId,
            @Parameter(description = "Número de registro da cabra", example = "2114517012")
            @RequestParam(value = "registrationNumber", required = false) String registrationNumber,
            @Parameter(description = "Nome ou parte do nome da cabra", example = "NAIDE")
            @RequestParam(value = "name", required = false) String name,
            @PageableDefault(size = 12) Pageable pageable) { // Tamanho padrão da página definido para 12

        Page<GoatResponseVO> goatsVO;

        // Prioriza a busca por nome, se informado
        if (name != null && !name.isBlank()) {
            goatsVO = goatFacade.findGoatsByNameAndFarmId(farmId, name, pageable);
        }
        // Senão, busca por número de registro (se informado)
        else if (registrationNumber != null && !registrationNumber.isBlank()) {
            goatsVO = goatFacade.findGoatsByFarmIdAndRegistrationNumber(farmId, registrationNumber, pageable);
        }
        // Se nenhum parâmetro for informado, retorna todos da fazenda
        else {
            goatsVO = goatFacade.findGoatsByFarmIdAndRegistrationNumber(farmId, null, pageable);
        }

        Page<GoatResponseDTO> goatsDTO = goatsVO.map(GoatDTOConverter::toResponseDTO);
        return ResponseEntity.ok(goatsDTO);
    }

    /**
     * Busca uma cabra pelo seu número de registro exato.
     * @param registrationNumber Número de registro da cabra.
     * @return ResponseEntity com o GoatResponseDTO da cabra encontrada.
     */
    @Operation(summary = "Busca uma cabra pelo número de registro",
            description = "Retorna os detalhes de uma cabra específica usando seu número de registro exato.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cabra encontrada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Cabra não encontrada.")
    })
    @GetMapping("/goats/registration/{registrationNumber}") // GET /goatfarms/goats/registration/{registrationNumber}
    public ResponseEntity<GoatResponseDTO> findByRegistrationNumber(
            @Parameter(description = "Número de registro da cabra", example = "2114517012", required = true)
            @PathVariable String registrationNumber) {
        return ResponseEntity.ok(GoatDTOConverter.toResponseDTO(
                goatFacade.findGoatByRegistrationNumber(registrationNumber)));
    }

    /**
     * Remove uma cabra do sistema pelo seu número de registro.
     * @param registrationNumber Número de registro da cabra a ser removida.
     * @return ResponseEntity sem conteúdo (status 204 No Content).
     */
    @Operation(summary = "Remove uma cabra do sistema",
            description = "Exclui uma cabra do sistema pelo seu número de registro. " +
                    "A cabra não pode estar referenciada por outro animal.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cabra removida com sucesso."),
            @ApiResponse(responseCode = "404", description = "Cabra não encontrada."),
            @ApiResponse(responseCode = "409", description = "Conflito: Cabra referenciada por outro animal.") // 409 Conflict para DatabaseException
    })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/goats/{registrationNumber}") // DELETE /goatfarms/goats/{registrationNumber}
    public ResponseEntity<Void> deleteGoat(
            @Parameter(description = "Número de registro da cabra a ser removida", example = "2114517012", required = true)
            @PathVariable String registrationNumber) {
        goatFacade.deleteGoatByRegistrationNumber(registrationNumber);
        return ResponseEntity.noContent().build();
    }


}