# Módulo Milk Production
Última atualização: 2026-02-26
Escopo: registro diário de ordenhas por cabra e consulta paginada de produção.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Módulo Lactação](./LACTATION_MODULE.md), [Guia de Migração](../03-api/API_VERSIONING_MIGRATION_GUIDE.md)

## Visão geral
Este módulo gerencia produções de leite por cabra, com operações de criação, consulta, atualização parcial e cancelamento lógico.

## Regras / Contratos
- Base URL: `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions`.
- `POST` exige `date`, `shift` e `volumeLiters`.
- Registro de producao depende de lactacao ativa.
- `PATCH` atualiza apenas campos permitidos (`volumeLiters`, `notes`).
- `DELETE` realiza cancelamento lógico (não remove histórico físico).
- Compatibilidade temporária: `/api/...` segue ativo por 1 ciclo como **DEPRECATED** (remoção planejada: 2026-06-30, v2.0.0).

## Endpoints
| Metodo | URL | Query params | Retorno |
|---|---|---|---|
| `POST` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions` | - | `201 Created` |
| `PATCH` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}` | - | `200 OK` |
| `GET` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions` | `from`, `to`, `includeCanceled`, `page`, `size`, `sort` | `200 OK` (pagina) |
| `DELETE` | `/api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}` | - | `204 No Content` |

Contrato curto (criacao):
- URL: `POST /api/v1/goatfarms/1/goats/BR123/milk-productions`
- Request curto:

```json
{
  "date": "2026-02-10",
  "shift": "MORNING",
  "volumeLiters": 2.45,
  "notes": "ordenha regular"
}
```

- Response curto:

```json
{
  "id": 55,
  "date": "2026-02-10",
  "shift": "MORNING",
  "volumeLiters": 2.45,
  "status": "ACTIVE"
}
```

Contrato curto (listagem):
- URL: `GET /api/v1/goatfarms/1/goats/BR123/milk-productions?from=2026-02-01&to=2026-02-10&includeCanceled=false&page=0&size=12`
- Query params:
  - `from` / `to`: filtro por intervalo de data (opcional)
  - `includeCanceled`: inclui cancelados (default `false`)
  - `page`, `size`, `sort`: paginacao e ordenacao

## Fluxos principais
1. Registro de producao:
   cria item diario vinculado a cabra/fazenda.
2. Correcao operacional:
   `PATCH` ajusta volume/notas sem reescrever historico completo.
3. Cancelamento:
   `DELETE` marca cancelamento e preserva rastreabilidade.

Observacoes de performance:
- Listagem e paginada e ordenada no banco.
- Filtros por periodo e escopo de fazenda reduzem carga de consulta.

## Erros/Status
- `400`: payload invalido ou parametros inconsistentes.
- `401`: autenticacao ausente/invalida.
- `403`: ownership/perfil insuficiente.
- `404`: producao nao encontrada.
- `422`: regra de negocio violada (ex.: sem lactacao ativa).
- Padrao de payload de erro: [API_CONTRACTS](../03-api/API_CONTRACTS.md).

## Referencias internas
- Controller: [src/main/java/com/devmaster/goatfarm/milk/api/controller/MilkProductionController.java](../../src/main/java/com/devmaster/goatfarm/milk/api/controller/MilkProductionController.java)
- DTOs: [src/main/java/com/devmaster/goatfarm/milk/api/dto](../../src/main/java/com/devmaster/goatfarm/milk/api/dto)



