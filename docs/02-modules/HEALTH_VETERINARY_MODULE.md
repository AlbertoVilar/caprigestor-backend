# Módulo Saúde e Veterinário
Última atualização: 2026-02-28
Escopo: eventos sanitários por cabra e consultas agregadas por fazenda.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Domínio](../00-overview/BUSINESS_DOMAIN.md)

## Visão geral
O módulo `health` registra, atualiza e consulta eventos de saúde (vacina, medicação, procedimento e ocorrências), com escopo farm-level e validação de ownership.

## Regras / Contratos
- Status de evento: `AGENDADO`, `REALIZADO`, `CANCELADO`.
- Tipos principais: `VACINA`, `VERMIFUGACAO`, `MEDICACAO`, `PROCEDIMENTO`, `DOENCA`.
- Ownership obrigatório nas rotas farm-level (`@ownershipService.canManageFarm`).
- Reabertura (`/{eventId}/reopen`) exige ownership e papel `ADMIN` ou `FARM_OWNER`.
- Indicador `overdue` é derivado (não é payload de entrada).
- Alertas de fazenda limitam `windowDays` para faixa segura (`1..30`).
- Compatibilidade temporária: `/api/...` segue ativo por 1 ciclo como **DEPRECATED** (remoção planejada: 2026-06-30, v2.0.0).

## Endpoints
### Escopo por cabra
Base URL: `/api/v1/goatfarms/{farmId}/goats/{goatId}/health-events`

| Método | URL | Query params | Retorno |
|---|---|---|---|
| `POST` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/health-events` | - | `201 Created` |
| `PUT` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}` | - | `200 OK` |
| `PATCH` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/done` | - | `200 OK` |
| `PATCH` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/cancel` | - | `200 OK` |
| `PATCH` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/reopen` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/health-events` | `from`, `to`, `type`, `status`, `page`, `size`, `sort` | `200 OK` (`Page` do Spring) |

Exemplo curto (criação):

```http
POST /api/v1/goatfarms/1/goats/BR123/health-events
Content-Type: application/json
```

```json
{
  "type": "VACINA",
  "title": "Vacina clostridiose",
  "scheduledDate": "2026-02-15",
  "notes": "Aplicar dose de reforço"
}
```

```json
{
  "id": 10,
  "farmId": 1,
  "goatId": "BR123",
  "status": "AGENDADO",
  "type": "VACINA"
}
```

Exemplo curto (marcar como realizado):

```http
PATCH /api/v1/goatfarms/1/goats/BR123/health-events/10/done
Content-Type: application/json
```

```json
{
  "performedAt": "2026-02-15T08:30:00",
  "responsible": "Técnico A",
  "notes": "Sem intercorrências"
}
```

### Escopo por fazenda
Base URL: `/api/v1/goatfarms/{farmId}/health-events`

| Método | URL | Query params | Retorno |
|---|---|---|---|
| `GET` | `/api/v1/goatfarms/{farmId}/health-events/calendar` | `from`, `to`, `type`, `status`, `page`, `size`, `sort` | `200 OK` (`Page` do Spring) |
| `GET` | `/api/v1/goatfarms/{farmId}/health-events/alerts` | `windowDays` | `200 OK` (contadores + top 5) |

Exemplo curto (alerts):

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

## Compatibilidade e paginação
- As rotas canônicas são sempre publicadas em `/api/v1/...`.
- O legado `/api/...` segue ativo apenas por compatibilidade temporária.
- As listagens `health-events` e `calendar` continuam retornando `Page` do Spring para preservar compatibilidade.
- O endpoint `alerts` retorna um agregado de contadores e listas reduzidas (top 5 por categoria).

## Erros/Status
- `400`: parâmetros inválidos (datas, enums, formatos).
- `401`: token ausente ou inválido.
- `403`: ownership/perfil insuficiente.
- `404`: evento não encontrado para `farmId/goatId/eventId`.
- `422`: regra de negócio violada (ex.: transição de status inválida).
- Padrão de payload de erro: [API_CONTRACTS](../03-api/API_CONTRACTS.md).

## Referências internas
- Controller por cabra: [src/main/java/com/devmaster/goatfarm/health/api/controller/HealthEventController.java](../../src/main/java/com/devmaster/goatfarm/health/api/controller/HealthEventController.java)
- Controller por fazenda: [src/main/java/com/devmaster/goatfarm/health/api/controller/FarmHealthEventController.java](../../src/main/java/com/devmaster/goatfarm/health/api/controller/FarmHealthEventController.java)
- DTOs de entrada/saída: [src/main/java/com/devmaster/goatfarm/health/api/dto](../../src/main/java/com/devmaster/goatfarm/health/api/dto)