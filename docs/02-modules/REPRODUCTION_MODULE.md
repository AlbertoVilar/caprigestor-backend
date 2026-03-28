# Modulo Reproduction
Ultima atualizacao: 2026-03-28
Escopo: cobertura, diagnostico, acompanhamento, alertas e encerramento de gestacoes.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Modulo Lactacao](./LACTATION_MODULE.md), [Guia de Migracao](../03-api/API_VERSIONING_MIGRATION_GUIDE.md)

## Visao geral
O modulo `reproduction` controla eventos de cobertura, checks de prenhez, status da gestacao e alertas farm-level para diagnostico pendente.

## Cobertura: regra ativa do ciclo
- Coberturas validas em data posterior sao permitidas e preservam o historico do animal.
- A referencia ativa para diagnostico, alertas e marcos do ciclo e sempre a cobertura efetiva mais recente na data de referencia.
- Duplicidade de cobertura efetiva na mesma data retorna `422` e orienta o uso da correcao de cobertura.
- Correcao operacional deve usar `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding/{coverageEventId}/corrections`; nao deve ser simulada com nova cobertura retroativa.
- O endpoint `GET /pregnancies/diagnosis-recommendation` resolve a cobertura efetiva mais recente de forma deterministica mesmo quando existe mais de uma cobertura historica valida.

## Diagnostico de prenhez: regras atuais
- O endpoint `PATCH /pregnancies/confirm` so aceita o diagnostico a partir de 60 dias apos a ultima cobertura efetiva.
- Antes desse marco, o backend responde `422` e mantem a gestacao sem confirmacao.
- O endpoint `GET /pregnancies/diagnosis-recommendation` informa quando a cabra entra no estado `ELIGIBLE_PENDING`.
- O alerta farm-level `GET /reproduction/alerts/pregnancy-diagnosis` reflete essa mesma elegibilidade e expoe `eligibleDate` e `daysOverdue`.
- Depois da confirmacao positiva, o contexto `milk` passa a considerar a secagem recomendada aos 90 dias de gestacao.

## Regras / Contratos
- Base por cabra: `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction`.
- Base de alertas por fazenda: `/api/v1/goatfarms/{farmId}/reproduction/alerts`.
- Ownership por `farmId` em todas as rotas do modulo.
- Fluxos de gestacao seguem estado de dominio (ativa, encerrada, motivo de encerramento).
- Integracao com `milk` ocorre por shared kernel (snapshot de prenhez), sem acoplamento direto de entidades.
- Compatibilidade temporaria: `/api/...` segue ativo por 1 ciclo como **DEPRECATED** (remocao planejada: 2026-06-30, v2.0.0).

## Endpoints
### Escopo por cabra
Base URL: `/api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction`

| Metodo | URL | Query params | Retorno |
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
  "checkDate": "2026-03-01",
  "checkResult": "POSITIVE",
  "notes": "Ultrassom"
}
```

Observacao operacional:

- se `checkDate` ainda estiver antes de 60 dias da ultima cobertura, o backend retorna `422`;
- se a cobertura ja tiver sido consumida por uma gestacao anterior, a confirmacao tambem retorna `422`.

### Escopo por fazenda (alertas)
Base URL: `/api/v1/goatfarms/{farmId}/reproduction/alerts`

| Metodo | URL | Query params | Retorno |
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

## Compatibilidade e paginacao
- As rotas canonicas sao sempre publicadas em `/api/v1/...`.
- O legado `/api/...` segue ativo apenas por compatibilidade temporaria.
- Os historicos por cabra (`events` e `pregnancies`) continuam retornando `Page` do Spring para preservar compatibilidade com consumidores ja publicados.
- O endpoint de alertas retorna um envelope agregado proprio (`totalPending` + `alerts`) e nao deve ser reinterpretado como `Page` no cliente.

## Erros/Status
- `400`: payload invalido, parametro invalido ou paginacao inconsistente.
- `401`: autenticacao ausente ou invalida.
- `403`: ownership/perfil insuficiente.
- `404`: recurso de reproducao nao encontrado.
- `422`: regra de negocio violada (transicao de estado, consistencia de datas, diagnostico antes de 60 dias etc.).
- Padrao de payload de erro: [API_CONTRACTS](../03-api/API_CONTRACTS.md).

## Referencias internas
- Controller principal: [src/main/java/com/devmaster/goatfarm/reproduction/api/controller/ReproductionController.java](../../src/main/java/com/devmaster/goatfarm/reproduction/api/controller/ReproductionController.java)
- Controller de alertas: [src/main/java/com/devmaster/goatfarm/reproduction/api/controller/FarmReproductionAlertsController.java](../../src/main/java/com/devmaster/goatfarm/reproduction/api/controller/FarmReproductionAlertsController.java)
- Testes de controller: [src/test/java/com/devmaster/goatfarm/reproduction/api/controller](../../src/test/java/com/devmaster/goatfarm/reproduction/api/controller)
