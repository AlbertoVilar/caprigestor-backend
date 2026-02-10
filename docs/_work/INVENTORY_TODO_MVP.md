# Inventory MVP - Backlog executavel
Ultima atualizacao: 2026-02-10
Escopo: plano incremental para implementar o modulo inventory sem quebrar arquitetura e contratos existentes.
Links relacionados: [Modulo Inventory](../02-modules/INVENTORY_MODULE.md), [ADR-002](../01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md)

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
- `InventoryBoundaryArchUnitTest` (ou equivalente) inicial para blindar fronteira de contexto.

### DoD
- Pacotes criados com naming consistente ao repositorio.
- Interfaces `ports/in` e `ports/out` revisadas e compilando.

### Risco / mitigacao
- Risco: criar acoplamento prematuro com outros contextos.
- Mitigacao: integrar apenas por `sourceModule/sourceRef`.

## Etapa 2 - Especificacao de esquema Flyway e entidades JPA
### Objetivo
- Materializar schema de `inventory_item`, `inventory_lot`, `stock_movement`, `stock_balance` com constraints e indices.

### Arquivos/classes afetadas (paths)
- `src/main/resources/db/migration/V23__create_inventory_core_tables.sql`
- `src/main/resources/db/migration/V24__create_inventory_indexes.sql`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/entity/InventoryItem.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/entity/InventoryLot.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/entity/StockMovement.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/entity/StockBalance.java`

### Testes esperados
- Testes de repository para constraints unicas e consultas principais.

### DoD
- Schema com `uk_stock_movement_farm_idempotency`.
- Constraint de quantidade positiva ativa.
- Indices para consultas de alerta e historico presentes.

### Risco / mitigacao
- Risco: colisoes de nomes/indices com migrations futuras.
- Mitigacao: prefixo consistente `inventory_` e revisao de naming.

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
- `OUT` sem saldo suficiente retorna `422`;
- `ADJUST` respeita regras de sinal.

### DoD
- Movimentos gravados em ledger e refletidos em `stock_balance` na mesma transacao.
- Sem regressao em padrao de excecoes (`InvalidArgumentException`, `BusinessRuleException`).

### Risco / mitigacao
- Risco: condicao de corrida em baixa simultanea.
- Mitigacao: lock de saldo (`select for update`) e teste concorrente dedicado.

## Etapa 4 - Idempotencia de movimentos
### Objetivo
- Garantir que retries nao criem movimentos duplicados.

### Arquivos/classes afetadas (paths)
- `src/main/java/com/devmaster/goatfarm/inventory/business/InventoryMovementBusiness.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/repository/StockMovementRepository.java`
- `src/main/java/com/devmaster/goatfarm/inventory/business/bo/MovementIdempotencyResultVO.java`

### Testes esperados
- `InventoryIdempotencyIntegrationTest`:
- mesmo `idempotencyKey` e mesmo payload retorna resposta idempotente;
- mesmo `idempotencyKey` e payload diferente retorna `409`.

### DoD
- `idempotencyKey` validada por fazenda.
- comportamento documentado em contrato REST.

### Risco / mitigacao
- Risco: chave curta/nao padronizada gerar colisoes.
- Mitigacao: guideline de formato no endpoint e validacao minima de tamanho.

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
- `trackLot=true` exige fluxo de lote para saidas.
- unicidade de nome por fazenda e lote por item.

### Risco / mitigacao
- Risco: itens inconsistentes entre unidades.
- Mitigacao: valida unidade permitida no momento do cadastro.

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
- paginacao.

### DoD
- `low-stock` e `expiring` com SLA de consulta aceitavel.
- resposta aderente aos padroes de `health/milk/reproduction`.

### Risco / mitigacao
- Risco: alerta de expiring sem normalizar timezone/data.
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

## Etapa 9 - Documentacao e gates finais
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
- `./mvnw.cmd -Dtest=MilkReproductionBoundaryArchUnitTest test`
- `./mvnw.cmd -Dtest=InventoryBoundaryArchUnitTest test`
- `./mvnw.cmd test` (suite completa)

### DoD
- docs sem link quebrado e sem protocolo local.
- nenhum acoplamento indevido detectado nos checks de arquitetura.

### Risco / mitigacao
- Risco: docs divergirem do contrato implementado.
- Mitigacao: PR checklist obrigatorio de contrato x controller x teste.
