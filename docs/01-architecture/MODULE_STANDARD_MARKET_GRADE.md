# Padrão de Módulo “Nível Mercado”

## Objetivo

Este documento consolida o padrão replicável para novos módulos e evoluções dos módulos existentes do CapriGestor.

## Estrutura hexagonal por módulo

Fluxo obrigatório:

`Controller -> Port In -> Business -> Port Out -> Adapter/Repository`

Regras:
- não mover lógica de domínio para controller;
- não acessar repository diretamente a partir do controller;
- não acoplar módulos entre si sem contrato explícito.

## Padrão de rotas

- Base canônica: `/api/v1`
- Compatibilidade temporária: `/api` legado em dual mapping, marcado como **DEPRECATED**
- Escopo por fazenda: `/api/v1/goatfarms/{farmId}/...`
- Todo controller em `api/controller` deve manter class-level `@RequestMapping` com `/api/v1`

## Paginação, filtros e ordenação

- Parâmetros padrão: `page`, `size`, `sort`
- Resposta padrão:

```json
{
  "content": [],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

- Validar filtros no `Business`
- Limite recomendado de `size`: `100`
- Datas devem validar intervalo (`fromDate <= toDate`)

## Padrão de erros

- `400`: argumento inválido / filtro inválido
- `404`: recurso não encontrado
- `409`: conflito / duplicidade / idempotência incompatível
- `422`: regra de negócio violada

Estrutura padrão:

```json
{
  "status": 400,
  "error": "Argumento inválido",
  "errors": [
    {
      "fieldName": "fromDate",
      "message": "Data inicial não pode ser maior que data final."
    }
  ]
}
```

## Checklist de testes

- unit: validações do `Business`
- integration: adapter/repository com filtros, paginação e ordenação
- controller: contrato HTTP, status e payload
- guardrails:
  - arquitetura (ex.: fronteira entre módulos)
  - versionamento (`/api/v1`)
- smoke/global gate antes de PR

## Checklist de documentação e Swagger

- Swagger em PT-BR
- exemplos de request/response
- rotas canônicas e legadas documentadas
- códigos HTTP relevantes (`200/201/400/403/404/409/422`)
- documentação do módulo atualizada em `docs/02-modules`

## UTF-8 e anti-mojibake

Antes de commitar:
- validar diff para `Ã`, `�`, `Â`, `â€“`, `â€”`, `â€œ`, `â€`
- manter arquivos em UTF-8
- revisar mensagens PT-BR com acentuação

## Git flow via PR

- criar branch `codex/feature/...`
- commits pequenos em inglês (Conventional Commits)
- abrir PR para `develop`
- validar checks
- mergear
- abrir PR `develop -> main`
- sincronizar local com `pull --ff-only`
- limpar branch local e remota após merge
