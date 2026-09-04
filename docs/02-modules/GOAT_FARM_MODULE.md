# GOAT_FARM_MODULE
Última atualização: 2026-08-07
Escopo: contratos e bordas HTTP do módulo base de Fazendas e Cabras (Goat/Farm).
Links relacionados: [API_CONTRACTS](../03-api/API_CONTRACTS.md), [Guia de Migração](../03-api/API_VERSIONING_MIGRATION_GUIDE.md), [Padrão Market-Grade](../01-architecture/MODULE_STANDARD_MARKET_GRADE.md)

Atualizado em 2026-09-04 para as referências genealógicas locais e externas.

## Objetivo do módulo
- Cadastrar fazendas caprinas.
- Atualizar dados completos da fazenda e do proprietário vinculado.
- Consultar permissões do usuário sobre a fazenda.
- Cadastrar, consultar, atualizar e remover cabras vinculadas a uma fazenda.
- Importar cabras da ABCC pública de forma opcional, sem obrigar o cadastro manual a depender da ABCC.

## Catálogo público e privacidade

- A leitura de fazendas, animais e genealogia é pública e somente para consulta.
- O catálogo pode exibir nome do responsável, telefones, e-mail de contato e redes sociais cadastradas, pois esses dados cumprem finalidade comercial de contato com a fazenda.
- CPF, credenciais, papéis de acesso e endereço detalhado não pertencem ao contrato público. Para endereço, a resposta pública limita-se a município, estado e país.
- Dados operacionais de sanidade, reprodução, lactação, estoque, alertas, relatórios e financeiro continuam protegidos por autenticação e autorização por fazenda.
- A resposta pública de fazenda é sanitizada no backend; a ausência de CPF no frontend não substitui essa proteção.

## Regra de bloqueio operacional por status do animal
- Animais com status diferente de `ATIVO` não podem sofrer escrita operacional.
- A regra é aplicada em:
  - reprodução
  - lactação
  - produção de leite
  - sanidade
- Mensagem de negócio padrão:
  - `Apenas cabras com status ATIVO podem ser manipuladas. Status atual: <STATUS>`
- Leitura e histórico continuam permitidos.
- A correção cadastral do próprio animal (incluindo eventual ajuste de status) não faz parte desse bloqueio operacional.

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
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/exit`

Reprodução (Sprint 1 - parto + cria(s)):
- `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}/births`
- Registra parto, encerra a gestação ativa com `BIRTH` e cadastra uma ou mais crias vinculadas.
- Vínculo mãe é obrigatório (matriz do path).
- Pai e mãe da cria seguem a política única de referências genealógicas deste
  módulo; o pai pode ser local, de outra fazenda ou externo pela ABCC.

## Caso de uso: cadastrar animal ou cria com referências genealógicas

- O cadastro manual (`POST /goats`) e o registro de cria no parto usam a mesma
  resolução de pai e mãe no módulo Goat. A rota de parto não possui lookup local
  próprio nem uma integração ABCC paralela.
- O sistema normaliza o RG, procura primeiro um `Goat` em todo o cadastro local
  e, se não houver resultado, reutiliza a porta pública de genealogia ABCC.
- Para `PO` e `PC`, pai e mãe são obrigatórios e precisam ser identificáveis por
  um `Goat` local ou por uma resposta ABCC válida. Um RG não localizado gera
  `422` no campo correspondente.
- Para `PA`, pai e mãe são opcionais. Um RG informado que não seja localizado
  pode ser mantido como referência `DECLARADO`; a ausência de um genitor não
  bloqueia o nascimento ou o cadastro.
- Pai identificado deve ser `MACHO`, mãe identificada deve ser `FEMEA`, e nenhum
  deles pode ser o próprio animal. Violação de regra de domínio retorna `422`.
- Se a ABCC estiver indisponível ou devolver dados insuficientes para validar uma
  categoria que exige genealogia, a API retorna `503`; isso não é apresentado
  como “não encontrado”.
- Uma referência local pode apontar para animal de outra fazenda apenas para
  genealogia. Ela não muda propriedade, permissões, vinculação do rebanho ou a
  regra de autorização por `farmId`.

## Persistência e projeção genealógica

- A migration `V36__add_external_genealogical_parent_references.sql` acrescenta
  `pai_rg_externo` e `mae_rg_externo` em `cabras`.
- Cada lado mantém uma única forma de referência: FK local ou RG externo; a
  constraint da base impede que ambas coexistam para o mesmo genitor.
- A árvore permanece montada sob demanda. Não há nova tabela de árvore, cópia de
  ancestrais ABCC ou criação de `Goat` fictício para representar genitor externo.

Reprodução (Sprint 2 - desmame):
- `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/weaning`
- Registra desmame de cria/animal jovem por data e mantém o histórico no contexto de reprodução.
- Regras mínimas:
  - `weaningDate` obrigatória, não futura e não anterior à data de nascimento.
  - Animal deve estar `ATIVO`.
  - Animal precisa ter vínculo local de mãe para elegibilidade do fluxo.
  - Não permite desmame duplicado para o mesmo animal.
- Transição adotada nesta sprint:
  - O domínio atual não possui categoria etária explícita para "desmamado".
  - A transição operacional é representada pelo evento reprodutivo `WEANING`.
  - `GoatStatus` permanece no modelo existente (sem criação de enum/tabela nova).

Invariantes fortes do fluxo reprodutivo (hotfix):
- Não é permitido registrar nova cobertura para matriz com gestação `ACTIVE`.
- Não é permitido reutilizar `coverage_event_id` já consumido por gestação anterior.
- Fechamento com `BIRTH` é exclusivo de `registerBirth` (não pode passar por `closePregnancy` genérico).
- `closePregnancy` exige `closeDate` não futura.
- Cobertura na mesma data (ou antes) de parto já registrado para a matriz é bloqueada.

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

## Regra de situação ABCC sem RGD
- `Sem RGD` não impede importação patrimonial.
- O identificador estável para o fluxo é o `registrationNumber` ABCC.
- No fluxo patrimonial ABCC, a situação `Sem RGD` é normalizada como `ATIVO` para manter coerência entre `preview`, `confirm` e `confirm-batch`.
- A deduplicação por `registrationNumber` e a validação de `TOD` continuam obrigatórias sem alteração.

## Saída controlada do animal do rebanho (backend-first)
- Objetivo: registrar saída operacional com rastreabilidade mínima sem exclusão física do animal.
- Endpoint:
  - `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/exit`
- Payload mínimo:
  - `exitType`: `VENDA`, `MORTE`, `DESCARTE`, `DOACAO`, `TRANSFERENCIA`
  - `exitDate`: data efetiva da saída
  - `notes`: observação opcional
- Regras:
  - só permite saída para animal em status `ATIVO`
  - bloqueia saída duplicada para o mesmo animal
  - `exitDate` obrigatória, não futura e não anterior à data de nascimento
- Transição de status:
  - `VENDA` -> `VENDIDO`
  - `MORTE` -> `FALECIDO`
  - `DESCARTE`, `DOACAO`, `TRANSFERENCIA` -> `INATIVO`
- Rastreabilidade persistida no `Goat`:
  - `exitType`, `exitDate`, `exitNotes`

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
  - `DECLARADO`
  - `AUSENTE`

## Resumo oficial da entrega — Genealogia complementar ABCC
- Feature pública preservada, alinhada ao fluxo público de genealogia do produto.
- Consulta complementar em modo `read-only`, sem persistência de ancestrais externos.
- Sem criação de novo `Goat` e sem impacto patrimonial no rebanho.
- Separação explícita da importação patrimonial ABCC (que continua com regra de TOD).
- Árvore híbrida local + ABCC com origem por nó (`LOCAL`, `ABCC`, `DECLARADO`,
  `AUSENTE`).
- Quando a raiz não possui resposta complementar na ABCC, um RG externo
  declarado pode ser apresentado como pai ou mãe sem persistir ancestrais.
- Lookup principal na ABCC por `registrationNumber`, sem heurística fraca por nome.

## Versionamento
- As rotas de fazendas, animais e importação ABCC são publicadas exclusivamente em `/api/v1/...`.
- O prefixo não versionado foi removido em 2026-08-07.
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
- `503`: consulta externa ABCC indisponível ou sem dados suficientes para a
  validação genealógica obrigatória.

## Cobertura mínima
- Unit: [GoatFarmBusinessTest](../../src/test/java/com/devmaster/goatfarm/farm/business/GoatFarmBusinessTest), [GoatBusinessTest](../../src/test/java/com/devmaster/goatfarm/goat/business/GoatBusinessTest), [GenealogicalParentageServiceTest](../../src/test/java/com/devmaster/goatfarm/goat/business/GenealogicalParentageServiceTest), [GoatAbccImportBusinessTest](../../src/test/java/com/devmaster/goatfarm/goat/business/GoatAbccImportBusinessTest)
- Controller: [GoatFarmControllerTest](../../src/test/java/com/devmaster/goatfarm/farm/api/GoatFarmControllerTest), [GoatControllerTest](../../src/test/java/com/devmaster/goatfarm/goat/api/GoatControllerTest), [GoatAbccImportControllerTest](../../src/test/java/com/devmaster/goatfarm/goat/api/GoatAbccImportControllerTest)
