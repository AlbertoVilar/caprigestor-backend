# Módulo Lactação
Última atualização: 2026-02-28
Escopo: abertura, encerramento, consulta de lactações e alertas de secagem por fazenda.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Módulo Milk Production](./MILK_PRODUCTION_MODULE.md), [Guia de Migração](../03-api/API_VERSIONING_MIGRATION_GUIDE.md)

## Visão geral
O módulo de lactação pertence ao contexto `milk` e controla o ciclo produtivo da cabra (abertura, histórico, sumários e secagem).

## Regras / Contratos
- Base principal por cabra: `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations`.
- Abertura exige `startDate`.
- Encerramento (`dry`) exige `endDate`.
- Consultas de sumário combinam dados da lactação, produção e recomendação de secagem.
- Rotas são farm-level com ownership por `farmId`.
- Compatibilidade temporária: `/api/...` segue ativo por 1 ciclo como **DEPRECATED** (remoção planejada: 2026-06-30, v2.0.0).

## Endpoints
### Escopo por cabra
Base URL: `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations`

| Método | URL | Query params | Retorno |
|---|---|---|---|
| `POST` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations` | - | `201 Created` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/active` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/active/summary` | - | `200 OK` |
| `PATCH` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}/dry` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}/summary` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations` | `page`, `size`, `sort` | `200 OK` (`Page` do Spring) |

Exemplo curto (abrir lactação):

```http
POST /api/v1/goatfarms/1/goats/BR123/lactations
Content-Type: application/json
```

```json
{
  "startDate": "2026-01-01"
}
```

```json
{
  "id": 120,
  "farmId": 1,
  "goatId": "BR123",
  "status": "ACTIVE",
  "startDate": "2026-01-01"
}
```

### Escopo por fazenda (alertas)
Base URL: `/api/v1/goatfarms/{farmId}/milk/alerts`

| Método | URL | Query params | Retorno |
|---|---|---|---|
| `GET` | `/api/v1/goatfarms/{farmId}/milk/alerts/dry-off` | `referenceDate`, `page`, `size` | `200 OK` (agregado com `totalPending` + `alerts`) |

Exemplo curto (dry-off alerts):

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

## Compatibilidade e paginação
- As rotas canônicas são sempre publicadas em `/api/v1/...`.
- O legado `/api/...` segue ativo apenas por compatibilidade temporária.
- O histórico de lactações continua retornando `Page` do Spring para preservar compatibilidade com consumidores já publicados.
- O endpoint de alertas retorna um envelope agregado próprio (`totalPending` + `alerts`).

## Erros/Status
- `400`: validação de payload, parâmetros inválidos ou paginação inconsistente.
- `401`: autenticação inválida ou ausente.
- `403`: ownership/perfil insuficiente.
- `404`: lactação não encontrada.
- `422`: regra de negócio violada (ex.: tentativa de transição inválida).
- Padrão de payload de erro: [API_CONTRACTS](../03-api/API_CONTRACTS.md).

## Referências internas
- Controller de lactação: [src/main/java/com/devmaster/goatfarm/milk/api/controller/LactationController.java](../../src/main/java/com/devmaster/goatfarm/milk/api/controller/LactationController.java)
- Controller de alertas: [src/main/java/com/devmaster/goatfarm/milk/api/controller/FarmMilkAlertsController.java](../../src/main/java/com/devmaster/goatfarm/milk/api/controller/FarmMilkAlertsController.java)
- DTOs: [src/main/java/com/devmaster/goatfarm/milk/api/dto](../../src/main/java/com/devmaster/goatfarm/milk/api/dto)