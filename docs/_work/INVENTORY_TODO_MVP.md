# Inventory MVP - Backlog executavel
Ultima atualizacao: 2026-02-10
Escopo: plano incremental para implementar o modulo inventory sem quebrar arquitetura e contratos existentes.
Links relacionados: [Modulo Inventory](../02-modules/INVENTORY_MODULE.md), [ADR-002](../01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md)

## Etapa 0 - Congelar escopo do MVP
### Objetivo
- Confirmar e registrar escopo fechado do MVP para evitar creep.

### Arquivos/classes afetadas (paths)
- `docs/02-modules/INVENTORY_MODULE.md`
- `docs/01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md`

### Testes esperados
- Revisao de arquitetura aprovada (sem codigo).

### DoD
- MVP fechado com in/out claro:
- entra: itens + lotes + movimentos + saldo + alertas;
- nao entra: purchases/sales/finance/feeding.

### Risco / mitigacao
- Risco: incluir funcionalidades financeiras no meio da entrega.
- Mitigacao: PR checklist com item explicito de escopo.

## Etapa 1 - Estrutura de pacote e contratos in/out
### Objetivo
- Criar estrutura hexagonal do modulo `inventory` e contratos iniciais de uso/persistencia.

### Arquivos/classes afetadas (paths)
- `src/main/java/com/devmaster/goatfarm/inventory/api/controller/`
- `src/main/java/com/devmaster/goatfarm/inventory/api/dto/`
- `src/main/java/com/devmaster/goatfarm/inventory/api/mapper/`
- `src/main/java/com/devmaster/goatfarm/inventory/application/ports/in/`
- `src/main/java/com/devmaster/goatfarm/inventory/application/ports/out/`
- `src/main/java/com/devmaster/goatfarm/inventory/business/`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/`

### Testes esperados
- `InventoryBoundaryArchUnitTest` inicial para blindar fronteira de contexto.

### DoD
- Pacotes criados com naming consistente ao repositorio.
- Interfaces `ports/in` e `ports/out` revisadas e compilando.

### Risco / mitigacao
- Risco: criar acoplamento prematuro com outros contextos.
- Mitigacao: integrar apenas por `sourceModule/sourceRef`.

## Etapa 2 - Especificacao de schema Flyway e entidades JPA
### Objetivo
- Materializar schema de `inventory_item`, `inventory_lot`, `stock_movement`, `stock_balance` com constraints e indices.

### Arquivos/classes afetadas (paths)
- `src/main/resources/db/migration/V23__create_inventory_core_tables.sql`
- `src/main/resources/db/migration/V24__create_inventory_indexes.sql`
- `src/main/resources/db/migration/V25__create_inventory_balance_partial_unique.sql`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/entity/InventoryItem.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/entity/InventoryLot.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/entity/StockMovement.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/entity/StockBalance.java`

### Testes esperados
- Testes de repository para constraints unicas e consultas principais.

### DoD
- `inventory_item (farm_id, name_normalized)` unico.
- `inventory_lot (item_id, lot_code)` unico.
- `stock_movement (farm_id, idempotency_key)` unico.
- `stock_balance` com unicidade por (`farm_id`, `item_id`, `lot_id`) + garantia para `lot_id null`.
- Constraint de quantidade positiva ativa.

### Risco / mitigacao
- Risco: divergencia de semantica para `lot_id null` no banco.
- Mitigacao: usar indice parcial dedicado e testes de concorrencia de criacao de saldo.

## Etapa 3 - Regras de negocio core (ledger + balance)
### Objetivo
- Implementar comandos de movimento com transacao unica e regra de saldo nao negativo.

### Arquivos/classes afetadas (paths)
- `src/main/java/com/devmaster/goatfarm/inventory/business/InventoryMovementBusiness.java`
- `src/main/java/com/devmaster/goatfarm/inventory/application/ports/in/InventoryMovementUseCase.java`
- `src/main/java/com/devmaster/goatfarm/inventory/application/ports/out/StockMovementPersistencePort.java`
- `src/main/java/com/devmaster/goatfarm/inventory/application/ports/out/StockBalancePersistencePort.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/adapter/StockMovementPersistenceAdapter.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/adapter/StockBalancePersistenceAdapter.java`

### Testes esperados
- `InventoryMovementBusinessTest`:
- `IN` aumenta saldo;
- `OUT` reduz saldo;
- `ADJUST` respeita `adjustDirection`;
- `OUT` e `ADJUST` decremento sem saldo suficiente retornam `422`.

### DoD
- Movimentos gravados em ledger e refletidos em `stock_balance` na mesma transacao.
- `quantity` sempre positiva.
- `trackLot=true` exige `lotId` para `IN`, `OUT` e `ADJUST`.
- Sem regressao em padrao de excecoes (`InvalidArgumentException`, `BusinessRuleException`).

### Risco / mitigacao
- Risco: condicao de corrida em baixa simultanea.
- Mitigacao: lock pessimista com ordem fixa (saldo item, depois saldo lote).

## Etapa 4 - Idempotencia de movimentos
### Objetivo
- Garantir que retries nao criem movimentos duplicados.

### Arquivos/classes afetadas (paths)
- `src/main/java/com/devmaster/goatfarm/inventory/business/InventoryMovementBusiness.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/repository/StockMovementRepository.java`
- `src/main/java/com/devmaster/goatfarm/inventory/api/controller/InventoryMovementController.java`
- `src/main/java/com/devmaster/goatfarm/inventory/business/bo/MovementIdempotencyResultVO.java`

### Testes esperados
- `InventoryIdempotencyIntegrationTest`:
- `Idempotency-Key` igual + payload igual retorna replay (`200`);
- `Idempotency-Key` igual + payload diferente retorna `409`;
- header ausente retorna `400`.

### DoD
- `Idempotency-Key` obrigatoria no header de `POST /movements`.
- Hash do payload persistido para diferenciar replay valido de conflito.
- comportamento documentado em contrato REST.

### Risco / mitigacao
- Risco: chave curta/nao padronizada gerar colisoes.
- Mitigacao: guideline de formato e validacao minima de tamanho no controller.

## Etapa 5 - CRUD de item e lote
### Objetivo
- Implementar ciclo de vida de item e lote com validacoes de rastreabilidade.

### Arquivos/classes afetadas (paths)
- `src/main/java/com/devmaster/goatfarm/inventory/business/InventoryItemBusiness.java`
- `src/main/java/com/devmaster/goatfarm/inventory/business/InventoryLotBusiness.java`
- `src/main/java/com/devmaster/goatfarm/inventory/api/controller/InventoryItemController.java`
- `src/main/java/com/devmaster/goatfarm/inventory/api/controller/InventoryLotController.java`

### Testes esperados
- `InventoryItemBusinessTest`
- `InventoryLotBusinessTest`
- `InventoryItemControllerTest`

### DoD
- `trackLot=true` exige lote para consumo/ajuste.
- `expiresAt` validado contra data de entrada.
- unicidade de nome por fazenda e lote por item.

### Risco / mitigacao
- Risco: itens inconsistentes entre unidades.
- Mitigacao: validar unidade permitida no momento do cadastro.

## Etapa 6 - Endpoints de saldo e historico
### Objetivo
- Expor leitura paginada de saldo e historico de movimentos farm-level.

### Arquivos/classes afetadas (paths)
- `src/main/java/com/devmaster/goatfarm/inventory/api/controller/InventoryStockController.java`
- `src/main/java/com/devmaster/goatfarm/inventory/api/controller/InventoryMovementController.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/repository/StockBalanceRepository.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/repository/StockMovementRepository.java`

### Testes esperados
- `InventoryMovementControllerIntegrationTest`
- `InventoryStockControllerIntegrationTest`

### DoD
- filtros por periodo, tipo, origem e item funcionando.
- leitura por item e por lote sem N+1.
- paginacao compativel com padrao global de `API_CONTRACTS`.

### Risco / mitigacao
- Risco: consultas pesadas sem indice adequado.
- Mitigacao: revisar plano de execucao e indices antes de merge.

## Etapa 7 - Alertas farm-level (low-stock e expiring)
### Objetivo
- Implementar alertas agregados para operacao diaria.

### Arquivos/classes afetadas (paths)
- `src/main/java/com/devmaster/goatfarm/inventory/business/InventoryAlertsBusiness.java`
- `src/main/java/com/devmaster/goatfarm/inventory/api/controller/FarmInventoryAlertsController.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/repository/InventoryAlertQueryRepository.java`

### Testes esperados
- `InventoryAlertsIntegrationTest` cobrindo:
- resposta `totalPending` + `alerts`;
- filtro de janela de expiracao;
- paginacao;
- severidade e ordenacao estavel.

### DoD
- `low-stock` e `expiring` com SLA de consulta aceitavel.
- severidade consistente (`HIGH`, `MEDIUM`, `LOW`).
- resposta aderente aos padroes de `health/milk/reproduction`.

### Risco / mitigacao
- Risco: alerta de expiracao sem normalizar timezone/data.
- Mitigacao: padronizar `LocalDate` e regra de corte diaria.

## Etapa 8 - Seguranca e ownership
### Objetivo
- Garantir que todos os endpoints inventory respeitem ownership padrao do projeto.

### Arquivos/classes afetadas (paths)
- `src/main/java/com/devmaster/goatfarm/inventory/api/controller/*.java`
- `src/test/java/com/devmaster/goatfarm/inventory/security/InventoryOwnershipIntegrationTest.java`

### Testes esperados
- cenarios `401`, `403`, `200/201` para owner/admin/operator vinculado.

### DoD
- todos os controllers usam `@PreAuthorize("@ownershipService.canManageFarm(#farmId)")`.
- sem endpoint inventory acessivel fora do escopo de fazenda.

### Risco / mitigacao
- Risco: inconsistencias com expressao antiga `isFarmOwner`.
- Mitigacao: padronizar expression e revisar code review checklist.

## Etapa 9 - Gates de arquitetura e checklist final
### Objetivo
- Fechar entrega com docs oficiais, contratos e guardrails.

### Arquivos/classes afetadas (paths)
- `docs/02-modules/INVENTORY_MODULE.md`
- `docs/03-api/API_CONTRACTS.md`
- `docs/00-overview/PROJECT_STATUS.md`
- `docs/00-overview/ROADMAP.md`
- `docs/INDEX.md`
- `src/test/java/com/devmaster/goatfarm/architecture/InventoryBoundaryArchUnitTest.java`

### Testes esperados
- `./mvnw.cmd -Dtest=HexagonalArchitectureGuardTest test`
- `./mvnw.cmd -Dtest=InventoryBoundaryArchUnitTest test`
- `./mvnw.cmd test` (suite completa)
- `rg -n "import com\\.devmaster\\.goatfarm\\.(health|milk|reproduction)\\." src/main/java/com/devmaster/goatfarm/inventory`

### DoD
- gate explicito: `inventory..` nao importa `health..`, `milk..`, `reproduction..` (exceto `sharedkernel..`).
- docs sem link quebrado e sem protocolo local.
- nenhum acoplamento indevido detectado nos checks de arquitetura.

### Risco / mitigacao
- Risco: docs divergirem do contrato implementado.
- Mitigacao: PR checklist obrigatorio de contrato x controller x teste.
