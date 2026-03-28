# Modulo Lactacao
Ultima atualizacao: 2026-03-28
Escopo: abertura, secagem, retomada, consulta de lactacoes e alertas de secagem por fazenda.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Modulo Milk Production](./MILK_PRODUCTION_MODULE.md), [Guia de Migracao](../03-api/API_VERSIONING_MIGRATION_GUIDE.md)

## Visao geral
O modulo de lactacao pertence ao contexto `milk` e controla o ciclo produtivo da cabra (abertura, historico, sumarios, secagem e retomada operacional quando a prenhez deixa de estar ativa sem parto).

## Regras operacionais atuais
- `ACTIVE`: lactacao em producao, apta a receber registros de leite.
- `DRY`: secagem confirmada. Nao existe lactacao ativa para producao enquanto o animal estiver seco.
- `CLOSED`: encerramento definitivo do ciclo, diferente da secagem operacional.
- Secagem confirmada nao abre um novo ciclo por si so. Ela apenas interrompe a producao atual.
- Com prenhez ativa apos secagem confirmada:
  - nao pode registrar producao de leite;
  - nao pode abrir nova lactacao;
  - nao pode retomar a lactacao secada.
- Se a prenhez for encerrada sem parto (por exemplo: `FALSE_POSITIVE`, `ABORTION`, `LOSS`), a mesma lactacao `DRY` pode ser retomada.
- Se a prenhez terminar em `BIRTH`, o caminho correto e iniciar uma nova lactacao, nao retomar a anterior.
- O resumo da lactacao ativa usa `pregnancy.recommendedDryOffDate` e `pregnancy.dryOffRecommendation` para orientar a secagem.

## Regras / Contratos
- Base principal por cabra: `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations`.
- Abertura exige `startDate`.
- Secagem (`dry`) exige `endDate` e move o ciclo para `DRY`.
- Retomada (`resume`) so e aceita para uma lactacao `DRY` cuja prenhez nao esteja mais ativa.
- Consultas de sumario combinam dados da lactacao, producao e recomendacao de secagem.
- Rotas sao farm-level com ownership por `farmId`.
- Compatibilidade temporaria: `/api/...` segue ativo por 1 ciclo como **DEPRECATED** (remocao planejada: 2026-06-30, v2.0.0).

## Endpoints
### Escopo por cabra
Base URL: `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations`

| Metodo | URL | Query params | Retorno |
|---|---|---|---|
| `POST` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations` | - | `201 Created` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/active` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/active/summary` | - | `200 OK` |
| `PATCH` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}/dry` | - | `200 OK` |
| `PATCH` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}/resume` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}/summary` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/lactations` | `page`, `size`, `sort` | `200 OK` (`Page` do Spring) |

Exemplo curto (abrir lactacao):

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

Exemplo curto (retomar lactacao):

```http
PATCH /api/v1/goatfarms/1/goats/BR123/lactations/120/resume
```

Retornos esperados:

- `200 OK` quando a lactacao estiver `DRY` e a prenhez correspondente nao estiver mais ativa;
- `422` quando ainda houver prenhez ativa;
- `422` quando a ultima prenhez tiver sido encerrada com `BIRTH`.

### Escopo por fazenda (alertas)
Base URL: `/api/v1/goatfarms/{farmId}/milk/alerts`

| Metodo | URL | Query params | Retorno |
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

## Compatibilidade e paginacao
- As rotas canonicas sao sempre publicadas em `/api/v1/...`.
- O legado `/api/...` segue ativo apenas por compatibilidade temporaria.
- O historico de lactacoes continua retornando `Page` do Spring para preservar compatibilidade com consumidores ja publicados.
- O endpoint de alertas retorna um envelope agregado proprio (`totalPending` + `alerts`).

## Erros/Status
- `400`: validacao de payload, parametros invalidos ou paginacao inconsistente.
- `401`: autenticacao invalida ou ausente.
- `403`: ownership/perfil insuficiente.
- `404`: lactacao nao encontrada.
- `422`: regra de negocio violada (ex.: tentativa de nova lactacao ou retomada com prenhez ativa apos secagem confirmada).
- Padrao de payload de erro: [API_CONTRACTS](../03-api/API_CONTRACTS.md).

## Referencias internas
- Controller de lactacao: [src/main/java/com/devmaster/goatfarm/milk/api/controller/LactationController.java](../../src/main/java/com/devmaster/goatfarm/milk/api/controller/LactationController.java)
- Controller de alertas: [src/main/java/com/devmaster/goatfarm/milk/api/controller/FarmMilkAlertsController.java](../../src/main/java/com/devmaster/goatfarm/milk/api/controller/FarmMilkAlertsController.java)
- DTOs: [src/main/java/com/devmaster/goatfarm/milk/api/dto](../../src/main/java/com/devmaster/goatfarm/milk/api/dto)
