# GOAT_FARM_MODULE
Última atualização: 2026-03-13
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
- `GET /api/v1/goatfarms/{farmId}/goats/imports/abcc/races`
- `POST /api/v1/goatfarms/{farmId}/goats/imports/abcc/search`
- `POST /api/v1/goatfarms/{farmId}/goats/imports/abcc/preview`
- `POST /api/v1/goatfarms/{farmId}/goats/imports/abcc/confirm`
- `POST /api/v1/goatfarms/{farmId}/goats/imports/abcc/confirm-batch`

## Fluxo de importação ABCC
- `search`: consulta lista pública da ABCC por raça/afixo e retorna candidatos normalizados para seleção.
- `preview`: carrega detalhes e genealogia do animal selecionado (pai/mãe quando disponíveis) sem persistir.
- `confirm`: confirma dados revisados e cria a cabra na fazenda reutilizando o fluxo de criação manual do módulo Goat.
- `confirm-batch`: importa em lote os animais selecionados da página atual da busca, sem derrubar o lote inteiro quando houver incompatibilidades.

## Regra forte de segurança por TOD
- Usuário comum (não `ROLE_ADMIN`) só pode usar importação ABCC para animais com `TOD` igual ao `TOD` da fazenda.
- `ROLE_ADMIN` global pode operar com qualquer TOD (override administrativo).
- Se a fazenda não tiver TOD configurado, o fluxo ABCC é bloqueado para usuário comum.
- A validação ocorre em profundidade no backend: `search`, `preview`, `confirm` e `confirm-batch`.

## Resultado do lote ABCC
Resumo:
- `totalSelected`
- `totalImported`
- `totalSkippedDuplicate`
- `totalSkippedTodMismatch`
- `totalError`

Status por item:
- `IMPORTED`
- `SKIPPED_DUPLICATE`
- `SKIPPED_TOD_MISMATCH`
- `ERROR`

Regras de decisão por item no lote:
- Duplicidade (`farmId + registrationNumber`) -> `SKIPPED_DUPLICATE`
- TOD incompatível para usuário comum -> `SKIPPED_TOD_MISMATCH`
- Item válido -> `IMPORTED`
- Falha técnica/validação residual -> `ERROR`

## Genealogia complementar ABCC (somente leitura)
- Objetivo: complementar a árvore genealógica de um animal já cadastrado no CapriGestor usando dados públicos da ABCC.
- Escopo:
  - Somente consulta e exibição.
  - Não persiste ancestrais externos.
  - Não cria novos animais no rebanho.
  - Não altera o fluxo patrimonial de importação ABCC.
- Consulta:
  - `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/genealogies?complementaryAbcc=true`
  - Mantém o endpoint local atual sem alteração de contrato quando `complementaryAbcc` não é informado.
- Segurança e publicidade:
  - Fluxo público de leitura, seguindo a mesma política pública da genealogia local.
  - Não aplica regra patrimonial de TOD da importação ABCC.
  - Não exige posse dos ancestrais externos para exibição da árvore.
- Chave de lookup ABCC:
  - `registrationNumber` do animal local.
  - Sem fallback por nome.
- Status de integração:
  - `FOUND`
  - `NOT_FOUND`
  - `UNAVAILABLE`
  - `INSUFFICIENT_DATA`
- Origem dos nós da árvore híbrida:
  - `LOCAL`
  - `ABCC`
  - `AUSENTE`

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
- `422`: falha de validação de payload/regra de negócio.

## Cobertura mínima
- Unit: [GoatFarmBusinessTest](../../src/test/java/com/devmaster/goatfarm/farm/business/GoatFarmBusinessTest), [GoatBusinessTest](../../src/test/java/com/devmaster/goatfarm/goat/business/GoatBusinessTest), [GoatAbccImportBusinessTest](../../src/test/java/com/devmaster/goatfarm/goat/business/GoatAbccImportBusinessTest)
- Controller: [GoatFarmControllerTest](../../src/test/java/com/devmaster/goatfarm/farm/api/GoatFarmControllerTest), [GoatControllerTest](../../src/test/java/com/devmaster/goatfarm/goat/api/GoatControllerTest), [GoatAbccImportControllerTest](../../src/test/java/com/devmaster/goatfarm/goat/api/GoatAbccImportControllerTest)
