# API_CONTRACTS
Última atualização: 2026-09-11
Escopo: padrões transversais de rotas, autenticação, paginação, idempotência e erros da API.
Links relacionados: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [Módulo Authority](../02-modules/AUTHORITY_ACCESS_MODULE.md), [Módulo Goat/Farm](../02-modules/GOAT_FARM_MODULE.md), [Módulo Reproduction](../02-modules/REPRODUCTION_MODULE.md), [Módulo Lactação](../02-modules/LACTATION_MODULE.md), [Módulo Milk Production](../02-modules/MILK_PRODUCTION_MODULE.md), [Módulo Health](../02-modules/HEALTH_VETERINARY_MODULE.md), [Módulo Inventory](../02-modules/INVENTORY_MODULE.md), [Módulo Commercial](../02-modules/COMMERCIAL_MODULE.md), [Módulo Articles](../02-modules/ARTICLE_BLOG_MODULE.md), [Guia de Migração de Versionamento](./API_VERSIONING_MIGRATION_GUIDE.md)

O catálogo de rotas abaixo é uma superfície resumida; os documentos dos módulos continuam sendo a referência detalhada de payloads e regras específicas.

## Visão geral
Este documento define contratos comuns para todos os controllers oficiais do backend.

### Identidade do animal durante a transição

Responses de Goat e dos módulos dependentes podem retornar `technicalId` ou
`goatTechnicalId` junto de `registrationNumber`/`goatId`. O primeiro é o
GoatId estrutural imutável; o segundo valor continua sendo o RG de negócio e
snapshot compatível com as rotas v1 atuais. O campo técnico é aditivo e não
altera o significado das URLs existentes. Rotas estruturais explícitas por
GoatId e a remoção do alias RG serão publicadas somente em uma versão futura.

## Regras / Contratos
### Base de rotas
- Base geral: `/api/v1`
- Escopo por fazenda: `/api/v1/goatfarms/{farmId}/...`
- Rotas públicas sem autenticação (quando aplicável) usam namespace separado, por exemplo: `/public/articles`.
- As consultas públicas `GET` de fazendas, animais e genealogia sob `/api/v1/goatfarms` são públicas por decisão de produto, exceto rotas explicitamente administrativas como `/api/v1/goatfarms/{farmId}/management`. Fazendas públicas podem incluir nome do responsável e e-mail de contato, mas não incluem telefones, CPF, credenciais, papéis ou endereço detalhado.

### Versionamento
- Endpoints de aplicação são publicados exclusivamente em `/api/v1/...`.
- O prefixo não versionado foi removido em 2026-08-07 e não possui fallback.
- Novos endpoints não devem ser publicados fora de `/api/v1`.

### Segurança
- Autenticação: JWT.
- Autorização: ownership por `farmId` e/ou roles (`ROLE_ADMIN`, `ROLE_OPERATOR`, `ROLE_FARM_OWNER`).
- Controllers farm-scoped declaram a intenção por `@CanManageFarm` (operação
  para ADMIN/owner/operator vinculado), `@FarmOwnerOnly` (ADMIN/owner) ou uma
  marca explícita de endpoint público/autenticado. A annotation não substitui
  a validação de negócio nem altera os papéis aceitos.
- Toda policy semântica farm-scoped usa o parâmetro `farmId`; o guard de
  arquitetura rejeita a criação de endpoint sem intenção declarada ou com
  identificador de fazenda não resolvível.
- Access tokens usam `typ=access`, emissor e audiência configurados; refresh tokens usam `typ=refresh` e só podem ser enviados para os endpoints de sessão.
- Respostas de segurança:
  - `401` via `CustomAuthenticationEntryPoint`
  - `403` via `CustomAccessDeniedHandler` ou `AccessDeniedException`
- Não existem endpoints REST para limpeza global do banco ou recriação de admin.
  Rebuild/reset de DEV é operacional e externo à API; bootstrap administrativo,
  quando habilitado, usa configuração externa e não executa limpeza de dados.

### Autenticação e sessão

- `POST /api/v1/auth/login`: emite access token e refresh token.
- `POST /api/v1/auth/register`: cadastro público de usuário com a autoridade padrão `ROLE_OPERATOR`.
- `POST /api/v1/auth/register-farm`: cadastro público atômico de fazenda, usuário, endereço e telefones.
- `POST /api/v1/auth/refresh`: faz rotação de refresh token e devolve o mesmo contrato do login. Reuso de token já consumido retorna `401` e revoga a família de sessões.
- `POST /api/v1/auth/logout`: recebe `{ "refreshToken": "..." }`, revoga a família e retorna `204`.
- `GET /api/v1/auth/me`: requer access token; refresh tokens são rejeitados pelo resource server.
- `POST /api/v1/auth/password-reset/request`: solicitação pública com resposta neutra.
- `POST /api/v1/auth/password-reset/confirm`: confirmação pública com token de uso único.
- `expiresIn` é expresso em segundos e corresponde ao TTL efetivo do access token.

### Paginação
- Parâmetros padrão: `page` (base 0), `size`, `sort`.
- O padrão alvo para novos contratos é `content` + metadados em `page.number`, `page.size`, `page.totalElements`, `page.totalPages`.
- Quando um módulo já publicado ainda retorna `Page` do Spring, a exceção deve ser documentada no módulo e preservada por compatibilidade.
- Lactation mantém `Page` do Spring somente no histórico HTTP por compatibilidade;
  os alertas de secagem preservam o envelope próprio `totalPending` + `alerts`.

### Convenções de payload
- DTOs de request e response separados por módulo.
- Datas em formato ISO (`yyyy-MM-dd` ou `yyyy-MM-dd'T'HH:mm:ss`).
- Mensagens de validação em PT-BR.

As seções por domínio abaixo destacam apenas rotas, formatos e exceções específicas. Os códigos HTTP transversais seguem as seções globais deste documento.

### Goat/Farm (cadastros base)
Rotas canônicas:
- `POST /api/v1/goatfarms`
- `GET /api/v1/goatfarms`
- `GET /api/v1/goatfarms/name?name=&page=&size=&sort=`
- `GET /api/v1/goatfarms/{id}`
- `GET /api/v1/goatfarms/{id}/management`
- `PUT /api/v1/goatfarms/{id}`
- `DELETE /api/v1/goatfarms/{id}`
- `GET /api/v1/goatfarms/{farmId}/permissions`
- `GET /api/v1/goatfarms/{farmId}/goats?page=&size=&sort=`
- `GET /api/v1/goatfarms/{farmId}/goats/search?name=&page=&size=&sort=`
- `GET /api/v1/goatfarms/{farmId}/goats/summary`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}`
- `POST /api/v1/goatfarms/{farmId}/goats`
- `PUT /api/v1/goatfarms/{farmId}/goats/{goatId}`
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/registration`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/registration-history`
- `DELETE /api/v1/goatfarms/{farmId}/goats/{goatId}`
- `POST /api/v1/goatfarms/{farmId}/goats/imports/abcc/search`
- `POST /api/v1/goatfarms/{farmId}/goats/imports/abcc/preview`
- `POST /api/v1/goatfarms/{farmId}/goats/imports/abcc/registration-lookup`
- `POST /api/v1/goatfarms/{farmId}/goats/imports/abcc/confirm`
- `POST /api/v1/goatfarms/{farmId}/goats/imports/abcc/confirm-batch`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/genealogies?complementaryAbcc=true`

Retificação registral (ID5-A):
- o `PATCH .../registration` recebe `tod`, `toe`, `source`,
  `evidenceReference` e `reason`; o servidor deriva o RG canônico como
  `TOD + TOE` e rejeita uma combinação inconsistente;
- a operação mantém o `GoatId`, não altera snapshots históricos de eventos,
  genealogia, reprodução, lactação, saúde, comercial ou auditoria, e cria uma
  entrada em `goat_registration_history` e na auditoria operacional;
- a autorização é `@FarmOwnerOnly`: `ADMIN` e `FARM_OWNER` da fazenda podem
  executar/listar o histórico; `OPERATOR`, usuários de outra fazenda e
  anônimos recebem `403`/`401` conforme o caso;
- um RG atual já utilizado na mesma fazenda retorna `409`. Para alterar apenas
  perfil, use o `PUT` comum; para alterar identidade registral, use este fluxo
  administrativo explícito. A consulta ABCC continua opcional e não é
  modificada por esta operação.

O endpoint de permissões retorna as capacidades efetivas do usuário para a
fazenda informada:

```json
{
  "canOperateFarm": true,
  "canAdministerFarm": false
}
```

`canOperateFarm` segue `@CanManageFarm` (ADMIN, FARM_OWNER da própria fazenda
ou OPERATOR vinculado); `canAdministerFarm` segue `@FarmOwnerOnly` (ADMIN ou
FARM_OWNER da própria fazenda). O vínculo operador–fazenda é sempre decidido
no backend.

`GET /api/v1/goatfarms/{id}` permanece público e sanitizado para o catálogo.
Para preencher a tela de edição, o frontend usa
`GET /api/v1/goatfarms/{farmId}/management`, protegido por `@FarmOwnerOnly`.
Essa leitura administrativa retorna o `GoatFarmFullResponseDTO` completo
(fazenda, proprietário, endereço e telefones) somente para ADMIN ou para o
FARM_OWNER oficial da própria fazenda. OPERATOR e proprietários de outras
fazendas recebem `403`; anônimos não recebem dados administrativos. A operação
é somente leitura e não altera a persistência. O `PUT /api/v1/goatfarms/{id}`
continua sendo o único fluxo de atualização.

Paginação atual:
- As listagens continuam retornando `Page` do Spring (`content`, `totalElements`, `number`, etc.) para preservar compatibilidade com o frontend já publicado.

Importação ABCC:
- Feature opcional do módulo Goat.
- Não substitui nem deprecia o cadastro manual.
- `registration-lookup` recebe `{ raceId, registrationNumber }` e retorna `FOUND`,
  `NOT_FOUND` ou `AMBIGUOUS`. A consulta é somente leitura; nenhum resultado cria ou
  atualiza um animal. A mesma combinação raça + RG é validada novamente no preview antes
  de ser disponibilizada para pré-preenchimento.
- O endpoint `confirm` reutiliza internamente as regras de criação manual de cabra para evitar duplicação de domínio.

Genealogia complementar ABCC:
- Consulta pública e `read-only` para complementar a genealogia do animal local.
- Não persiste ancestrais externos e não cria novo `Goat`.
- Não aplica regra patrimonial de TOD da importação ABCC.
- Lookup principal por `registrationNumber`, sem fallback por nome.
- Status de integração: `FOUND`, `NOT_FOUND`, `UNAVAILABLE`, `INSUFFICIENT_DATA`.

Referências genealógicas em comandos de criação:
- `fatherRegistrationNumber` e `motherRegistrationNumber` são resolvidos pela
  mesma política em `POST /goats`, atualizações de animal e criação de cria no
  parto.
- `PO` e `PC` exigem pai e mãe identificáveis localmente ou por consulta ABCC.
  `PA` permite ausência e RG externo declarado não localizado.
- Pai e mãe identificados devem ter, respectivamente, sexo `MACHO` e `FEMEA`.
  O RG do próprio animal não pode ser usado como genitor.
- Referência de outra fazenda é apenas genealógica: não altera ownership,
  permissões ou o escopo da rota.
- A API persiste uma FK local ou o RG externo, nunca uma árvore da ABCC. A
  resposta de genealogia pode sinalizar origem `LOCAL`, `ABCC`, `DECLARADO` ou
  `AUSENTE`.


### Reproduction (gestação e alertas)
Rotas canônicas:
- `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding`
- `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/breeding/{coverageEventId}/corrections`
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/confirm`
- `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/checks`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/active`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}`
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/{pregnancyId}/close`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/events?page=&size=&sort=`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies?page=&size=&sort=`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/reproduction/pregnancies/diagnosis-recommendation?referenceDate=`
- `GET /api/v1/goatfarms/{farmId}/reproduction/alerts/pregnancy-diagnosis?referenceDate=&page=&size=`
- `GET /api/v1/goatfarms/{farmId}/reproduction/alerts/births-due?referenceDate=&page=&size=`

Paginação atual:
- Os endpoints `events` e `pregnancies` continuam retornando `Page` do Spring para preservar compatibilidade com o frontend já publicado.
- Os endpoints `pregnancy-diagnosis` e `births-due` retornam envelope agregado
  com `totalPending` e `alerts`; `page >= 0`, `size` entre 1 e 100, padrões 0/20.
- Em `births-due`, cada item contém `pregnancyId`, `goatId`,
  `expectedDueDate` e `daysOverdue`; `goatTechnicalId` é retornado de forma
  aditiva quando disponível. São retornadas gestações ativas da fazenda
  com previsão na referência ou anterior, ordenadas por previsão e ID crescentes.
- Os controllers usam `canManageFarm`, incluindo operador vinculado; a criação
  da cria reutiliza a mesma autorização operacional no caso de uso Goat.

No comando de parto, `kids[].registrationNumber` deve conter 10 a 12 caracteres
(números e uma letra final opcional), começando pelo TOD da fazenda de nascimento.
`kids[].birthDate`, se informada, deve coincidir com `birthDate` do parto.
Formato/data inválidos retornam `400`; inconsistência de TOD retorna `422`.
Detalhamento: [caso de uso de parto](../02-modules/REPRODUCTION_MODULE.md#caso-de-uso-comunicar-parto-e-cadastrar-cria).

### Commercial

- Consultas, resumos e cadastro de cliente exigem usuário autorizado a operar a fazenda: ADMIN, FARM_OWNER próprio ou OPERATOR formalmente vinculado.
- Registro de venda de animal ou leite, baixa de pagamento e lançamento de despesa operacional são mutações financeiras ou patrimoniais definitivas e exigem ADMIN ou FARM_OWNER da própria fazenda.
- A autorização das mutações sensíveis é aplicada no controller e validada novamente no caso de uso antes da persistência.
- `POST /api/v1/goatfarms/{farmId}/commercial/ownership-sales` exige
  `targetFarmId`, GoatId técnico e `idempotencyKey`; o novo request não contém
  `customerId`. A fazenda destino é o comprador canônico. Sem `paymentDate`, cria
  venda `OPEN` e transferência `REQUESTED`, mantendo a propriedade no ledger
  atual.
- Quando `paymentDate` é informado, deve ser maior ou igual a `saleDate` e não
  pode estar no futuro. A venda nasce `PAID` e a transferência é concluída
  atomicamente no mesmo comando.
- A resposta expõe `targetFarmId`, `targetFarmName` e `targetFarmTod`. Campos
  `customerId/customerName` são nulos em novas vendas internas e permanecem
  apenas para leitura compatível de registros históricos.
- `PATCH .../ownership-sales/{saleId}/payment` é uma mutação da fazenda de
  origem. Quando o pagamento é confirmado, o backend marca a venda como `PAID`,
  fecha/abre os períodos canônicos, move a projeção atual e conclui o handoff na
  mesma transação. O comprador não precisa aceitar nem rejeitar a venda.
- Cancelamento de uma venda interna não paga continua autorizado pela fazenda de
  origem; vendas pagas/concluídas não podem ser canceladas. Aceite/rejeição
  de `INTERNAL_SALE` retornam erro de regra; permanecem exclusivos do fluxo
  `INTERNAL_TRANSFER`.

### Articles

- Leitura pública: `GET /public/articles`, `GET /public/articles/highlights` e
  `GET /public/articles/{slug}`.
- Administração de rascunhos, publicação, destaque e remoção:
  `/api/v1/articles/**`, exclusivamente para `ROLE_ADMIN`.

### Leitura pública de resumo do rebanho

`GET /api/v1/goatfarms/{farmId}/goats/summary` é uma consulta pública marcada
com `@PublicEndpoint` no controller e liberada pelo `SecurityConfig`. Pode ser
chamada sem token e retorna `200` quando a fazenda existe. O DTO contém apenas
agregados do rebanho (total, sexo, situação e distribuição por raça), sem
informações de mutação ou dados de autorização.

As operações de escrita do mesmo recurso continuam exigindo suas políticas de
fazenda (`@CanManageFarm` ou `@FarmOwnerOnly`); a consulta pública não altera
ownership nem expõe mutações.

Exemplo de alerta pendente:

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

### Lactation e Milk Production
Rotas canônicas de lactação:
- `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/lactations`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/active`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/active/summary`
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}/dry`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/lactations/{lactationId}/summary`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/lactations?page=&size=&sort=`
- `GET /api/v1/goatfarms/{farmId}/milk/alerts/dry-off?referenceDate=&page=&size=`

Rotas canônicas de produção de leite:
- `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions`
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions?from=&to=&includeCanceled=&page=&size=&sort=`
- `DELETE /api/v1/goatfarms/{farmId}/goats/{goatId}/milk-productions/{id}`

Paginação atual:
- As listagens de lactação e produção continuam retornando `Page` do Spring para preservar compatibilidade.
- O endpoint `dry-off` retorna envelope agregado com `totalPending` e `alerts`.
- A listagem de produção de leite mantém o JSON Spring atual (`content` e o
  objeto `page` com `number`, `size`, `totalElements` e `totalPages`). A
  neutralização de `Pageable` ocorre somente dentro do backend e não altera o
  contrato HTTP. A listagem de lactação permanece pendente para F4-I2.

Exemplo de alerta de secagem:

```http
GET /api/v1/goatfarms/1/milk/alerts/dry-off?referenceDate=2026-02-10&page=0&size=20
```

```json
{
  "totalPending": 1,
  "alerts": [
    {
      "lactationId": 120,
      "goatId": "BR123",
      "dryOffRecommendation": true
    }
  ]
}
```

### Health (eventos sanitários)
Rotas canônicas:
- `POST /api/v1/goatfarms/{farmId}/goats/{goatId}/health-events`
- `PUT /api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}`
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/done`
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/cancel`
- `PATCH /api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}/reopen`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/health-events/{eventId}`
- `GET /api/v1/goatfarms/{farmId}/goats/{goatId}/health-events?from=&to=&type=&status=&page=&size=&sort=`
- `GET /api/v1/goatfarms/{farmId}/health-events/calendar?from=&to=&type=&status=&page=&size=&sort=`
- `GET /api/v1/goatfarms/{farmId}/health-events/alerts?windowDays=`

Paginação atual:
- As listagens de eventos por cabra e o calendário da fazenda continuam retornando `Page` do Spring para preservar compatibilidade.
- O endpoint `alerts` retorna contadores e listas resumidas, não um `Page`.

Exemplo de alerta sanitário:

```http
GET /api/v1/goatfarms/1/health-events/alerts?windowDays=7
```

```json
{
  "dueTodayCount": 2,
  "upcomingCount": 5,
  "overdueCount": 1,
  "windowDays": 7
}
```

### Idempotência de comandos
Para endpoints que exigem idempotência (ex.: `POST /api/v1/goatfarms/{farmId}/inventory/movements`):
- Header obrigatório: `Idempotency-Key`.
- Primeira execução válida: `201 Created`.
- Mesma key + payload equivalente: replay (`200` com resposta persistida).
- Mesma key + payload diferente: `409 Conflict`.
- Ausência de key: `400 Bad Request`.

### Inventory (itens de estoque)
Para `POST /api/v1/goatfarms/{farmId}/inventory/items`:
- Resposta de criação: `201 Created`.
- Nome duplicado na mesma fazenda: `409 Conflict`.
- Listagem paginada: `GET /api/v1/goatfarms/{farmId}/inventory/items`.

### Inventory (consultas)
- `GET /api/v1/goatfarms/{farmId}/inventory/balances`
  - filtros opcionais: `itemId`, `lotId`, `activeOnly`
  - paginação padrão: `page`, `size`, `sort`
  - resposta paginada com `itemId`, `itemName`, `trackLot`, `lotId`, `quantity`
- `GET /api/v1/goatfarms/{farmId}/inventory/movements`
  - filtros opcionais: `itemId`, `lotId`, `type`, `fromDate`, `toDate`
  - ordenação padrão: `movementDate desc`, `createdAt desc`
  - compatibilidade: a ordenação de movimentos é fixa; parâmetros `sort` são
    aceitos pelo transporte, mas não alteram a ordem efetiva.
  - resposta paginada com `movementId`, `type`, `adjustDirection`, `quantity`, `itemId`, `itemName`, `lotId`, `movementDate`, `reason`, `resultingBalance`, `unitCost`, `subtotalCost`, `freightCost`, `discountAmount`, `totalCost`, `purchaseDate`, `supplierName`, `createdAt`
- validações obrigatórias:
  - `fromDate <= toDate`
  - `size <= 100`

Exemplo de consulta de saldos:

```http
GET /api/v1/goatfarms/1/inventory/balances?itemId=101&page=0&size=20
```

Exemplo de consulta de histórico:

```http
GET /api/v1/goatfarms/1/inventory/movements?type=OUT&fromDate=2026-02-01&toDate=2026-02-28&page=0&size=20
```

### Inventory (entrada por compra)
Para `POST /api/v1/goatfarms/{farmId}/inventory/movements`, uma compra usa `type=IN` e a seguinte composição:

```json
{
  "type": "IN",
  "quantity": 32.143,
  "itemId": 101,
  "movementDate": "2026-08-01",
  "unitCost": 112.0000,
  "freightCost": 45.50,
  "discountAmount": 12.25,
  "purchaseDate": "2026-08-01",
  "supplierName": "Durrancho"
}
```

Resposta financeira calculada pelo servidor:

```json
{
  "unitCost": 112.0000,
  "subtotalCost": 3600.02,
  "freightCost": 45.50,
  "discountAmount": 12.25,
  "totalCost": 3633.27
}
```

- O cliente novo não precisa enviar `totalCost`.
- Se um cliente legado enviar `unitCost` e `totalCost`, o servidor valida a fórmula completa.
- Se enviar apenas `totalCost`, o servidor deriva o custo unitário das mercadorias, considerando frete e desconto.

## Erros/Status
### Estrutura de erro padrão
Erros seguem estrutura `ValidationError`:

```json
{
  "timestamp": "2026-02-18T10:00:00Z",
  "status": 422,
  "error": "Regra de negócio violada",
  "path": "/api/v1/goatfarms/1/inventory/movements",
  "errors": [
    {
      "fieldName": "quantity",
      "message": "Saldo insuficiente para realizar a movimentação"
    }
  ]
}
```

### Mapeamento principal de status
| Status | Origem típica |
|---|---|
| `400 Bad Request` | `InvalidArgumentException`, `IllegalArgumentException`, JSON inválido |
| `401 Unauthorized` | falha de autenticação/token |
| `403 Forbidden` | falha de ownership/perfil |
| `404 Not Found` | `ResourceNotFoundException` |
| `405 Method Not Allowed` | método HTTP não suportado |
| `409 Conflict` | `DuplicateEntityException`, `DataIntegrityViolationException` |
| `415 Unsupported Media Type` | content type não suportado |
| `422 Unprocessable Entity` | `BusinessRuleException`, validação de bean |
| `503 Service Unavailable` | consulta ABCC indisponível ou insuficiente para validação obrigatória |
| `500 Internal Server Error` | erro não tratado |

### Ownership Transfer (W7)

O workflow HTTP de propriedade expõe somente `INTERNAL_TRANSFER`. Todas as
rotas exigem access token JWT; não há `@PublicEndpoint` nem matcher `permitAll`.
ADMIN pode operar globalmente. FARM_OWNER precisa administrar a fazenda
envolvida; OPERATOR não pode solicitar, aceitar, rejeitar, cancelar ou consultar
transferências por este contrato.

Rotas canônicas:

- `POST /api/v1/ownership-transfers`
- `GET /api/v1/ownership-transfers/{transferId}`
- `POST /api/v1/ownership-transfers/{transferId}/accept`
- `POST /api/v1/ownership-transfers/{transferId}/reject`
- `POST /api/v1/ownership-transfers/{transferId}/cancel`
- `GET /api/v1/goatfarms/{farmId}/ownership-transfers?direction=INCOMING|OUTGOING&status=&page=0&size=20`

O request de criação é:

```json
{
  "goatId": 123,
  "targetFarmId": 45,
  "reason": "Transfer between farms",
  "idempotencyKey": "client-generated-key"
}
```

`sourceFarmId` não é aceito como autoridade do cliente: a origem é sempre
resolvida pelo período aberto canônico do Goat. `goatId` e `targetFarmId` são
positivos; `reason` é obrigatório e limitado a 1000 caracteres; a chave de
idempotência é obrigatória e limitada a 255 caracteres. Uma criação válida
retorna `201 Created`, cabeçalho `Location` e o DTO da transferência.

O response não expõe a versão de persistência nem entidades JPA:

```json
{
  "id": 900,
  "goatId": 123,
  "sourceFarmId": 10,
  "targetFarmId": 45,
  "kind": "INTERNAL_TRANSFER",
  "status": "REQUESTED",
  "reason": "Transfer between farms",
  "requestedAt": "2026-09-14T12:00:00Z",
  "acceptedAt": null,
  "effectiveAt": null,
  "completedAt": null,
  "cancelledAt": null
}
```

O ciclo normal é `REQUESTED -> COMPLETED`, `REJECTED` ou `CANCELLED`. Aceite,
rejeição e cancelamento delegam integralmente ao caso de uso transacional do
W6, preservando o ledger de ownership e a projeção legada.

`idempotencyKey` é vinculada ao solicitante: uma repetição exata retorna o
mesmo recurso; a mesma chave com Goat, destino ou motivo diferente retorna
`422` por conflito de regra.

O endpoint de inbox/outbox exige `direction` e aceita `status` opcional, com
`page >= 0` e `1 <= size <= 100`. `INCOMING` filtra `targetFarmId`; `OUTGOING`
filtra `sourceFarmId`. A ordenação é determinística por `requestedAt DESC, id
DESC`, e a resposta usa o envelope paginado (`content`, `totalElements`,
`number`, `size`, `totalPages`).

Respostas esperadas: `400` quando `direction` ou `status` estiver ausente ou
inválido, ou quando outro argumento for sintaticamente inválido; `401` sem
autenticação válida; `403` sem administração da fazenda de origem/destino;
`404` para transferência/fazenda/cabra inexistente ou para tipos de
transferência ainda não expostos; e `422` para Bean Validation do corpo,
limites semânticos de paginação ou violação do ciclo de vida/regra de negócio.

### Goat Ownership History (W9.2)

`GET /api/v1/goats/{goatId}/ownership-history` é privado e exige autenticação
JWT. O parâmetro é exclusivamente o `GoatId` estrutural positivo; não há
`farmId`, RG, registro, TOD, TOE, paginação ou identificador alternativo.
ADMIN pode ler o histórico completo, inclusive quando todos os períodos estão
encerrados. O FARM_OWNER atual precisa administrar a fazenda proprietária
canônica; proprietário apenas histórico e OPERATOR recebem `403`.

Resposta `200`:

```json
{
  "goatId": 42,
  "periods": [
    {
      "farmId": 10,
      "startedAt": "2025-01-01T00:00:00Z",
      "endedAt": "2026-03-01T12:00:00Z",
      "entryType": "MANUAL_IMPORT",
      "exitType": "TRANSFER_OUT",
      "current": false
    },
    {
      "farmId": 20,
      "startedAt": "2026-03-01T12:00:00Z",
      "endedAt": null,
      "entryType": "TRANSFER_IN",
      "exitType": null,
      "current": true
    }
  ]
}
```

A ordem dos períodos é a ordem canônica retornada pelo caso de uso. O
controller não reconsulta persistência nem recalcula autorização. Os campos
internos `id`, versão JPA, `source`, `transferId`, solicitante, motivo,
`idempotencyKey`, `saleId`, nome da fazenda e `cabras.capril_id` não fazem parte
do contrato. Respostas esperadas: `400` para GoatId ausente, inválido, zero,
negativo ou overflow; `401` sem token; `403` sem autorização; `404` quando o
GoatId não existe; e `422` quando o caso de uso reporta inconsistência canônica
por meio do handler global.

## Referências internas
- Handler global: [src/main/java/com/devmaster/goatfarm/config/exceptions/GlobalExceptionHandler.java](../../src/main/java/com/devmaster/goatfarm/config/exceptions/GlobalExceptionHandler.java)
- Entry point 401: [src/main/java/com/devmaster/goatfarm/config/security/CustomAuthenticationEntryPoint.java](../../src/main/java/com/devmaster/goatfarm/config/security/CustomAuthenticationEntryPoint.java)
- Handler 403: [src/main/java/com/devmaster/goatfarm/config/security/CustomAccessDeniedHandler.java](../../src/main/java/com/devmaster/goatfarm/config/security/CustomAccessDeniedHandler.java)
- Módulos oficiais: [../02-modules](../02-modules)
