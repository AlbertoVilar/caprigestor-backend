# GOAT_FARM_MODULE
Última atualização: 2026-02-28
Escopo: contratos e bordas HTTP do módulo base de Fazendas e Cabras (Goat/Farm).
Links relacionados: [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Guia de Migração](../03-api/API_VERSIONING_MIGRATION_GUIDE.md), [Padrão Market-Grade](../01-architecture/MODULE_STANDARD_MARKET_GRADE.md)

## Objetivo do módulo
- Cadastrar fazendas caprinas.
- Atualizar dados completos da fazenda e do proprietário vinculado.
- Consultar permissões do usuário sobre a fazenda.
- Cadastrar, consultar, atualizar e remover cabras vinculadas a uma fazenda.
- Importar cabras da ABCC pública de forma opcional, sem obrigar o cadastro manual a depender da ABCC.

## Rotas canônicas
Fazenda:
- `POST /api/v1/goatfarms`
- `PUT /api/v1/goatfarms/{id}`
- `GET /api/v1/goatfarms/{id}`
- `GET /api/v1/goatfarms`
- `GET /api/v1/goatfarms/name?name=&page=&size=&sort=`
- `DELETE /api/v1/goatfarms/{id}`
- `GET /api/v1/goatfarms/{farmId}/permissions`

Cabras:
- `POST /api/v1/goatfarms/{farmId}/goats`
- `PUT /api/v1/goatfarms/{farmId}/goats/{goatId}`
- `DELETE /api/v1/goatfarms/{farmId}/goats/{goatId}`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}`
- `GET /api/v1/goatfarms/{farmId}/goats?page=&size=&sort=`
- `GET /api/v1/goatfarms/{farmId}/goats/search?name=&page=&size=&sort=`

Importação ABCC (opcional):
- `POST /api/v1/goatfarms/{farmId}/goats/imports/abcc/search`
- `POST /api/v1/goatfarms/{farmId}/goats/imports/abcc/preview`
- `POST /api/v1/goatfarms/{farmId}/goats/imports/abcc/confirm`

## Fluxo de importação ABCC
- `search`: consulta lista pública da ABCC por raça/afixo e retorna candidatos normalizados para seleção.
- `preview`: carrega detalhes e genealogia do animal selecionado (pai/mãe quando disponíveis) sem persistir.
- `confirm`: confirma dados revisados e cria a cabra na fazenda reutilizando o fluxo de criação manual do módulo Goat.
- O cadastro manual continua intacto e independente da ABCC.

## Compatibilidade temporária
- As rotas legadas em `/api/...` continuam ativas por compatibilidade.
- Essas rotas são **DEPRECATED** e devem ser removidas após 2026-06-30.
- O frontend e novos consumidores devem usar apenas `/api/v1/...`.

## Paginação e filtros
- Listagens de fazendas e cabras aceitam `page`, `size` e `sort`.
- O contrato atual retorna `Page` do Spring (`content`, `totalElements`, `number`, etc.).
- Não houve normalização para o envelope `{ content, page }` nesta rodada para evitar quebra de contrato com consumidores já publicados.

## Status e erros esperados
- `200`: consulta ou atualização bem-sucedida.
- `201`: criação bem-sucedida.
- `204`: exclusão bem-sucedida.
- `400`: parâmetros inválidos.
- `403`: falha de autorização/ownership.
- `404`: recurso não encontrado.
- `409`: conflito de unicidade (quando aplicável).
- `422`: falha de validação de payload.

## Cobertura mínima
- Unit: [GoatFarmBusinessTest](../../src/test/java/com/devmaster/goatfarm/farm/business/GoatFarmBusinessTest), [GoatBusinessTest](../../src/test/java/com/devmaster/goatfarm/goat/business/GoatBusinessTest)
- Controller: [GoatFarmControllerTest](../../src/test/java/com/devmaster/goatfarm/farm/api/GoatFarmControllerTest), [GoatControllerTest](../../src/test/java/com/devmaster/goatfarm/goat/api/GoatControllerTest)
