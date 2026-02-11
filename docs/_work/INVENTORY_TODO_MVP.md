# Inventory TODO MVP (Checklist de Engenharia)
Última atualização: 2026-02-11
Escopo: plano executável para implementar o MVP de Inventory do núcleo à API, mantendo arquitetura hexagonal e contratos documentados.
Links relacionados: [Módulo Inventory](../02-modules/INVENTORY_MODULE.md), [ADR-002](../01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Portal](../INDEX.md)

## Status
- Documento de trabalho para execução técnica.
- Não representa implementação já concluída.

## Escopo de entrega
### MVP
- itens (`inventory_item`) com ativação/desativação.
- lotes (`inventory_lot`) com validade.
- movimentos (`inventory_movement`) com idempotência.
- saldo materializado (`inventory_balance`).
- idempotência persistida (`inventory_idempotency`) para replay e detecção de mismatch.
- alertas farm-level (`low-stock`, `expiring`).
- segurança por ownership e gates de arquitetura.

### POST-MVP
- compras/vendas end-to-end.
- custos avançados (médio ponderado, valuation completo).
- integração orientada a eventos (outbox + consumers) como backbone.
- integrações financeiras ampliadas.

## Ordem sugerida de implementação (núcleo -> API)
### Milestone 1 - DDL/Flyway e constraints
Objetivo:
- criar tabelas, constraints e índices do Inventory.

Arquivos/classes afetadas:
> Observação: substitua `Vxx` pelo próximo número de versão disponível no projeto.

- `src/main/resources/db/migration/Vxx__create_inventory_core_tables.sql`
- `src/main/resources/db/migration/Vxx__create_inventory_indexes.sql`
- `src/main/resources/db/migration/Vxx__create_inventory_balance_partial_unique.sql`

Testes esperados:
- testes de repository para unicidades e consultas principais.

DoD:
- `inventory_item (farm_id, name_normalized)` único.
- `inventory_lot (item_id, lot_code)` único.
- unicidade de `inventory_balance` com suporte a `lot_id null`.
- `inventory_idempotency (farm_id, idempotency_key)` único (base do replay + mismatch).
- constraint `quantity > 0` ativa.

Risco/Mitigação:
- risco: semântica incorreta de unicidade com `lot_id null`.
- mitigação: índice parcial + teste dedicado.

### Milestone 2 - Entidades, Ports e regras de domínio
Objetivo:
- criar entidades e ports hexagonais para item, lote, movimento e saldo.

Arquivos/classes afetadas:
- `src/main/java/com/devmaster/goatfarm/inventory/application/ports/in/`
- `src/main/java/com/devmaster/goatfarm/inventory/application/ports/out/`
- `src/main/java/com/devmaster/goatfarm/inventory/business/`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/entity/`

Testes esperados:
- unit tests de regras de domínio (`IN`, `OUT`, `ADJUST`, `trackLot`, saldo negativo).

DoD:
- `OUT` e `ADJUST` decremento sem saldo suficiente retornam regra de negócio (`422` via handler).
- `trackLot=true` exige `lotId` para `IN/OUT/ADJUST`.
- `ADJUST` exige `adjustDirection` explícito.

Risco/Mitigação:
- risco: regra divergente entre business e controller.
- mitigação: validação de domínio centralizada no business.

### Milestone 3 - Consistência e concorrência (ledger + balance)
Objetivo:
- implementar transação única para movimento com lock de saldo.

Arquivos/classes afetadas:
- `src/main/java/com/devmaster/goatfarm/inventory/business/InventoryMovementBusiness.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/adapter/StockMovementPersistenceAdapter.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/adapter/StockBalancePersistenceAdapter.java`

Testes esperados:
- integração concorrente com duas baixas simultâneas no mesmo item/lote.

DoD:
- lock pessimista (`SELECT ... FOR UPDATE`) aplicado.
- ordem de lock fixa (item -> balances impactados; em lotes, ordenar por `lotId` asc).
- `upsert + relock` quando saldo ainda não existe.
- ledger e balance atualizados na mesma transação.

Risco/Mitigação:
- risco: deadlock.
- mitigação: ordem de lock fixa + cenários concorrentes em teste.

### Milestone 4 - API de items, lots e movements
Objetivo:
- publicar endpoints MVP com segurança e paginação.

Arquivos/classes afetadas:
- `src/main/java/com/devmaster/goatfarm/inventory/api/controller/InventoryItemController.java`
- `src/main/java/com/devmaster/goatfarm/inventory/api/controller/InventoryLotController.java`
- `src/main/java/com/devmaster/goatfarm/inventory/api/controller/InventoryMovementController.java`
- `src/main/java/com/devmaster/goatfarm/inventory/api/dto/`
- `src/main/java/com/devmaster/goatfarm/inventory/api/mapper/`

Testes esperados:
- integração dos controllers para `200/201/400/401/403/404/409/422`.

DoD:
- `POST /movements` exige header `Idempotency-Key`.
- mesma key + payload igual => replay idempotente (`200`).
- mesma key + payload diferente => `409`.
- ownership aplicado em todos os endpoints farm-level.

Risco/Mitigação:
- risco: quebra de contrato de erro/paginação.
- mitigação: revisar contra `API_CONTRACTS.md` antes do merge.

### Milestone 5 - Stock snapshot e alertas farm-level
Objetivo:
- implementar leitura de saldo e alertas operacionais.

Arquivos/classes afetadas:
- `src/main/java/com/devmaster/goatfarm/inventory/api/controller/InventoryStockController.java`
- `src/main/java/com/devmaster/goatfarm/inventory/api/controller/FarmInventoryAlertsController.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/repository/StockBalanceRepository.java`
- `src/main/java/com/devmaster/goatfarm/inventory/persistence/repository/InventoryAlertQueryRepository.java`

Testes esperados:
- integração para `low-stock` e `expiring` com paginação.

DoD:
- formato de resposta de alertas: `totalPending` + `alerts[]`.
- ordenação estável de alertas (severidade e critérios secundários).
- filtros de janela (`days`) funcionando.

Risco/Mitigação:
- risco: alerta inconsistente por timezone.
- mitigação: padronizar regra com `LocalDate`.

### Milestone 6 - Gates de arquitetura, documentação e fechamento
Objetivo:
- blindar fronteira de contexto e fechar documentação final.

Arquivos/classes afetadas:
- `src/test/java/com/devmaster/goatfarm/architecture/InventoryBoundaryArchUnitTest.java`
- `docs/02-modules/INVENTORY_MODULE.md`
- `docs/01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md`
- `docs/03-api/API_CONTRACTS.md`
- `docs/INDEX.md`

Testes esperados:
- execução dos gates de arquitetura e suíte de testes.

DoD:
- `inventory` sem import interno de `health|milk|reproduction` (exceto `sharedkernel`).
- documentação alinhada com comportamento implementado.
- sem links locais proibidos (`file:///`, `C:\`, `/home/`).

Risco/Mitigação:
- risco: documentação divergir da implementação.
- mitigação: checklist de PR obrigatório para contrato x controller x teste.

## Checklist de validação final (DoD consolidado)
Executar antes de abrir PR:
- `./mvnw -Dtest=HexagonalArchitectureGuardTest test` (Windows: `./mvnw.cmd ...`)
- `./mvnw -Dtest=InventoryBoundaryArchUnitTest test` (Windows: `./mvnw.cmd ...`)
- `./mvnw test` (Windows: `./mvnw.cmd test`)
- `rg -n "import com\\.devmaster\\.goatfarm\\.(health|milk|reproduction)\\." src/main/java/com/devmaster/goatfarm/inventory`
- `rg -n "file:///|C:\\\\|/home/" docs -g "!docs/_work/**" -g "!docs/_archive/**"`

Critério objetivo de pronto:
- testes verdes;
- gates de arquitetura verdes;
- contratos de API e docs sincronizados;
- sem violações de links/policies de documentação.
