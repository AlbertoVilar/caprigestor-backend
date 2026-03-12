# Módulo Inventory (Estoque)
Última atualização: 2026-03-12
Escopo: estado técnico e funcional do módulo Inventory após a formalização do ciclo de vida de lotes e o alinhamento da cadeia Flyway.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API Contracts](../03-api/API_CONTRACTS.md), [ADR-002](../01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md), [TODO MVP](../_work/INVENTORY_TODO_MVP.md), [Guia de Migração](../03-api/API_VERSIONING_MIGRATION_GUIDE.md)

## Status do documento
- Natureza: especificação + status de implementação.
- Estado atual do módulo: itens, lotes reais, ledger de movimentos, saldo materializado e idempotência implementados.
- Objetivo: manter contratos, invariantes e estratégia de consistência sincronizados com o código e com a cadeia de migrations.

## Visão geral
O módulo `inventory` controla estoque por fazenda (`farmId`) com rastreabilidade de entradas, saídas e ajustes.

O escopo atual cobre:
- itens de estoque;
- lotes reais vinculados à fazenda e ao item;
- movimentos (`IN`, `OUT`, `ADJUST`);
- saldo materializado (`inventory_balance`);
- idempotência persistida (`inventory_idempotency`);
- endpoints farm-level para itens, lotes, movimentos e saldos.

## Regras e invariantes
- `farmId` é obrigatório em todas as operações.
- `quantity` deve ser sempre `> 0`.
- `OUT` não pode gerar saldo negativo.
- `ADJUST` com `DECREASE` não pode gerar saldo negativo.
- `trackLot=true` exige lote válido e ativo.
- `trackLot=false` proíbe `lotId`.
- lote pertence a uma única fazenda e a um único item.
- `code` é obrigatório no lote.
- unicidade mínima do lote: `(farm_id, item_id, code_normalized)`.
- `inventory_movement` continua imutável após gravação.
- `Idempotency-Key` reutilizada com payload diferente retorna `409`.

## Contratos REST
Base canônica: `/api/v1/goatfarms/{farmId}/inventory`

### Itens
| Método | URL | Finalidade |
|---|---|---|
| `POST` | `/items` | cadastrar item de estoque |
| `GET` | `/items` | listar itens de estoque |

### Lotes
| Método | URL | Finalidade |
|---|---|---|
| `POST` | `/lots` | cadastrar lote de estoque |
| `GET` | `/lots` | listar lotes por fazenda, com filtro opcional por `itemId` e `active` |
| `PATCH` | `/lots/{lotId}/active` | ativar ou inativar lote |

Exemplo mínimo de criação de lote:
```json
{
  "itemId": 101,
  "code": "RACAO-2026-03",
  "description": "Fornecedor Alfa, entrega de março",
  "expirationDate": "2026-09-30",
  "active": true
}
```

Exemplo mínimo de resposta:
```json
{
  "id": 501,
  "farmId": 1,
  "itemId": 101,
  "code": "RACAO-2026-03",
  "description": "Fornecedor Alfa, entrega de março",
  "expirationDate": "2026-09-30",
  "active": true
}
```

### Movimentos
| Método | URL | Finalidade |
|---|---|---|
| `POST` | `/movements` | registrar `IN`, `OUT` ou `ADJUST` |
| `GET` | `/movements` | listar histórico paginado com filtros |

### Saldos
| Método | URL | Finalidade |
|---|---|---|
| `GET` | `/balances` | listar saldos paginados por item e lote |

## Evolução do modelo de lote
A tabela `inventory_lot` deixou de ser apenas `id` e passou a sustentar o fluxo real com:
- `farm_id`
- `item_id`
- `code`
- `code_normalized`
- `description`
- `expiration_date`
- `active`

Migrations relevantes:
- `V24__normalize_capril_vilar_goat_colors.sql`
- `V25__evolve_inventory_lot_lifecycle.sql`

Correção formal de versionamento:
- a versão `24` já havia sido aplicada em ambientes de desenvolvimento como normalização de cores do seed do Capril Vilar;
- o ciclo de vida de lotes foi promovido inicialmente usando a mesma versão, o que gerou divergência de checksum no Flyway;
- a cadeia correta passou a ser: manter `V24` para a normalização já aplicada e publicar a evolução de lotes em `V25`.

Compatibilidade preservada:
- `inventory_balance.lot_id` e `inventory_movement.lot_id` continuam sendo a referência técnica;
- o fluxo de movimentação permanece o mesmo, agora com validação contextual do lote.

## Consistência da movimentação
Quando o item possui `trackLot=true`, a movimentação só é aceita se:
- `lotId` existir;
- o lote pertencer à mesma fazenda;
- o lote pertencer ao mesmo item;
- o lote estiver ativo.

Isso elimina o uso de `lotId` solto sem contexto de item/fazenda.

## Segurança
Todos os endpoints farm-level do módulo seguem:
- `@PreAuthorize("@ownershipService.canManageFarm(#farmId)")`

## Observações
- Não houve criação de módulo paralelo de lotes fora de `inventory`.
- O fluxo de lotes foi encaixado na arquitetura já existente: controller -> port in -> business -> port out -> adapter/repository.
- A correção de Flyway é de integridade de versionamento; ela não altera o comportamento funcional do Bloco 3.