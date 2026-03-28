# MÃ³dulo Inventory (Estoque)
Ãšltima atualizaÃ§Ã£o: 2026-03-12
Escopo: estado tÃ©cnico e funcional do mÃ³dulo Inventory apÃ³s a formalizaÃ§Ã£o do ciclo de vida de lotes e o alinhamento da cadeia Flyway.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API Contracts](../03-api/API_CONTRACTS.md), [ADR-002](../01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md), [TODO MVP](../_work/INVENTORY_TODO_MVP.md), [Guia de MigraÃ§Ã£o](../03-api/API_VERSIONING_MIGRATION_GUIDE.md)

## Status do documento
- Natureza: especificaÃ§Ã£o + status de implementaÃ§Ã£o.
- Estado atual do mÃ³dulo: itens, lotes reais, ledger de movimentos, saldo materializado e idempotÃªncia implementados.
- Objetivo: manter contratos, invariantes e estratÃ©gia de consistÃªncia sincronizados com o cÃ³digo e com a cadeia de migrations.

## VisÃ£o geral
O mÃ³dulo `inventory` controla estoque por fazenda (`farmId`) com rastreabilidade de entradas, saÃ­das e ajustes.

O escopo atual cobre:
- itens de estoque;
- lotes reais vinculados Ã  fazenda e ao item;
- movimentos (`IN`, `OUT`, `ADJUST`);
- saldo materializado (`inventory_balance`);
- idempotÃªncia persistida (`inventory_idempotency`);
- endpoints farm-level para itens, lotes, movimentos e saldos.

## Regras e invariantes
- `farmId` Ã© obrigatÃ³rio em todas as operaÃ§Ãµes.
- `quantity` deve ser sempre `> 0`.
- `OUT` nÃ£o pode gerar saldo negativo.
- `ADJUST` com `DECREASE` nÃ£o pode gerar saldo negativo.
- `trackLot=true` exige lote vÃ¡lido e ativo.
- `trackLot=false` proÃ­be `lotId`.
- lote pertence a uma Ãºnica fazenda e a um Ãºnico item.
- `code` Ã© obrigatÃ³rio no lote.
- unicidade mÃ­nima do lote: `(farm_id, item_id, code_normalized)`.
- `inventory_movement` continua imutÃ¡vel apÃ³s gravaÃ§Ã£o.
- `Idempotency-Key` reutilizada com payload diferente retorna `409`.

## Contratos REST
Base canÃ´nica: `/api/v1/goatfarms/{farmId}/inventory`

### Itens
| MÃ©todo | URL | Finalidade |
|---|---|---|
| `POST` | `/items` | cadastrar item de estoque |
| `GET` | `/items` | listar itens de estoque |

### Lotes
| MÃ©todo | URL | Finalidade |
|---|---|---|
| `POST` | `/lots` | cadastrar lote de estoque |
| `GET` | `/lots` | listar lotes por fazenda, com filtro opcional por `itemId` e `active` |
| `PATCH` | `/lots/{lotId}/active` | ativar ou inativar lote |

Exemplo mÃ­nimo de criaÃ§Ã£o de lote:
```json
{
  "itemId": 101,
  "code": "RACAO-2026-03",
  "description": "Fornecedor Alfa, entrega de marÃ§o",
  "expirationDate": "2026-09-30",
  "active": true
}
```

Exemplo mÃ­nimo de resposta:
```json
{
  "id": 501,
  "farmId": 1,
  "itemId": 101,
  "code": "RACAO-2026-03",
  "description": "Fornecedor Alfa, entrega de marÃ§o",
  "expirationDate": "2026-09-30",
  "active": true
}
```

### Movimentos
| MÃ©todo | URL | Finalidade |
|---|---|---|
| `POST` | `/movements` | registrar `IN`, `OUT` ou `ADJUST` |
| `GET` | `/movements` | listar histÃ³rico paginado com filtros |

### Saldos
| MÃ©todo | URL | Finalidade |
|---|---|---|
| `GET` | `/balances` | listar saldos paginados por item e lote |

## EvoluÃ§Ã£o do modelo de lote
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

CorreÃ§Ã£o formal de versionamento:
- a versÃ£o `24` jÃ¡ havia sido aplicada em ambientes de desenvolvimento como normalizaÃ§Ã£o de cores do seed do Capril Vilar;
- o ciclo de vida de lotes foi promovido inicialmente usando a mesma versÃ£o, o que gerou divergÃªncia de checksum no Flyway;
- a cadeia correta passou a ser: manter `V24` para a normalizaÃ§Ã£o jÃ¡ aplicada e publicar a evoluÃ§Ã£o de lotes em `V25`.

Compatibilidade preservada:
- `inventory_balance.lot_id` e `inventory_movement.lot_id` continuam sendo a referÃªncia tÃ©cnica;
- o fluxo de movimentaÃ§Ã£o permanece o mesmo, agora com validaÃ§Ã£o contextual do lote.

## ConsistÃªncia da movimentaÃ§Ã£o
Quando o item possui `trackLot=true`, a movimentaÃ§Ã£o sÃ³ Ã© aceita se:
- `lotId` existir;
- o lote pertencer Ã  mesma fazenda;
- o lote pertencer ao mesmo item;
- o lote estiver ativo.

Isso elimina o uso de `lotId` solto sem contexto de item/fazenda.

## SeguranÃ§a
Todos os endpoints farm-level do mÃ³dulo seguem:
- `@PreAuthorize("@ownershipService.canManageFarm(#farmId)")`

## ObservaÃ§Ãµes
- NÃ£o houve criaÃ§Ã£o de mÃ³dulo paralelo de lotes fora de `inventory`.
- O fluxo de lotes foi encaixado na arquitetura jÃ¡ existente: controller -> port in -> business -> port out -> adapter/repository.
- A correÃ§Ã£o de Flyway Ã© de integridade de versionamento; ela nÃ£o altera o comportamento funcional do Bloco 3.`r`n`r`n## Atualizacao 2026-03-28 - custo de compra na entrada
O modulo `inventory` passou a aceitar metadados economicos em movimentos `IN` que representem compra operacional.

Campos suportados na entrada por compra:
- `unitCost`
- `totalCost`
- `purchaseDate`
- `supplierName` opcional
- `reason` opcional

Regras adicionais:
- custo de compra so pode ser informado em `IN`;
- `purchaseDate` e obrigatoria quando houver custo;
- `unitCost` e `totalCost` devem ser positivos;
- quando ambos forem informados, a consistencia com `quantity` e validada;
- quando apenas um deles for informado, o outro pode ser derivado no backend;
- movimentos continuam imutaveis apos gravacao.

Limites conscientes:
- esta etapa nao implementa custo medio, FIFO ou valuation contabil;
- o custo registrado serve ao controle operacional e ao resumo mensal simples da fazenda.

