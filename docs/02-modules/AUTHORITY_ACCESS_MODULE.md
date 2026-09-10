# Módulo Authority / acesso / recuperação de senha
Última atualização: 2026-09-08
Escopo: autenticação, refresh, cadastro inicial, administração de usuários e recuperação de senha do CapriGestor.
Links relacionados: [Portal](../INDEX.md), [Contratos da API](../03-api/API_CONTRACTS.md), [Arquitetura](../01-architecture/ARCHITECTURE.md), [Rotação JWT](../04-security/JWT_KEY_ROTATION_RUNBOOK.md), [Resposta a incidentes](../04-security/SECURITY_INCIDENT_RESPONSE.md)

## Administração de usuários

- Todos os endpoints em `/api/v1/users/**` são exclusivamente administrativos e exigem `ROLE_ADMIN`.
- A restrição existe tanto na configuração HTTP quanto no `UserController`, como defesa em profundidade.
- Alterações administrativas de senha e de papéis também validam a autoridade antes de codificar senha, consultar papéis ou modificar uma entidade persistente.
- `POST /api/v1/auth/register` permanece público, não recebe papéis no contrato e cria o usuário somente com o papel padrão `ROLE_OPERATOR`. Campos desconhecidos, inclusive uma tentativa de enviar `roles`, são rejeitados.
- A autorização por fazenda distingue propriedade e operação: ADMIN possui acesso global, FARM_OWNER precisa ser o responsável da fazenda e OPERATOR precisa de vínculo persistido em `FarmOperator`.
- `OwnershipService` consulta o vínculo operacional por `FarmAccessQueryPort`; o adapter de persistência concentra o acesso ao repositório Spring Data.
- Na W4, a consulta do proprietário foi reduzida ao contrato `FarmOwnerQueryPort`,
  e a decisão recebe `AuthenticatedPrincipal` (id, email, nome e authorities),
  evitando que a emissão de JWT e as verificações de ownership precisem
  carregar a entidade JPA `User`.
- `GET /api/v1/auth/me` permanece disponível para o usuário autenticado consultar os próprios dados. Esta correção não cria uma API de edição do perfil próprio.
- O endpoint legado de diagnóstico de papéis foi removido: não possuía consumidor funcional e expunha dados administrativos desnecessários.
- O fluxo interno de atualização do responsável por uma fazenda continua protegido pela validação de propriedade e não permite alteração de papéis.
- Alterações de senha, redefinição de senha e alterações de papéis revogam todas as sessões de refresh do usuário. O access token já emitido continua válido somente até sua expiração curta.

## Políticas semânticas de autorização

Os controllers usam poucas annotations de intenção, definidas em
`config.security.authorization`, sem alterar a política efetiva:

- `@AdminOnly`: somente `ROLE_ADMIN`.
- `@FarmOwnerOnly`: `ROLE_ADMIN` ou `ROLE_FARM_OWNER` proprietário da fazenda;
  não concede capacidade a `ROLE_OPERATOR`.
- `@CanManageFarm`: `ROLE_ADMIN`, proprietário oficial da fazenda ou
  `ROLE_OPERATOR` com vínculo persistido em `FarmOperator`.
- `@PublicEndpoint`: marca uma rota pública aprovada pela configuração HTTP.
- `@AuthenticatedFarmRead`: marca uma leitura farm-scoped que exige somente
  autenticação por compatibilidade, sem transformar essa leitura em ownership.

O contrato administrativo de fazenda é separado do catálogo público:
`GET /api/v1/goatfarms/{farmId}/management` usa `@FarmOwnerOnly` e devolve os
dados completos necessários à edição apenas para ADMIN ou FARM_OWNER oficial
da fazenda. OPERATOR não recebe capacidade administrativa. O endpoint público
`GET /api/v1/goatfarms/{farmId}` permanece sanitizado e não deve ser reutilizado
como fonte de dados privados para telas de manutenção.

O resumo agregado do rebanho (`GET /api/v1/goatfarms/{farmId}/goats/summary`)
usa `@PublicEndpoint`, pois sua política aprovada é pública. A marca
`@AuthenticatedFarmRead` permanece disponível para futuras leituras farm-scoped
que exijam autenticação; ela não é usada por esse endpoint.

| Política | ADMIN | FARM_OWNER da própria fazenda | FARM_OWNER de outra fazenda | OPERATOR vinculado | OPERATOR sem vínculo | Anônimo |
| --- | --- | --- | --- | --- | --- | --- |
| `@CanManageFarm` | permite | permite | nega | permite | nega | `401` |
| `@FarmOwnerOnly` | permite | permite | nega | nega | nega | `401` |
| `@AdminOnly` | permite | nega | nega | nega | nega | `401` |
| `@PublicEndpoint` | permite | permite | permite | permite | permite | público |
| `@AuthenticatedFarmRead` | permite | permite | permite* | permite* | permite* | `401` |

\* A leitura autenticada não aplica ownership por compatibilidade; o escopo de
dados continua sendo responsabilidade do caso de uso/adapter correspondente.

As duas policies farm-scoped resolvem o parâmetro de método `farmId`. O guard
`AuthorizationPolicyGuardTest` falha se um novo endpoint de controller sob
`/api/v1/goatfarms` não declarar uma dessas intenções, uma rota pública,
autenticação explícita ou uma exceção `@PreAuthorize` documentada. O mesmo
guard verifica que policies farm-scoped continuam recebendo um parâmetro
`farmId`, evitando uma anotação aplicada silenciosamente ao identificador errado.

`GET /api/v1/goatfarms/{farmId}/permissions` permanece uma exceção legítima:
aceita qualquer papel oficial autenticado e retorna as capacidades calculadas
para a fazenda solicitada. O contrato é farm-scoped e expõe apenas duas
intenções semânticas: `canOperateFarm` (a mesma política de `@CanManageFarm`,
incluindo OPERATOR vinculado) e `canAdministerFarm` (a mesma política de
`@FarmOwnerOnly`, ADMIN global ou FARM_OWNER da própria fazenda). O backend é
a fonte de verdade do vínculo operador–fazenda; clientes não devem inferir
permissões operacionais apenas pela role do token.

### Limites de segurança

`OwnershipService` continua oferecendo `isFarmOwner` para políticas
administrativas e `canManageFarm` para operações de fazenda. As duas funções
compartilham a mesma resolução de principal e do proprietário oficial, mas
`canManageFarm` também consulta o vínculo persistido em `FarmOperator` para
permitir operadores. A diferença é deliberada: trocar uma chamada por outra
altera a política, não apenas a forma de consulta.

## Sessão JWT

- O access token é emitido com `typ=access`, emissor, audiência, `kid` e `jti`; sua duração padrão é 15 minutos (`security.jwt.duration`).
- O refresh token é emitido com `typ=refresh`, `scope=REFRESH`, `jti` e `familyId`; sua duração padrão é 7 dias (`security.jwt.refresh-duration`).
- `POST /api/v1/auth/refresh` aceita exclusivamente refresh tokens. Cada uso consome a sessão persistida, emite uma nova sessão da mesma família e registra a substituição.
- Reuso de refresh token consumido, expirado ou revogado invalida toda a família. Não são armazenados tokens brutos: apenas SHA-256, identificadores e metadados de ciclo de vida.
- `POST /api/v1/auth/logout` revoga a família do refresh token apresentado e retorna `204`.

## Recuperação de senha MVP
Entrou neste MVP:
- solicitacao publica de reset por email
- token aleatorio forte com hash persistido
- token de uso unico
- expiracao curta de 30 minutos
- invalidacao de tokens anteriores do mesmo usuario
- cooldown simples por email/usuario
- resposta neutra para nao revelar existencia de email
- envio de email com link de redefinicao
- tela publica de solicitacao e tela publica de redefinicao

## Endpoints
- `POST /api/v1/auth/password-reset/request`
- `POST /api/v1/auth/password-reset/confirm`

## Fluxo
1. O usuario informa o email na tela `Esqueci minha senha`.
2. O backend sempre responde com a mesma mensagem neutra.
3. Se o email existir e nao estiver em cooldown, o sistema invalida tokens anteriores, gera um novo token, persiste apenas o hash e envia o link por email.
4. O usuario abre o link recebido.
5. O frontend envia token bruto + nova senha + confirmacao para o backend.
6. O backend valida token, expiracao, uso unico e revogacao.
7. A senha e atualizada com o mesmo encoder BCrypt ja usado no projeto.
8. O token e marcado como utilizado, as sessões de refresh existentes são revogadas e não podem ser reutilizadas.

## Configuracao local / HML
Variaveis relevantes:
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USER`
- `MAIL_PASSWORD`
- `MAIL_SMTP_AUTH`
- `MAIL_SMTP_STARTTLS`
- `MAIL_TIMEOUT_MS`
- `PASSWORD_RESET_FRONTEND_BASE_URL`
- `PASSWORD_RESET_FROM_ADDRESS`
- `PASSWORD_RESET_TTL_MINUTES`
- `PASSWORD_RESET_COOLDOWN_SECONDS`
- `JWT_DURATION` (segundos; padrão local: 900)
- `JWT_REFRESH_DURATION` (segundos; padrão local: 604800)
- `JWT_ISSUER`, `JWT_AUDIENCE`, `JWT_KEY_ID` (obrigatórios em produção)

## Validacao local com Mailpit
1. Suba o ambiente: `docker compose -f docker/docker-compose.yml up -d mailpit`
2. A interface do Mailpit fica em [http://localhost:8025](http://localhost:8025)
3. Solicite um reset com um email existente.
4. Abra o email capturado no Mailpit.
5. Siga o link de redefinicao.
6. Confirme que a nova senha funciona no login.

Padrao local recomendado:
- `PASSWORD_RESET_FRONTEND_BASE_URL=http://localhost:5173`

## O que ficou para fase 2
- revogação imediata de access tokens (hoje a revogação é imediata para refresh tokens; access tokens expiram em até 15 minutos)
- rate limit por IP
- captcha / anti-abuso adicional
- envio assincrono de email
- templates de email mais ricos
- observabilidade dedicada do fluxo

## Observação de segurança

As sessões de refresh são invalidadas automaticamente quando a senha ou os papéis mudam. O access token não é consultado no banco a cada requisição; por isso a contenção total depende do seu TTL curto até que exista uma lista de revogação distribuída.
