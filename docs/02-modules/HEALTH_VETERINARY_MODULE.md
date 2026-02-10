# Modulo Saude e Veterinario
Ultima atualizacao: 2026-02-10
Escopo: eventos sanitarios por cabra e consultas agregadas por fazenda.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Dominio](../00-overview/BUSINESS_DOMAIN.md)

## Visao geral
O modulo `health` registra, atualiza e consulta eventos de saude (vacina, medicacao, procedimento e ocorrencias), com escopo farm-level e validacao de ownership.

## Regras / Contratos
- Status de evento: `AGENDADO`, `REALIZADO`, `CANCELADO`.
- Tipos principais: `VACINA`, `VERMIFUGACAO`, `MEDICACAO`, `PROCEDIMENTO`, `DOENCA`.
- Ownership obrigatorio nas rotas farm-level (`@ownershipService.canManageFarm`).
- Reabertura (`/{eventId}/reopen`) exige ownership e papel `ADMIN` ou `FARM_OWNER`.
- Indicador `overdue` e derivado (nao e payload de entrada).
- Alertas de fazenda limitam `windowDays` para faixa segura (`1..30`).

## Endpoints
### Escopo por cabra
Base URL: `/api/goatfarms/{farmId}/goats/{goatId}/health-events`

| Metodo | URL | Query params | Retorno |
|---|---|---|---|
| `POST` | `/api/goatfarms/{farmId}/goats/{goatId}/health-events` | - | `201 Created` |
| `PUT` | `/api/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}` | - | `200 OK` |
| `PATCH` | `/api/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/done` | - | `200 OK` |
| `PATCH` | `/api/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/cancel` | - | `200 OK` |
| `PATCH` | `/api/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/reopen` | - | `200 OK` |
| `GET` | `/api/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}` | - | `200 OK` |
| `GET` | `/api/goatfarms/{farmId}/goats/{goatId}/health-events` | `from`, `to`, `type`, `status`, `page`, `size`, `sort` | `200 OK` (pagina) |

Contrato curto (criacao):
- URL: `POST /api/goatfarms/1/goats/BR123/health-events`
- Request curto:

```json
{
  "type": "VACINA",
  "title": "Vacina clostridiose",
  "scheduledDate": "2026-02-15",
  "notes": "Aplicar dose reforco"
}
```

- Response curto:

```json
{
  "id": 10,
  "farmId": 1,
  "goatId": "BR123",
  "status": "AGENDADO",
  "type": "VACINA"
}
```

Contrato curto (marcar como realizado):
- URL: `PATCH /api/goatfarms/1/goats/BR123/health-events/10/done`
- Request curto:

```json
{
  "performedAt": "2026-02-15T08:30:00",
  "responsible": "Tecnico A",
  "notes": "Sem intercorrencias"
}
```

### Escopo por fazenda
Base URL: `/api/goatfarms/{farmId}/health-events`

| Metodo | URL | Query params | Retorno |
|---|---|---|---|
| `GET` | `/api/goatfarms/{farmId}/health-events/calendar` | `from`, `to`, `type`, `status`, `page`, `size`, `sort` | `200 OK` (agenda paginada) |
| `GET` | `/api/goatfarms/{farmId}/health-events/alerts` | `windowDays` | `200 OK` (contadores + top 5) |

Contrato curto (alerts):
- URL: `GET /api/goatfarms/1/health-events/alerts?windowDays=7`
- Response curto:

```json
{
  "dueTodayCount": 2,
  "upcomingCount": 5,
  "overdueCount": 1,
  "windowDays": 7
}
```

## Fluxos principais
1. Agendamento:
   cria evento `AGENDADO` para cabra da fazenda.
2. Execucao:
   `done` grava data/hora de execucao e responsavel.
3. Cancelamento/Reabertura:
   `cancel` encerra o compromisso; `reopen` retorna para `AGENDADO` com regra de perfil.
4. Visao de fazenda:
   `calendar` lista agenda por periodo e `alerts` agrega pendencias.

Observacoes de performance:
- Consultas de lista sao paginadas por fazenda/cabra.
- Endpoint de alertas retorna contadores e listas reduzidas (top 5 por categoria), evitando varrer todo historico no cliente.

## Erros/Status
- `400`: parametros invalidos (datas, enums, formatos).
- `401`: token ausente/invalido.
- `403`: ownership/perfil insuficiente.
- `404`: evento nao encontrado para `farmId/goatId/eventId`.
- `422`: regra de negocio violada (ex.: transicao de status invalida).
- Padrao de payload de erro: [API_CONTRACTS](../03-api/API_CONTRACTS.md).

## Referencias internas
- Controller por cabra: [src/main/java/com/devmaster/goatfarm/health/api/controller/HealthEventController.java](../../src/main/java/com/devmaster/goatfarm/health/api/controller/HealthEventController.java)
- Controller por fazenda: [src/main/java/com/devmaster/goatfarm/health/api/controller/FarmHealthEventController.java](../../src/main/java/com/devmaster/goatfarm/health/api/controller/FarmHealthEventController.java)
- DTOs de entrada/saida: [src/main/java/com/devmaster/goatfarm/health/api/dto](../../src/main/java/com/devmaster/goatfarm/health/api/dto)
