# Modulo Inventory (Estoque)
Ultima atualizacao: 2026-02-10
Escopo: especificacao do MVP de estoque farm-level com itens, lotes/validade, ledger de movimentos, saldo materializado e alertas.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Modulo Health](./HEALTH_VETERINARY_MODULE.md), [Modulo Lactacao](./LACTATION_MODULE.md), [Modulo Reproduction](./REPRODUCTION_MODULE.md), [ADR Inventory](../01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md), [TODO MVP](../_work/INVENTORY_TODO_MVP.md)

## Visao geral
O modulo `inventory` sera o contexto de referencia para controle de estoque por fazenda, com rastreabilidade completa de entradas, saidas e ajustes.

### MVP exato (entra agora)
- cadastro e consulta de itens (`inventory_item`) por fazenda, com `minQuantity`, `trackLot` e status ativo/inativo;
- cadastro e consulta de lotes (`inventory_lot`) com `lotCode`, `expiresAt` e quantidade inicial;
- movimentos de estoque (`stock_movement`) com tipos `IN`, `OUT`, `ADJUST`, idempotencia e rastreabilidade por origem (`sourceModule`, `sourceRef`);
- saldo materializado (`stock_balance`) para leitura rapida:
- nivel item (`lotId = null`) para consultas gerais;
- nivel lote (`lotId != null`) para itens `trackLot=true`;
- alertas farm-level:
- `low-stock` (abaixo do minimo);
- `expiring` (lote proximo do vencimento);
- padrao de resposta: `totalPending` + `alerts[]`.

### Fora do MVP (nao entra agora)
- modulo de compras/vendas (`purchases/sales`);
- financeiro contabil/fiscal;
- custo medio ponderado e valorizacao contabil completa;
- integracao orientada a eventos (outbox/consumer) como requisito de consistencia;
- feed management completo (consumo planejado de racao com simulacao nutricional).

## Dominio
### Bounded context
- Contexto: `inventory`.
- Escopo: estritamente farm-level (`farmId` obrigatorio em todas as operacoes).
- Fronteiras:
- nao importa entidades internas de `health`, `milk`, `reproduction`, `finance`, `purchases` ou `sales`;
- integra por referencias `sourceModule` + `sourceRef`.

### Agregados e entidades
#### StockItem (agregado raiz)
- Campos principais:
- `id`
- `farmId`
- `name`
- `nameNormalized`
- `category` (`MEDICAMENTO`, `VACINA`, `HORMONIO`, `RACAO`, `INSUMO`, `OUTRO`)
- `unit` (`ML`, `DOSE`, `KG`, `L`, `UN`)
- `minQuantity`
- `active`
- `trackLot` (boolean)
- Responsabilidades:
- definir metadados do item;
- definir politica de rastreabilidade por lote;
- governar limite minimo para alerta.

#### StockLot (entidade do agregado StockItem)
- Campos principais:
- `id`
- `farmId`
- `itemId`
- `lotCode`
- `expiresAt`
- `initialQuantity`
- `active`
- Responsabilidades:
- identificar lote fisico;
- habilitar controle de validade;
- suportar baixa por lote (selecionado ou FEFO em evolucao futura).

#### StockMovement (ledger)
- Campos principais:
- `id`
- `farmId`
- `itemId`
- `lotId` (obrigatorio para item `trackLot=true`)
- `movementType` (`IN`, `OUT`, `ADJUST`)
- `adjustDirection` (`INCREMENT`, `DECREMENT`) obrigatorio quando `movementType=ADJUST`
- `quantity` (sempre positiva)
- `occurredAt`
- `reason`
- `sourceModule` (`HEALTH`, `REPRODUCTION`, `MILK`, `PURCHASES`, `SALES`, `MANUAL`)
- `sourceRef` (string)
- `idempotencyKey`
- `unitCost` (opcional MVP, recomendado desde inicio para evolucao financeira)
- Responsabilidades:
- registrar trilha auditavel imutavel;
- registrar origem de consumo/entrada sem FK cruzada;
- garantir idempotencia de escrita.

#### StockBalance (saldo materializado)
- Campos principais:
- `farmId`
- `itemId`
- `lotId` (nulo para saldo consolidado por item, nao nulo para saldo por lote)
- `onHandQuantity`
- `updatedAt`
- Responsabilidades:
- leitura performatica de saldo atual;
- base para alertas e consultas frequentes;
- evitar agregacao pesada no ledger em tempo de leitura.

### Value Objects sugeridos
- `QuantityVO` (valor numerico + unidade valida para item)
- `InventorySourceVO` (`sourceModule`, `sourceRef`)
- `InventoryAlertVO` (payload padrao de alertas farm-level)

## Invariantes de negocio (nao negociaveis)
- `farmId` e obrigatorio em todas as operacoes.
- `quantity` sempre `> 0`; o sentido do movimento vem de `movementType`:
- `IN` soma saldo;
- `OUT` subtrai saldo;
- `ADJUST` usa `adjustDirection` para definir soma/subtracao.
- `movementType=OUT` nao pode resultar em saldo negativo.
- `movementType=ADJUST` com `adjustDirection=DECREMENT` nao pode resultar em saldo negativo.
- `trackLot=true` exige `lotId` para qualquer movimento (`IN`, `OUT`, `ADJUST`).
- `expiresAt` (quando informado) nao pode ser anterior a data de entrada do lote.
- `StockMovement` e imutavel apos gravacao.
- `StockBalance` deve refletir o ledger na mesma transacao de comando.
- Unicidades minimas:
- `inventory_item (farm_id, name_normalized)` unico;
- `inventory_lot (item_id, lot_code)` unico;
- `stock_movement (farm_id, idempotency_key)` unico;
- `stock_balance` unico por escopo (`farm_id`, `item_id`, `lot_id`) com garantia para `lot_id null` via indice parcial.

## Diagramas
### Context map (integracao sem acoplamento de entidades)
```mermaid
graph LR
    Health[health] -->|sourceModule/sourceRef| Inventory[inventory]
    Reproduction[reproduction] -->|sourceModule/sourceRef| Inventory
    Milk[milk] -->|sourceModule/sourceRef| Inventory
    Purchases[purchases futuro] -->|entradas| Inventory
    Sales[sales futuro] -->|saidas| Inventory
    Inventory -->|saldo e alertas farm-level| Front[frontend]
    Inventory -->|base de custo futuro| Finance[finance futuro]
```

### Fluxo de movimento (idempotencia + lock + ledger + balance)
```mermaid
sequenceDiagram
    participant C as Controller
    participant B as InventoryBusiness
    participant M as StockMovementPort
    participant SB as StockBalancePort

    C->>B: POST movement + header Idempotency-Key
    B->>M: busca por (farmId, idempotencyKey)
    alt chave existente com mesmo hash de payload
        B-->>C: 200 OK (replay idempotente)
    else chave existente com payload diferente
        B-->>C: 409 Conflict
    else chave nova
        B->>SB: lock saldo item (FOR UPDATE)
        opt item trackLot=true
            B->>SB: lock saldo lote (FOR UPDATE)
        end
        B->>B: valida invariantes e saldo nao negativo
        B->>M: grava ledger imutavel
        B->>SB: atualiza saldo item e saldo lote
        B-->>C: 201 Created
    end
```

### Visao hexagonal do modulo
```mermaid
graph TD
    A[InventoryController] --> B[Ports In]
    B --> C[InventoryBusiness]
    C --> D[Ports Out]
    D --> E[StockItemPersistenceAdapter]
    D --> F[StockLotPersistenceAdapter]
    D --> G[StockMovementPersistenceAdapter]
    D --> H[StockBalancePersistenceAdapter]
```

## API
Base URL: `/api/goatfarms/{farmId}/inventory`

Padrao de seguranca:
- todos os endpoints usam `@PreAuthorize("@ownershipService.canManageFarm(#farmId)")`.

Padrao de resposta de alerta:
- `totalPending` + `alerts[]`.

### Endpoints de itens
| Metodo | URL | Query params | Retorno |
|---|---|---|---|
| `GET` | `/api/goatfarms/{farmId}/inventory/items` | `category`, `active`, `search`, `page`, `size`, `sort` | `200 OK` (pagina) |
| `POST` | `/api/goatfarms/{farmId}/inventory/items` | - | `201 Created` |
| `PATCH` | `/api/goatfarms/{farmId}/inventory/items/{itemId}` | - | `200 OK` |
| `GET` | `/api/goatfarms/{farmId}/inventory/items/{itemId}` | - | `200 OK` |

Contrato curto (criar item):
- URL: `POST /api/goatfarms/1/inventory/items`
- Request:
```json
{
  "name": "Vacina clostridiose",
  "category": "VACINA",
  "unit": "DOSE",
  "minQuantity": 20,
  "trackLot": true
}
```
- Response:
```json
{
  "id": 101,
  "farmId": 1,
  "name": "Vacina clostridiose",
  "category": "VACINA",
  "unit": "DOSE",
  "minQuantity": 20,
  "trackLot": true,
  "active": true
}
```

### Endpoints de lotes
| Metodo | URL | Query params | Retorno |
|---|---|---|---|
| `GET` | `/api/goatfarms/{farmId}/inventory/items/{itemId}/lots` | `active`, `expiringBefore`, `page`, `size`, `sort` | `200 OK` (pagina) |
| `POST` | `/api/goatfarms/{farmId}/inventory/items/{itemId}/lots` | - | `201 Created` |
| `PATCH` | `/api/goatfarms/{farmId}/inventory/items/{itemId}/lots/{lotId}` | - | `200 OK` |

Contrato curto (criar lote):
- URL: `POST /api/goatfarms/1/inventory/items/101/lots`
- Request:
```json
{
  "lotCode": "VAC-2026-0009",
  "expiresAt": "2026-12-31",
  "initialQuantity": 50
}
```
- Response:
```json
{
  "id": 7001,
  "itemId": 101,
  "lotCode": "VAC-2026-0009",
  "expiresAt": "2026-12-31",
  "onHandQuantity": 50
}
```

### Endpoints de movimentos (ledger)
| Metodo | URL | Query params | Headers | Retorno |
|---|---|---|---|---|
| `POST` | `/api/goatfarms/{farmId}/inventory/movements` | - | `Idempotency-Key` (obrigatorio) | `201 Created` |
| `GET` | `/api/goatfarms/{farmId}/inventory/movements` | `itemId`, `lotId`, `movementType`, `sourceModule`, `sourceRef`, `from`, `to`, `page`, `size`, `sort` | - | `200 OK` (pagina) |

Contrato curto (baixa por evento de saude):
- URL: `POST /api/goatfarms/1/inventory/movements`
- Header:
```text
Idempotency-Key: health-10-dose-1
```
- Request:
```json
{
  "itemId": 101,
  "lotId": 7001,
  "movementType": "OUT",
  "quantity": 1,
  "reason": "Aplicacao de vacina",
  "sourceModule": "HEALTH",
  "sourceRef": "health-event:10"
}
```
- Response:
```json
{
  "id": 9001,
  "farmId": 1,
  "itemId": 101,
  "movementType": "OUT",
  "quantity": 1,
  "sourceModule": "HEALTH",
  "sourceRef": "health-event:10",
  "onHandAfter": 49
}
```

Contrato curto (ajuste de inventario):
- URL: `POST /api/goatfarms/1/inventory/movements`
- Header:
```text
Idempotency-Key: inv-adjust-2026-02-10-01
```
- Request:
```json
{
  "itemId": 101,
  "lotId": 7001,
  "movementType": "ADJUST",
  "adjustDirection": "DECREMENT",
  "quantity": 2,
  "reason": "Quebra de frasco",
  "sourceModule": "MANUAL",
  "sourceRef": "count:2026-02-10"
}
```

### Contrato de idempotencia
- mesma `Idempotency-Key` + mesmo payload logico (hash igual):
- primeira execucao: `201`;
- replay: `200` com mesmo corpo de negocio e `idempotentReplay=true`.
- mesma `Idempotency-Key` + payload diferente:
- `409 Conflict`.
- `Idempotency-Key` ausente em `POST /movements`:
- `400 Bad Request`.

### Endpoints de saldo
| Metodo | URL | Query params | Retorno |
|---|---|---|---|
| `GET` | `/api/goatfarms/{farmId}/inventory/stock` | `itemId`, `category`, `includeLots`, `page`, `size`, `sort` | `200 OK` (pagina) |

### Endpoints de alertas
| Metodo | URL | Query params | Retorno |
|---|---|---|---|
| `GET` | `/api/goatfarms/{farmId}/inventory/alerts/low-stock` | `category`, `severity`, `page`, `size` | `200 OK` (`totalPending` + `alerts`) |
| `GET` | `/api/goatfarms/{farmId}/inventory/alerts/expiring` | `days` (default `30`, max `180`), `category`, `severity`, `page`, `size` | `200 OK` (`totalPending` + `alerts`) |

Contrato curto (low-stock):
- URL: `GET /api/goatfarms/1/inventory/alerts/low-stock?page=0&size=20`
- Response:
```json
{
  "totalPending": 2,
  "alerts": [
    {
      "severity": "HIGH",
      "itemId": 101,
      "itemName": "Vacina clostridiose",
      "onHandQuantity": 12,
      "minQuantity": 20,
      "deficit": 8
    }
  ]
}
```

Contrato curto (expiring):
- URL: `GET /api/goatfarms/1/inventory/alerts/expiring?days=30&page=0&size=20`
- Response:
```json
{
  "totalPending": 1,
  "alerts": [
    {
      "severity": "MEDIUM",
      "itemId": 101,
      "itemName": "Vacina clostridiose",
      "lotId": 7001,
      "lotCode": "VAC-2026-0009",
      "expiresAt": "2026-03-15",
      "daysToExpire": 12
    }
  ]
}
```

## Persistencia / Performance
### Modelo de dados proposto (especificacao)
#### `inventory_item`
- `id` PK
- `farm_id` NOT NULL
- `name` NOT NULL
- `name_normalized` NOT NULL
- `category` NOT NULL
- `unit` NOT NULL
- `min_quantity` NOT NULL
- `track_lot` NOT NULL
- `active` NOT NULL
- `created_at`, `updated_at`
- Constraints:
- `uk_inventory_item_farm_name` (`farm_id`, `name_normalized`)

#### `inventory_lot`
- `id` PK
- `farm_id` NOT NULL
- `item_id` NOT NULL
- `lot_code` NOT NULL
- `expires_at` NULL
- `initial_quantity` NOT NULL
- `active` NOT NULL
- `created_at`, `updated_at`
- Constraints:
- `fk_inventory_lot_item` (`item_id` -> `inventory_item.id`)
- `uk_inventory_lot_item_code` (`item_id`, `lot_code`)

#### `stock_movement`
- `id` PK
- `farm_id` NOT NULL
- `item_id` NOT NULL
- `lot_id` NULL
- `movement_type` NOT NULL
- `adjust_direction` NULL
- `quantity` NOT NULL
- `occurred_at` NOT NULL
- `reason` NULL
- `source_module` NOT NULL
- `source_ref` NULL
- `idempotency_key` NOT NULL
- `payload_hash` NOT NULL
- `unit_cost` NULL
- `created_at`
- Constraints:
- `fk_stock_movement_item` (`item_id` -> `inventory_item.id`)
- `fk_stock_movement_lot` (`lot_id` -> `inventory_lot.id`)
- `uk_stock_movement_farm_idempotency` (`farm_id`, `idempotency_key`)
- `ck_stock_movement_quantity_positive` (`quantity > 0`)
- `ck_stock_movement_adjust_direction` (obrigatorio quando `movement_type='ADJUST'`)

#### `stock_balance`
- Campos:
- `id` PK tecnica;
- `farm_id` NOT NULL;
- `item_id` NOT NULL;
- `lot_id` NULL;
- `on_hand_quantity` NOT NULL;
- `updated_at` NOT NULL.
- Semantica:
- 1 linha por item consolidado (`lot_id null`);
- 1 linha por lote (`lot_id not null`) quando `trackLot=true`.
- Constraints:
- `fk_stock_balance_item` (`item_id` -> `inventory_item.id`)
- `fk_stock_balance_lot` (`lot_id` -> `inventory_lot.id`)
- `uk_stock_balance_farm_item_lot` (`farm_id`, `item_id`, `lot_id`)
- `uk_stock_balance_farm_item_null_lot` (parcial para garantir uma unica linha com `lot_id null`)

### Indices recomendados
- `idx_inventory_item_farm_category_active` (`farm_id`, `category`, `active`)
- `idx_inventory_lot_farm_expires` (`farm_id`, `expires_at`)
- `idx_stock_movement_farm_item_time` (`farm_id`, `item_id`, `occurred_at desc`)
- `idx_stock_movement_farm_source` (`farm_id`, `source_module`, `source_ref`)
- `idx_stock_balance_farm_item` (`farm_id`, `item_id`)

### Estrategia de concorrencia e consistencia
- abordagem padrao: row-lock pessimista com `SELECT ... FOR UPDATE` em `stock_balance`;
- ordem de lock para evitar deadlock:
- primeiro saldo consolidado por item (`lot_id null`);
- depois saldo por lote (`lot_id not null`, quando aplicavel);
- se a linha de saldo nao existir:
- executar upsert;
- reler com lock antes de validar e atualizar;
- transacao unica por comando:
- valida idempotencia;
- aplica locks;
- valida invariantes de negocio;
- grava ledger;
- atualiza saldos.

### Flyway (somente planejamento)
- `V23__create_inventory_core_tables.sql`:
- cria `inventory_item`, `inventory_lot`, `stock_movement`, `stock_balance` e constraints.
- `V24__create_inventory_indexes.sql`:
- cria indices de leitura de saldo, historico e alertas.
- `V25__create_inventory_balance_partial_unique.sql`:
- cria indice parcial para unicidade de `stock_balance` com `lot_id null`.

## Alertas
### Low-stock
- Regra: `onHandQuantity < minQuantity`.
- Severidade sugerida:
- `HIGH`: `onHandQuantity <= minQuantity * 0.5`;
- `MEDIUM`: `onHandQuantity > minQuantity * 0.5` e `< minQuantity`.
- Ordenacao estavel:
- `severity desc`, `deficit desc`, `itemName asc`.
- Escopo: farm-level.
- Resposta: `totalPending` + `alerts[]`.
- Paginacao: obrigatoria.

### Expiring
- Regra: lotes com `expiresAt` entre `today` e `today + days`.
- Janela default: `30`.
- Janela maxima: `180`.
- Severidade sugerida:
- `HIGH`: vence em `<= 7` dias;
- `MEDIUM`: vence em `8..30` dias;
- `LOW`: vence em `31..days`.
- Ordenacao estavel:
- `severity desc`, `daysToExpire asc`, `lotCode asc`.
- Escopo: apenas `trackLot=true`.

## Seguranca
- Ownership:
- todos os controllers de `inventory` usam `@PreAuthorize("@ownershipService.canManageFarm(#farmId)")`.
- Perfis:
- `ROLE_ADMIN`, `ROLE_OPERATOR`, `ROLE_FARM_OWNER` conforme politica atual.
- Nao permitir endpoints de estoque sem escopo de fazenda.

## Integracoes
### Farm (obrigatoria)
- `farmId` em todas as rotas e queries.
- dados de estoque totalmente isolados por fazenda.

### Health
- Integracao por referencia:
- `sourceModule=HEALTH`
- `sourceRef=health-event:{eventId}`
- `health` nao depende de entidade de `inventory`.
- baixa de estoque pode ser feita por chamada explicita ao endpoint de movimento.

### Reproduction
- Integracao por referencia:
- `sourceModule=REPRODUCTION`
- `sourceRef` em padrao: `pregnancy:{id}` ou `reproductive-event:{id}`.

### Milk
- Integracao por referencia:
- `sourceModule=MILK`
- `sourceRef` em padrao: `lactation:{id}` ou `milk-production:{id}`.

### Events
- Integracao opcional futura:
- publicar evento de dominio de movimento (`inventory.movement.created`) para observabilidade.
- nao depender do consumer atual de `events` para consistencia transacional do estoque.

## Testes
### Unit
- `InventoryBusinessTest`:
- cria item/lote;
- movimento `IN`, `OUT`, `ADJUST`;
- bloqueio de saldo negativo;
- idempotencia por `Idempotency-Key`;
- validacoes de expiracao e minimo.

### Integration
- `InventoryControllerTest`:
- `401` sem token;
- `403` sem ownership;
- `200/201` com ownership valido.
- `InventoryAlertsIntegrationTest`:
- `low-stock` e `expiring` com `totalPending` + `alerts`;
- severidade e ordenacao estavel.
- `InventoryConcurrencyIntegrationTest`:
- disputa de baixa simultanea no mesmo item/lote;
- validacao de integridade de saldo.

### Arquitetura
- manter `HexagonalArchitectureGuardTest` verde;
- adicionar gate explicito `InventoryBoundaryArchUnitTest`:
- pacote `inventory..` nao pode importar `health..`, `milk..`, `reproduction..`, exceto `sharedkernel..`.
- checks auxiliares de import proibido:
- `rg -n "import com\\.devmaster\\.goatfarm\\.(health|milk|reproduction)\\." src/main/java/com/devmaster/goatfarm/inventory`

## Erros/Status
- `400`: payload invalido, parametros invalidos, `Idempotency-Key` ausente.
- `401`: autenticacao ausente/invalida.
- `403`: ownership/perfil insuficiente.
- `404`: item/lote/movimento nao encontrado no escopo da fazenda.
- `409`: conflito de idempotencia com payload diferente.
- `422`: regra de negocio (saldo insuficiente, lote expirado, movimento invalido).
- Padrao de payload de erro: [API_CONTRACTS](../03-api/API_CONTRACTS.md).

## Observacoes
- Este documento especifica o blueprint do modulo, sem implementacao Java neste momento.
- A implementacao deve seguir backlog detalhado em [INVENTORY_TODO_MVP.md](../_work/INVENTORY_TODO_MVP.md).
- Decisoes arquiteturais formais estao no [ADR-002](../01-architecture/ADR/ADR-002-inventory-ledger-balance-and-lots.md).
