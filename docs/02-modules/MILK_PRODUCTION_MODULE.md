# Módulo Milk Production
Última atualização: 2026-02-28
Escopo: registro diário de ordenhas por cabra e consulta paginada de produção.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Módulo Lactação](./LACTATION_MODULE.md), [Guia de Migração](../03-api/API_VERSIONING_MIGRATION_GUIDE.md)

## Visão geral
Este módulo gerencia produções de leite por cabra, com operações de criação, consulta, atualização parcial e cancelamento lógico.

## Regras / Contratos
- Base URL: `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions`.
- `POST` exige `date`, `shift` e `volumeLiters`.
- Registro de produção depende de lactação ativa.
- `PATCH` atualiza apenas campos permitidos (`volumeLiters`, `notes`).
- `DELETE` realiza cancelamento lógico (não remove histórico físico).
- Compatibilidade temporária: `/api/...` segue ativo por 1 ciclo como **DEPRECATED** (remoção planejada: 2026-06-30, v2.0.0).

## Endpoints
| Método | URL | Query params | Retorno |
|---|---|---|---|
| `POST` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions` | - | `201 Created` |
| `PATCH` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions` | `from`, `to`, `includeCanceled`, `page`, `size`, `sort` | `200 OK` (`Page` do Spring) |
| `DELETE` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}` | - | `204 No Content` |

Exemplo curto (criação):

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

## Compatibilidade e paginação
- As rotas canônicas são sempre publicadas em `/api/v1/...`.
- O legado `/api/...` segue ativo apenas por compatibilidade temporária.
- A listagem continua retornando `Page` do Spring para preservar compatibilidade com consumidores já publicados.

## Erros/Status
- `400`: payload inválido, filtros inconsistentes ou paginação inválida.
- `401`: autenticação ausente ou inválida.
- `403`: ownership/perfil insuficiente.
- `404`: produção não encontrada.
- `422`: regra de negócio violada (ex.: sem lactação ativa).
- Padrão de payload de erro: [API_CONTRACTS](../03-api/API_CONTRACTS.md).

## Referências internas
- Controller: [src/main/java/com/devmaster/goatfarm/milk/api/controller/MilkProductionController.java](../../src/main/java/com/devmaster/goatfarm/milk/api/controller/MilkProductionController.java)
- DTOs: [src/main/java/com/devmaster/goatfarm/milk/api/dto](../../src/main/java/com/devmaster/goatfarm/milk/api/dto)
## Bloqueio por carencia sanitaria ativa (2026-03-29)
- O registro de producao continua exigindo lactacao ativa.
- Alem disso, o backend passou a bloquear `POST /milk-productions` quando a cabra possui carencia de leite ativa derivada de evento sanitario realizado.
- O bloqueio e validado no backend, com mensagem operacional explicita informando o fim da carencia e a origem resumida do tratamento.
- A liberacao ocorre automaticamente por regra temporal simples quando a carencia expira.
- Esta etapa nao cria motor generico de restricoes zootecnicas; apenas reaproveita o modulo `health` como fonte da regra operacional.
