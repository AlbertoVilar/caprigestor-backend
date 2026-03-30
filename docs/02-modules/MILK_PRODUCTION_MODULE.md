# MÃ³dulo Milk Production
Ãšltima atualizaÃ§Ã£o: 2026-02-28
Escopo: registro diÃ¡rio de ordenhas por cabra e consulta paginada de produÃ§Ã£o.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [MÃ³dulo LactaÃ§Ã£o](./LACTATION_MODULE.md), [Guia de MigraÃ§Ã£o](../03-api/API_VERSIONING_MIGRATION_GUIDE.md)

## VisÃ£o geral
Este mÃ³dulo gerencia produÃ§Ãµes de leite por cabra, com operaÃ§Ãµes de criaÃ§Ã£o, consulta, atualizaÃ§Ã£o parcial e cancelamento lÃ³gico.

## Regras / Contratos
- Base URL: `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions`.
- `POST` exige `date`, `shift` e `volumeLiters`.
- Registro de produÃ§Ã£o depende de lactaÃ§Ã£o ativa.
- `PATCH` atualiza apenas campos permitidos (`volumeLiters`, `notes`).
- `DELETE` realiza cancelamento lÃ³gico (nÃ£o remove histÃ³rico fÃ­sico).
- Compatibilidade temporÃ¡ria: `/api/...` segue ativo por 1 ciclo como **DEPRECATED** (remoÃ§Ã£o planejada: 2026-06-30, v2.0.0).

## Endpoints
| MÃ©todo | URL | Query params | Retorno |
|---|---|---|---|
| `POST` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions` | - | `201 Created` |
| `PATCH` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions` | `from`, `to`, `includeCanceled`, `page`, `size`, `sort` | `200 OK` (`Page` do Spring) |
| `DELETE` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}` | - | `204 No Content` |

Exemplo curto (criaÃ§Ã£o):

```http
POST /api/v1/goatfarms/1/goats/BR123/milk-productions
Content-Type: application/json
```

```json
{
  "date": "2026-02-10",
  "shift": "MORNING",
  "volumeLiters": 2.45,
  "notes": "Ordenha regular"
}
```

```json
{
  "id": 55,
  "date": "2026-02-10",
  "shift": "MORNING",
  "volumeLiters": 2.45,
  "status": "ACTIVE"
}
```

Exemplo curto (listagem):

```http
GET /api/v1/goatfarms/1/goats/BR123/milk-productions?from=2026-02-01&to=2026-02-10&includeCanceled=false&page=0&size=12
```

## Compatibilidade e paginaÃ§Ã£o
- As rotas canÃ´nicas sÃ£o sempre publicadas em `/api/v1/...`.
- O legado `/api/...` segue ativo apenas por compatibilidade temporÃ¡ria.
- A listagem continua retornando `Page` do Spring para preservar compatibilidade com consumidores jÃ¡ publicados.

## Erros/Status
- `400`: payload invÃ¡lido, filtros inconsistentes ou paginaÃ§Ã£o invÃ¡lida.
- `401`: autenticaÃ§Ã£o ausente ou invÃ¡lida.
- `403`: ownership/perfil insuficiente.
- `404`: produÃ§Ã£o nÃ£o encontrada.
- `422`: regra de negÃ³cio violada (ex.: sem lactaÃ§Ã£o ativa).
- PadrÃ£o de payload de erro: [API_CONTRACTS](../03-api/API_CONTRACTS.md).

## ReferÃªncias internas
- Controller: [src/main/java/com/devmaster/goatfarm/milk/api/controller/MilkProductionController.java](../../src/main/java/com/devmaster/goatfarm/milk/api/controller/MilkProductionController.java)
- DTOs: [src/main/java/com/devmaster/goatfarm/milk/api/dto](../../src/main/java/com/devmaster/goatfarm/milk/api/dto)
## Carencia sanitaria operacional no leite (2026-03-29)
- O registro de producao continua exigindo lactacao ativa.
- Quando existir carencia de leite ativa derivada de evento sanitario realizado, o backend continua permitindo o registro da ordenha para preservar o historico biologico real do animal.
- Nesses casos, a producao e salva com snapshot sanitario proprio:
  - `recordedDuringMilkWithdrawal`
  - `milkWithdrawalEventId`
  - `milkWithdrawalEndDate`
  - `milkWithdrawalSource`
- Esse snapshot preserva a rastreabilidade historica mesmo depois da expiracao temporal da carencia.
- A UI deve alertar fortemente que o leite segue restrito para uso comercial enquanto a carencia estiver ativa.
- Esta etapa nao cria motor generico de restricoes zootecnicas; apenas reaproveita o modulo `health` como fonte da leitura operacional.

## Producao diaria consolidada da fazenda (2026-03-30)
- Esta frente NAO substitui o controle individual por cabra.
- O modulo `milk` passa a ter dois niveis de leitura:
  - `producao individual`, por cabra, para analise zootecnica;
  - `producao consolidada da fazenda`, por `farmId + productionDate`, para leitura operacional.
- O consolidado diario da fazenda persiste:
  - `productionDate`
  - `totalProduced`
  - `withdrawalProduced`
  - `marketableProduced`
  - `notes`
- Regra central:
  - `totalProduced` representa o volume total registrado no consolidado do dia;
  - `withdrawalProduced` representa o volume restrito/em carencia;
  - `marketableProduced` representa o volume liberado/comercializavel;
  - `withdrawalProduced + marketableProduced` deve ser coerente com `totalProduced`.
- O backend aceita derivacao segura:
  - se apenas `totalProduced` for informado, o restrito assume `0` e o liberado e derivado;
  - se apenas um dos lados (`withdrawalProduced` ou `marketableProduced`) for informado junto com o total, o outro lado e derivado;
  - se ambos forem enviados, a soma precisa bater exatamente com o total.
- Nao existe reconciliacao automatica obrigatoria entre:
  - soma das ordenhas individuais;
  - consolidado diario da fazenda.
- O objetivo desta etapa e oferecer um registro operacional simples, confiavel e utilizavel no dia a dia.

### Endpoints do consolidado
Base canonica:

```text
/api/v1/goatfarms/{farmId}/milk-consolidated-productions
```

Rotas:

- `PUT /api/v1/goatfarms/{farmId}/milk-consolidated-productions/{productionDate}`
- `GET /api/v1/goatfarms/{farmId}/milk-consolidated-productions/daily?date=YYYY-MM-DD`
- `GET /api/v1/goatfarms/{farmId}/milk-consolidated-productions/monthly?year=YYYY&month=MM`
- `GET /api/v1/goatfarms/{farmId}/milk-consolidated-productions/annual?year=YYYY`

### Semantica de carencia no consolidado
- A logica individual de carencia permanece intacta.
- O leite registrado individualmente em carencia continua existindo no historico do animal.
- No consolidado da fazenda, esse volume deve compor `withdrawalProduced`.
- O volume `marketableProduced` nao pode ser inflado por leite restrito/em carencia.

### Leituras disponiveis
- **Visao diaria**: retorna um unico resumo para a data selecionada, inclusive quando nao houver registro salvo.
- **Visao mensal**: agrega os registros diarios do mes e devolve a lista de dias registrados.
- **Visao anual**: agrega os registros do ano por mes, sem abrir BI ou analytics pesado.

### Limites conscientes desta etapa
- Nao ha reconciliacao automatica litro a litro entre individual e consolidado.
- Nao ha controle de tanque, lote industrial ou fechamento de fabrica/laticinio.
- Nao ha integracao contabil automatica com venda, custo ou financeiro alem da leitura operacional.
