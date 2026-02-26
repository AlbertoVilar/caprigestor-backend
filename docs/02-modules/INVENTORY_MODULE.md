# Módulo Inventory (Estoque)
Última atualização: 2026-02-26
Escopo: estado técnico e funcional do MVP de Inventory após implementação do ledger core no backend.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [ADR-002](../01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md), [TODO MVP](../_work/INVENTORY_TODO_MVP.md), [Módulo Health](./HEALTH_VETERINARY_MODULE.md), [Módulo Lactation](./LACTATION_MODULE.md), [Módulo Reproduction](./REPRODUCTION_MODULE.md), [Guia de Migração](../03-api/API_VERSIONING_MIGRATION_GUIDE.md)

## Status do documento
- Natureza: especificação + status de implementação.
- Estado atual do modulo: ledger core implementado (movement, balance, idempotency, API POST /api/v1/goatfarms/{farmId}/inventory/movements).
- Objetivo: manter contratos, invariantes e estratégia de consistência sincronizados com o código.

## Visao geral
O modulo `inventory` controla estoque por fazenda (`farmId`) com rastreabilidade de entradas, saidas e ajustes.

No MVP atual, o modulo cobre:
- itens de estoque;
- lotes;
- movimentos (`IN`, `OUT`, `ADJUST`);
- saldo materializado (`inventory_balance`);
- idempotencia persistida (`inventory_idempotency`);
- endpoint farm-level de comando para movimentos.

Fora do MVP:
- compras e vendas ponta a ponta;
- custo medio avancado;
- alertas e snapshots completos;
- integracoes orientadas a eventos como backbone obrigatorio.

## Regras e invariantes (nao negociaveis)
- `farmId` obrigatorio em todas as operacoes.
- `quantity` sempre `> 0`.
- `OUT` nao pode gerar saldo negativo.
- `ADJUST` com `adjustDirection=DECREASE` nao pode gerar saldo negativo.
- `ADJUST` exige `adjustDirection` explicito (`INCREASE` ou `DECREASE`).
- `IN` e `OUT` exigem `adjustDirection` nulo.
- `trackLot=true` exige `lotId`; `trackLot=false` proibe `lotId`.
- `inventory_movement` e imutavel apos gravacao.
- chave idempotente reutilizada com payload diferente retorna `409`.

## Contratos REST do MVP
Base: `/api/v1/goatfarms/{farmId}/inventory`

Padroes obrigatorios:
- seguranca por ownership: `@PreAuthorize("@ownershipService.canManageFarm(#farmId)")`.
- erros/status alinhados ao [API_CONTRACTS](../03-api/API_CONTRACTS.md).

### Movements (ledger)
| Metodo | URL | Finalidade |
|---|---|---|
| `POST` | `/movements` | registrar `IN`, `OUT` ou `ADJUST` (implementado) |
| `GET` | `/movements` | listar historico (planejado) |

Exemplo minimo (POST /api/v1/goatfarms/{farmId}/inventory/movements):
- Headers:
  - `Idempotency-Key: <string>`
- Request:
```json
{
  "itemId": 101,
  "type": "OUT",
  "quantity": 2.0,
  "lotId": 10,
  "adjustDirection": null,
  "movementDate": "2026-02-18",
  "reason": "Baixa por aplicacao sanitaria"
}
```
- Response:
```json
{
  "movementId": 9001,
  "itemId": 101,
  "lotId": 10,
  "type": "OUT",
  "quantity": 2.0,
  "movementDate": "2026-02-18",
  "resultingBalance": 18.0,
  "createdAt": "2026-02-18T12:00:00Z"
}
```

Regra de idempotencia:
- `Idempotency-Key` obrigatoria no `POST /api/v1/goatfarms/{farmId}/inventory/movements`.
- primeira execucao valida: `201 Created`.
- mesma key + mesmo payload logico: replay idempotente (`200` com mesma resposta).
- mesma key + payload diferente: `409 Conflict`.
- ausencia de key: `400 Bad Request`.

## Concorrencia e consistencia
Estrategia implementada para comando de movimento:
- transacao unica por comando;
- lock pessimista com `SELECT ... FOR UPDATE`;
- ordem fixa: lock de item (`inventory_item`) e depois lock de saldo (`inventory_balance`);
- `upsert` de saldo quando necessario;
- validacao de invariantes antes de gravar;
- gravacao de ledger, saldo e idempotencia na mesma transacao.

## Seguranca
Todos os endpoints farm-level do modulo seguem:
- `@PreAuthorize("@ownershipService.canManageFarm(#farmId)")`.

## Gate de arquitetura (fronteira)
Regra ativa:
- `inventory` nao pode importar classes internas de `health`, `milk` e `reproduction`.

Validacao:
- `InventoryBoundaryArchUnitTest` (implementado).
- `rg -n "import com\\.devmaster\\.goatfarm\\.(health|milk|reproduction)\\." src/main/java/com/devmaster/goatfarm/inventory`.

## Persistencia e performance
- `inventory_movement` e fonte de verdade para trilha auditavel.
- `inventory_balance` e materializado para leitura rapida.
- migracao Flyway implementada em `V23__create_inventory_ledger_core_tables.sql`.

## Erros/Status
Status esperados:
- `200` (replay idempotente), `201` (criacao real), `400`, `401`, `403`, `404`, `409`, `422`, `500`.

## Observacoes
- Este documento representa o contrato alvo e o estado atual do modulo.
- Ajustes de escopo devem atualizar tambem o [ADR-002](../01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md) e o [TODO MVP](../_work/INVENTORY_TODO_MVP.md).


