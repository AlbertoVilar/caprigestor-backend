# Modulo Lactacao
Ultima atualizacao: 2026-02-10
Escopo: abertura, encerramento, consulta de lactacoes e alertas de secagem por fazenda.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Modulo Milk Production](./MILK_PRODUCTION_MODULE.md)

## Visao geral
O modulo de lactacao pertence ao contexto `milk` e controla o ciclo produtivo da cabra (abertura, historico, sumarios e secagem).

## Regras / Contratos
- Base principal por cabra: `/api/goatfarms/{farmId}/goats/{goatId}/lactations`.
- Abertura exige `startDate`.
- Encerramento (dry) exige `endDate`.
- Consultas de sumario combinam dados da lactacao, producao e recomendacao de secagem.
- Rotas sao farm-level com ownership por `farmId`.

## Endpoints
### Escopo por cabra
Base URL: `/api/goatfarms/{farmId}/goats/{goatId}/lactations`

| Metodo | URL | Query params | Retorno |
|---|---|---|---|
| `POST` | `/api/goatfarms/{farmId}/goats/{goatId}/lactations` | - | `201 Created` |
| `GET` | `/api/goatfarms/{farmId}/goats/{goatId}/lactations/active` | - | `200 OK` |
| `GET` | `/api/goatfarms/{farmId}/goats/{goatId}/lactations/active/summary` | - | `200 OK` |
| `PATCH` | `/api/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}/dry` | - | `200 OK` |
| `GET` | `/api/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}` | - | `200 OK` |
| `GET` | `/api/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}/summary` | - | `200 OK` |
| `GET` | `/api/goatfarms/{farmId}/goats/{goatId}/lactations` | `page`, `size`, `sort` | `200 OK` (pagina) |

Contrato curto (abrir lactacao):
- URL: `POST /api/goatfarms/1/goats/BR123/lactations`
- Request curto:

```json
{
  "startDate": "2026-01-01"
}
```

- Response curto:

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
Base URL: `/api/goatfarms/{farmId}/milk/alerts`

| Metodo | URL | Query params | Retorno |
|---|---|---|---|
| `GET` | `/api/goatfarms/{farmId}/milk/alerts/dry-off` | `referenceDate`, `page`, `size` | `200 OK` (alertas agregados) |

Contrato curto (dry-off alerts):
- URL: `GET /api/goatfarms/1/milk/alerts/dry-off?referenceDate=2026-02-10&page=0&size=20`
- Response curto:

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

## Fluxos principais
1. Abertura de ciclo:
   cria lactacao ativa para cabra/fazenda.
2. Encerramento de ciclo:
   `PATCH .../dry` fecha lactacao com `endDate`.
3. Monitoramento:
   sumarios e alertas orientam momento de secagem com base em dados de producao e prenhez.

Observacoes de performance:
- Historico por cabra e paginado.
- Alertas farm-level retornam pagina agregada, evitando carga total em uma unica resposta.

## Erros/Status
- `400`: validacao de payload ou parametros.
- `401`: autenticacao invalida/ausente.
- `403`: ownership/perfil insuficiente.
- `404`: lactacao nao encontrada.
- `422`: regra de negocio violada (ex.: tentativa de transicao invalida).
- Padrao de payload de erro: [API_CONTRACTS](../03-api/API_CONTRACTS.md).

## Referencias internas
- Controller lactacao: [src/main/java/com/devmaster/goatfarm/milk/api/controller/LactationController.java](../../src/main/java/com/devmaster/goatfarm/milk/api/controller/LactationController.java)
- Controller alertas: [src/main/java/com/devmaster/goatfarm/milk/api/controller/FarmMilkAlertsController.java](../../src/main/java/com/devmaster/goatfarm/milk/api/controller/FarmMilkAlertsController.java)
- DTOs lactacao: [src/main/java/com/devmaster/goatfarm/milk/api/dto](../../src/main/java/com/devmaster/goatfarm/milk/api/dto)
