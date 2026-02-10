# Modulo Reproduction
Ultima atualizacao: 2026-02-10
Escopo: cobertura, diagnostico, acompanhamento e encerramento de gestacoes.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Modulo Lactacao](./LACTATION_MODULE.md)

## Visao geral
O modulo `reproduction` controla eventos de cobertura, checks de prenhez, status da gestacao e alertas farm-level para diagnostico pendente.

## Regras / Contratos
- Base por cabra: `/api/goatfarms/{farmId}/goats/{goatId}/reproduction`.
- Base de alertas por fazenda: `/api/goatfarms/{farmId}/reproduction/alerts`.
- Ownership por `farmId` em todas as rotas do modulo.
- Fluxos de gestacao seguem estado de dominio (ativa, encerrada, motivo de encerramento).
- Integracao com `milk` ocorre por shared kernel (snapshot de prenhez), sem acoplamento direto de entidades.

## Endpoints
### Escopo por cabra
Base URL: `/api/goatfarms/{farmId}/goats/{goatId}/reproduction`

| Metodo | URL | Query params | Retorno |
|---|---|---|---|
| `POST` | `/api/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding` | - | `201 Created` |
| `POST` | `/api/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding/{coverageEventId}/corrections` | - | `201 Created` |
| `PATCH` | `/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/confirm` | - | `200 OK` |
| `POST` | `/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/checks` | - | `201 Created` |
| `GET` | `/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/active` | - | `200 OK` |
| `GET` | `/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}` | - | `200 OK` |
| `PATCH` | `/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}/close` | - | `200 OK` |
| `GET` | `/api/goatfarms/{farmId}/goats/{goatId}/reproduction/events` | `page`, `size`, `sort` | `200 OK` (pagina) |
| `GET` | `/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies` | `page`, `size`, `sort` | `200 OK` (pagina) |
| `GET` | `/api/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/diagnosis-recommendation` | `referenceDate` | `200 OK` |

Contrato curto (registrar cobertura):
- URL: `POST /api/goatfarms/1/goats/BR123/reproduction/breeding`
- Request curto:

```json
{
  "eventDate": "2026-01-10",
  "breedingType": "NATURAL",
  "breederRef": "BODE-08"
}
```

- Response curto:

```json
{
  "id": 501,
  "farmId": 1,
  "goatId": "BR123",
  "eventType": "BREEDING",
  "eventDate": "2026-01-10"
}
```

Contrato curto (confirmar prenhez):
- URL: `PATCH /api/goatfarms/1/goats/BR123/reproduction/pregnancies/confirm`
- Request curto:

```json
{
  "checkDate": "2026-02-08",
  "checkResult": "POSITIVE",
  "notes": "ultrassom"
}
```

### Escopo por fazenda (alertas)
Base URL: `/api/goatfarms/{farmId}/reproduction/alerts`

| Metodo | URL | Query params | Retorno |
|---|---|---|---|
| `GET` | `/api/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis` | `referenceDate`, `page`, `size` | `200 OK` (pendencias agregadas) |

Contrato curto (alertas):
- URL: `GET /api/goatfarms/1/reproduction/alerts/pregnancy-diagnosis?referenceDate=2026-02-08&page=0&size=20`
- Response curto:

```json
{
  "totalPending": 2,
  "alerts": [
    {
      "goatId": "BR123",
      "eligibleDate": "2026-02-05",
      "daysOverdue": 3
    }
  ]
}
```

## Fluxos principais
1. Cobertura:
   registra evento base do ciclo reprodutivo.
2. Diagnostico:
   check positivo confirma gestacao; check negativo registra evento e pode encerrar ativo indevido.
3. Encerramento:
   `close` fecha gestacao com data/motivo (parto, perda, aborto etc.).
4. Operacao de fazenda:
   alertas agregam animais com diagnostico pendente para acao operacional.

Observacoes de performance:
- Historicos (`events`, `pregnancies`) sao paginados por cabra.
- Alertas farm-level retornam pagina agregada, com filtros por data de referencia.

## Erros/Status
- `400`: payload invalido/parametro invalido.
- `401`: autenticacao ausente/invalida.
- `403`: ownership/perfil insuficiente.
- `404`: recurso de reproducao nao encontrado.
- `422`: regra de negocio violada (transicao de estado, consistencia de datas etc.).
- Padrao de payload de erro: [API_CONTRACTS](../03-api/API_CONTRACTS.md).

## Referencias internas
- Controller principal: [src/main/java/com/devmaster/goatfarm/reproduction/api/controller/ReproductionController.java](../../src/main/java/com/devmaster/goatfarm/reproduction/api/controller/ReproductionController.java)
- Controller de alertas: [src/main/java/com/devmaster/goatfarm/reproduction/api/controller/FarmReproductionAlertsController.java](../../src/main/java/com/devmaster/goatfarm/reproduction/api/controller/FarmReproductionAlertsController.java)
- DTOs: [src/main/java/com/devmaster/goatfarm/reproduction/api/dto](../../src/main/java/com/devmaster/goatfarm/reproduction/api/dto)
