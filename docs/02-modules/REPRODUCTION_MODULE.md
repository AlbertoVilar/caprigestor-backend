# Módulo Reproduction
Última atualização: 2026-02-28
Escopo: cobertura, diagnóstico, acompanhamento e encerramento de gestações.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Módulo Lactação](./LACTATION_MODULE.md), [Guia de Migração](../03-api/API_VERSIONING_MIGRATION_GUIDE.md)

## Visão geral
O módulo `reproduction` controla eventos de cobertura, checks de prenhez, status da gestação e alertas farm-level para diagnóstico pendente.

## Cobertura: regra ativa do ciclo
- Coberturas válidas em data posterior são permitidas e preservam o histórico do animal.
- A referência ativa para diagnóstico, alertas e marcos do ciclo é sempre a cobertura efetiva mais recente na data de referência.
- Duplicidade de cobertura efetiva na mesma data retorna `422` e orienta o uso da correção de cobertura.
- Correção operacional deve usar `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding/{coverageEventId}/corrections`; não deve ser simulada com nova cobertura retroativa.
- O endpoint `GET /pregnancies/diagnosis-recommendation` resolve a cobertura efetiva mais recente de forma determinística mesmo quando existe mais de uma cobertura histórica válida.

## Regras / Contratos
- Base por cabra: `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction`.
- Base de alertas por fazenda: `/api/v1/goatfarms/{farmId}/reproduction/alerts`.
- Ownership por `farmId` em todas as rotas do módulo.
- Fluxos de gestação seguem estado de domínio (ativa, encerrada, motivo de encerramento).
- Integração com `milk` ocorre por shared kernel (snapshot de prenhez), sem acoplamento direto de entidades.
- Compatibilidade temporária: `/api/...` segue ativo por 1 ciclo como **DEPRECATED** (remoção planejada: 2026-06-30, v2.0.0).

## Endpoints
### Escopo por cabra
Base URL: `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction`

| Método | URL | Query params | Retorno |
|---|---|---|---|
| `POST` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding` | - | `201 Created` |
| `POST` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding/{coverageEventId}/corrections` | - | `201 Created` |
| `PATCH` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/confirm` | - | `200 OK` |
| `POST` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/checks` | - | `201 Created` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/active` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}` | - | `200 OK` |
| `PATCH` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}/close` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/events` | `page`, `size`, `sort` | `200 OK` (`Page` do Spring) |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies` | `page`, `size`, `sort` | `200 OK` (`Page` do Spring) |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/diagnosis-recommendation` | `referenceDate` | `200 OK` |

Exemplo curto (registrar cobertura):

```http
POST /api/v1/goatfarms/1/goats/BR123/reproduction/breeding
Content-Type: application/json
```

```json
{
  "eventDate": "2026-01-10",
  "breedingType": "NATURAL",
  "breederRef": "BODE-08"
}
```

```json
{
  "id": 501,
  "farmId": 1,
  "goatId": "BR123",
  "eventType": "BREEDING",
  "eventDate": "2026-01-10"
}
```

Exemplo curto (confirmar prenhez):

```http
PATCH /api/v1/goatfarms/1/goats/BR123/reproduction/pregnancies/confirm
Content-Type: application/json
```

```json
{
  "checkDate": "2026-02-08",
  "checkResult": "POSITIVE",
  "notes": "Ultrassom"
}
```

### Escopo por fazenda (alertas)
Base URL: `/api/v1/goatfarms/{farmId}/reproduction/alerts`

| Método | URL | Query params | Retorno |
|---|---|---|---|
| `GET` | `/api/v1/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis` | `referenceDate`, `page`, `size` | `200 OK` (agregado com `totalPending` + `alerts`) |

Exemplo curto (alertas):

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

## Compatibilidade e paginação
- As rotas canônicas são sempre publicadas em `/api/v1/...`.
- O legado `/api/...` segue ativo apenas por compatibilidade temporária.
- Os históricos por cabra (`events` e `pregnancies`) continuam retornando `Page` do Spring para preservar compatibilidade com consumidores já publicados.
- O endpoint de alertas retorna um envelope agregado próprio (`totalPending` + `alerts`) e não deve ser reinterpretado como `Page` no cliente.

## Erros/Status
- `400`: payload inválido, parâmetro inválido ou paginação inconsistente.
- `401`: autenticação ausente ou inválida.
- `403`: ownership/perfil insuficiente.
- `404`: recurso de reprodução não encontrado.
- `422`: regra de negócio violada (transição de estado, consistência de datas etc.).
- Padrão de payload de erro: [API_CONTRACTS](../03-api/API_CONTRACTS.md).

## Referências internas
- Controller principal: [src/main/java/com/devmaster/goatfarm/reproduction/api/controller/ReproductionController.java](../../src/main/java/com/devmaster/goatfarm/reproduction/api/controller/ReproductionController.java)
- Controller de alertas: [src/main/java/com/devmaster/goatfarm/reproduction/api/controller/FarmReproductionAlertsController.java](../../src/main/java/com/devmaster/goatfarm/reproduction/api/controller/FarmReproductionAlertsController.java)
- Testes de controller: [src/test/java/com/devmaster/goatfarm/reproduction/api/controller](../../src/test/java/com/devmaster/goatfarm/reproduction/api/controller)
