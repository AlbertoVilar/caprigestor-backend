# API_CONTRACTS
Última atualização: 2026-02-28
Escopo: padrões transversais de rotas, autenticação, paginação, idempotência e erros da API.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [Módulo Goat/Farm](../02-modules/GOAT_FARM_MODULE.md), [Módulo Reproduction](../02-modules/REPRODUCTION_MODULE.md), [Módulo Lactação](../02-modules/LACTATION_MODULE.md), [Módulo Milk Production](../02-modules/MILK_PRODUCTION_MODULE.md), [Módulo Health](../02-modules/HEALTH_VETERINARY_MODULE.md), [Módulo Inventory](../02-modules/INVENTORY_MODULE.md), [Guia de Migração de Versionamento](./API_VERSIONING_MIGRATION_GUIDE.md)

## Visão geral
Este documento define contratos comuns para todos os controllers oficiais do backend.

## Regras / Contratos
### Base de rotas
- Base geral: `/api/v1`
- Escopo por fazenda: `/api/v1/goatfarms/{farmId}/...`
- Rotas públicas sem autenticação (quando aplicável) usam namespace separado, por exemplo: `/public/articles`.

### Versionamento e compatibilidade
- Rotas canônicas: sempre em `/api/v1/...`.
- Compatibilidade temporária: rotas legadas em `/api/...` permanecem ativas por 1 ciclo como **DEPRECATED**.
- Remoção planejada das rotas legadas: **2026-06-30** (versão alvo **v2.0.0**).
- Novos endpoints não devem ser publicados fora de `/api/v1`.

### Segurança
- Autenticação: JWT.
- Autorização: ownership por `farmId` e/ou roles (`ROLE_ADMIN`, `ROLE_OPERATOR`, `ROLE_FARM_OWNER`).
- Respostas de segurança:
  - `401` via `CustomAuthenticationEntryPoint`
  - `403` via `CustomAccessDeniedHandler` ou `AccessDeniedException`

### Paginação
- Parâmetros padrão: `page` (base 0), `size`, `sort`.
- O padrão alvo para novos contratos é `content` + metadados em `page.number`, `page.size`, `page.totalElements`, `page.totalPages`.
- Quando um módulo já publicado ainda retorna `Page` do Spring, a exceção deve ser documentada no módulo e preservada por compatibilidade.

### Convenções de payload
- DTOs de request e response separados por módulo.
- Datas em formato ISO (`yyyy-MM-dd` ou `yyyy-MM-dd'T'HH:mm:ss`).
- Mensagens de validação em PT-BR.

### Goat/Farm (cadastros base)
Rotas canônicas:
- `POST /api/v1/goatfarms`
- `GET /api/v1/goatfarms`
- `GET /api/v1/goatfarms/name?name=&page=&size=&sort=`
- `GET /api/v1/goatfarms/{id}`
- `PUT /api/v1/goatfarms/{id}`
- `DELETE /api/v1/goatfarms/{id}`
- `GET /api/v1/goatfarms/{farmId}/permissions`
- `GET /api/v1/goatfarms/{farmId}/goats?page=&size=&sort=`
- `GET /api/v1/goatfarms/{farmId}/goats/search?name=&page=&size=&sort=`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}`
- `POST /api/v1/goatfarms/{farmId}/goats`
- `PUT /api/v1/goatfarms/{farmId}/goats/{goatId}`
- `DELETE /api/v1/goatfarms/{farmId}/goats/{goatId}`

Compatibilidade:
- Rotas legadas equivalentes em `/api/...` seguem ativas como **DEPRECATED** até 2026-06-30.

Paginação atual:
- As listagens continuam retornando `Page` do Spring (`content`, `totalElements`, `number`, etc.) para preservar compatibilidade com o frontend já publicado.

Status principais:
- `200` em consultas e atualizações
- `201` em criações
- `204` em exclusões
- `403` em falha de ownership/perfil
- `404` em recurso não encontrado
- `409` em conflitos de unicidade
- `422` em payload inválido

### Reproduction (gestação e alertas)
Rotas canônicas:
- `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding`
- `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding/{coverageEventId}/corrections`
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/confirm`
- `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/checks`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/active`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}`
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}/close`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/events?page=&size=&sort=`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies?page=&size=&sort=`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/diagnosis-recommendation?referenceDate=`
- `GET /api/v1/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis?referenceDate=&page=&size=`

Compatibilidade:
- Rotas legadas equivalentes em `/api/...` seguem ativas como **DEPRECATED** até 2026-06-30.

Paginação atual:
- Os endpoints `events` e `pregnancies` continuam retornando `Page` do Spring para preservar compatibilidade com o frontend já publicado.
- O endpoint `pregnancy-diagnosis` retorna envelope agregado com `totalPending` e `alerts`.

Status principais:
- `200` em consultas e atualizações
- `201` em criações
- `400` em payload inválido, parâmetros inconsistentes ou paginação inválida
- `403` em falha de ownership/perfil
- `404` em recurso não encontrado
- `422` em regra de negócio violada

Exemplo de alerta pendente:

```http
GET /api/v1/goatfarms/1/reproduction/alerts/pregnancy-diagnosis?referenceDate=2026-02-08&page=0&size=20
```

```json
{
  "totalPending": 2,
  "alerts": [
    {
      "goatId": "BR123",
      "eligibleDate": "2026-02-05",
      "daysOverdue": 3,
      "lastCoverageDate": "2026-01-06",
      "lastCheckDate": null
    }
  ]
}
```

### Lactation e Milk Production
Rotas canônicas de lactação:
- `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/lactations`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/active`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/active/summary`
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}/dry`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}/summary`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/lactations?page=&size=&sort=`
- `GET /api/v1/goatfarms/{farmId}/milk/alerts/dry-off?referenceDate=&page=&size=`

Rotas canônicas de produção de leite:
- `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions`
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions?from=&to=&includeCanceled=&page=&size=&sort=`
- `DELETE /api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}`

Compatibilidade:
- Rotas legadas equivalentes em `/api/...` seguem ativas como **DEPRECATED** até 2026-06-30.

Paginação atual:
- As listagens de lactação e produção continuam retornando `Page` do Spring para preservar compatibilidade.
- O endpoint `dry-off` retorna envelope agregado com `totalPending` e `alerts`.

Status principais:
- `200` em consultas e atualizações
- `201` em criações
- `204` em cancelamentos lógicos
- `400` em payload inválido, filtros inconsistentes ou paginação inválida
- `403` em falha de ownership/perfil
- `404` em recurso não encontrado
- `422` em regra de negócio violada

Exemplo de alerta de secagem:

```http
GET /api/v1/goatfarms/1/milk/alerts/dry-off?referenceDate=2026-02-10&page=0&size=20
```

```json
{
  "totalPending": 1,
  "alerts": [
    {
      "lactationId": 120,
      "goatId": "BR123",
      "dryOffRecommendation": true
    }
  ]
}
```

### Health (eventos sanitários)
Rotas canônicas:
- `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/health-events`
- `PUT /api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}`
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/done`
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/cancel`
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/reopen`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/health-events?from=&to=&type=&status=&page=&size=&sort=`
- `GET /api/v1/goatfarms/{farmId}/health-events/calendar?from=&to=&type=&status=&page=&size=&sort=`
- `GET /api/v1/goatfarms/{farmId}/health-events/alerts?windowDays=`

Compatibilidade:
- Rotas legadas equivalentes em `/api/...` seguem ativas como **DEPRECATED** até 2026-06-30.

Paginação atual:
- As listagens de eventos por cabra e o calendário da fazenda continuam retornando `Page` do Spring para preservar compatibilidade.
- O endpoint `alerts` retorna contadores e listas resumidas, não um `Page`.

Status principais:
- `200` em consultas e atualizações
- `201` em criações
- `400` em payload inválido, filtros inconsistentes ou paginação inválida
- `401` em falha de autenticação
- `403` em falha de ownership/perfil
- `404` em recurso não encontrado
- `422` em regra de negócio violada

Exemplo de alerta sanitário:

```http
GET /api/v1/goatfarms/1/health-events/alerts?windowDays=7
```

```json
{
  "dueTodayCount": 2,
  "upcomingCount": 5,
  "overdueCount": 1,
  "windowDays": 7
}
```

### Idempotência de comandos
Para endpoints que exigem idempotência (ex.: `POST /api/v1/goatfarms/{farmId}/inventory/movements`):
- Header obrigatório: `Idempotency-Key`.
- Primeira execução válida: `201 Created`.
- Mesma key + payload equivalente: replay (`200` com resposta persistida).
- Mesma key + payload diferente: `409 Conflict`.
- Ausência de key: `400 Bad Request`.

### Inventory (itens de estoque)
Para `POST /api/v1/goatfarms/{farmId}/inventory/items`:
- Resposta de criação: `201 Created`.
- Nome duplicado na mesma fazenda: `409 Conflict`.
- Listagem paginada: `GET /api/v1/goatfarms/{farmId}/inventory/items`.

### Inventory (consultas)
- `GET /api/v1/goatfarms/{farmId}/inventory/balances`
  - filtros opcionais: `itemId`, `lotId`, `activeOnly`
  - paginação padrão: `page`, `size`, `sort`
  - resposta paginada com `itemId`, `itemName`, `trackLot`, `lotId`, `quantity`
- `GET /api/v1/goatfarms/{farmId}/inventory/movements`
  - filtros opcionais: `itemId`, `lotId`, `type`, `fromDate`, `toDate`
  - ordenação padrão: `movementDate desc`, `createdAt desc`
  - resposta paginada com `movementId`, `type`, `adjustDirection`, `quantity`, `itemId`, `itemName`, `lotId`, `movementDate`, `reason`, `resultingBalance`, `createdAt`
- validações obrigatórias:
  - `fromDate <= toDate`
  - `size <= 100`

Exemplo de consulta de saldos:

```http
GET /api/v1/goatfarms/1/inventory/balances?itemId=101&page=0&size=20
```

Exemplo de consulta de histórico:

```http
GET /api/v1/goatfarms/1/inventory/movements?type=OUT&fromDate=2026-02-01&toDate=2026-02-28&page=0&size=20
```

## Erros/Status
### Estrutura de erro padrão
Erros seguem estrutura `ValidationError`:

```json
{
  "timestamp": "2026-02-18T10:00:00Z",
  "status": 422,
  "error": "Regra de negócio violada",
  "path": "/api/v1/goatfarms/1/inventory/movements",
  "errors": [
    {
      "fieldName": "quantity",
      "message": "Saldo insuficiente para realizar a movimentação"
    }
  ]
}
```

### Mapeamento principal de status
| Status | Origem típica |
|---|---|
| `400 Bad Request` | `InvalidArgumentException`, `IllegalArgumentException`, JSON inválido |
| `401 Unauthorized` | falha de autenticação/token |
| `403 Forbidden` | falha de ownership/perfil |
| `404 Not Found` | `ResourceNotFoundException` |
| `405 Method Not Allowed` | método HTTP não suportado |
| `409 Conflict` | `DuplicateEntityException`, `DataIntegrityViolationException` |
| `415 Unsupported Media Type` | content type não suportado |
| `422 Unprocessable Entity` | `BusinessRuleException`, validação de bean |
| `500 Internal Server Error` | erro não tratado |

## Referências internas
- Handler global: [src/main/java/com/devmaster/goatfarm/config/exceptions/GlobalExceptionHandler.java](../../src/main/java/com/devmaster/goatfarm/config/exceptions/GlobalExceptionHandler.java)
- Entry point 401: [src/main/java/com/devmaster/goatfarm/config/security/CustomAuthenticationEntryPoint.java](../../src/main/java/com/devmaster/goatfarm/config/security/CustomAuthenticationEntryPoint.java)
- Handler 403: [src/main/java/com/devmaster/goatfarm/config/security/CustomAccessDeniedHandler.java](../../src/main/java/com/devmaster/goatfarm/config/security/CustomAccessDeniedHandler.java)
- Módulos oficiais: [../02-modules](../02-modules)