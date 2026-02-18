# Inventory TODO MVP (Checklist de Engenharia)
Ultima atualizacao: 2026-02-18
Escopo: plano executavel do MVP de Inventory com status de execucao do ledger core.
Links relacionados: [Modulo Inventory](../02-modules/INVENTORY_MODULE.md), [ADR-002](../01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Portal](../INDEX.md)

## Status atual
- [x] Milestone 2 - Entidades, ports e regras de dominio do ledger core.
- [x] Milestone 3 - Consistencia e concorrencia (locks item -> balance, upsert, idempotencia persistida).
- [x] Milestone 4 (parcial) - `POST /inventory/movements` com ownership, validacao e Swagger.
- [x] Gate de fronteira - `InventoryBoundaryArchUnitTest` ativo.
- [ ] Milestone 4 (restante) - endpoints de listagem de movimentos.
- [ ] Milestone 5 - stock snapshot e alertas farm-level.

## Escopo de entrega
### MVP
- itens (`inventory_item`) com ativacao/desativacao.
- lotes (`inventory_lot`) com validade.
- movimentos (`inventory_movement`) com idempotencia.
- saldo materializado (`inventory_balance`).
- idempotencia persistida (`inventory_idempotency`) para replay e mismatch.
- seguranca por ownership e gates de arquitetura.

### POST-MVP
- compras/vendas end-to-end.
- custos avancados (medio ponderado e valuation completo).
- integracao orientada a eventos (outbox + consumers) como backbone.

## Milestones detalhados
### Milestone 1 - DDL/Flyway e constraints
Objetivo:
- criar tabelas, constraints e indices do Inventory.

Status:
- [x] Migration `V23__create_inventory_ledger_core_tables.sql` criada.
- [ ] Validar em ambiente PostgreSQL real com `flyway migrate` e smoke test de startup.

### Milestone 2 - Entidades, Ports e regras de dominio
Status: concluido.

DoD atendido:
- `OUT` e `ADJUST DECREASE` sem saldo suficiente retornam `BusinessRuleException (422)`.
- `trackLot=true` exige `lotId`; `trackLot=false` proibe `lotId`.
- `ADJUST` exige `adjustDirection` explicito (`INCREASE` ou `DECREASE`).

### Milestone 3 - Consistencia e concorrencia (ledger + balance)
Status: concluido.

DoD atendido:
- lock pessimista (`SELECT ... FOR UPDATE`) aplicado em item e saldo.
- ordem de lock fixa (item -> balance).
- `upsert` de saldo implementado com suporte a `lotId null` e nao-null.
- ledger, balance e idempotencia atualizados na mesma transacao.

### Milestone 4 - API de items, lots e movements
Status: parcial.

Entregue:
- `POST /api/goatfarms/{farmId}/inventory/movements`.
- header `Idempotency-Key` obrigatorio.
- ownership com `@PreAuthorize("@ownershipService.canManageFarm(#farmId)")`.
- Swagger PT-BR com status `201`, `400`, `404`, `409`, `422`.

Pendente:
- testes de integracao HTTP do endpoint de movement.
- endpoints de consulta/listagem (`GET /movements`).

### Milestone 5 - Stock snapshot e alertas farm-level
Status: pendente.

### Milestone 6 - Gates de arquitetura, documentacao e fechamento
Status: em andamento.

Entregue:
- `InventoryBoundaryArchUnitTest`.
- atualizacao de docs de modulo/API para naming `DECREASE/INCREASE`.

## Checklist de validacao final
Executar antes de abrir PR:
- `./mvnw -Dtest=HexagonalArchitectureGuardTest test`
- `./mvnw -Dtest=InventoryBoundaryArchUnitTest test`
- `./mvnw -Dtest=InventoryMovementBusinessTest test`
- `./mvnw -Dtest=InventoryMovementPersistenceAdapterIntegrationTest test`
- `./mvnw -U clean test`
- `rg -n "import com\\.devmaster\\.goatfarm\\.(health|milk|reproduction)\\." src/main/java/com/devmaster/goatfarm/inventory`

Criterio objetivo de pronto:
- testes verdes;
- fronteira de arquitetura verde;
- docs e contrato de API sincronizados;
- sem inconsistencias de nomenclatura (`INCREASE/DECREASE`).
