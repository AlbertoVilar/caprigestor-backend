# Status do Projeto CapriGestor Backend

Última atualização: 2026-09-12
Escopo: único estado humano versionado e conciso do backend. Código, migrations,
testes, configuração e CI são a fonte técnica primária.

Links: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md),
[Quality Gates](../01-architecture/QUALITY_GATES.md),
[Contratos API](../03-api/API_CONTRACTS.md).

## Baseline atual

- A baseline integrada de `develop` inclui a PR #269 (`DEV-A11-I3-A`), merge
  `f1ff12f`; as boundaries Authority/Security I2 e Article I3-A estão concluídas.
- A última migration é `V44__enforce_single_active_lactation.sql`; não há V45.
- O backend é um monólito modular Java/Spring Boot com PostgreSQL/Flyway,
  autenticação JWT e autorização farm-scoped.
- `CAPRIGESTOR_CURRENT_STATE.md`, se existir localmente, é cache não
  autoritativo e candidato à remoção; não é necessário em clone limpo.
- O estado de execução e a saúde do CI devem ser confirmados nos workflows e
  no repositório no momento da tarefa; este documento não substitui essa prova.

## Arquitetura atual

O destino é arquitetura hexagonal pragmática: API/Web → casos de uso → domínio,
com persistência, segurança e integrações atrás de ports/adapters. O core já
possui boundaries mecânicos para domínio, controller, Goat, lactação, leite,
reprodução e segurança, mas ainda há dívida legada em outros módulos.

Autorização usa `FarmAuthorizationUseCase` e `CurrentPrincipalQueryUseCase`.
`SecurityContextHolder` permanece confinado ao adapter de principal atual. As
rotas farm-scoped declaram políticas semânticas (`@CanManageFarm`,
`@FarmOwnerOnly`, `@AdminOnly`, `@AuthenticatedFarmRead` ou `@PublicEndpoint`).

## Waves concluídas relevantes

- DEV-A11-R (Final Hexagonal Closure Audit) foi concluída em modo read-only. Os
  14 pares legados de ports de aplicação para entidades JPA foram identificados;
  após a I2-C, restavam 11 pares explícitos. As waves DEV-A11-I3-A (Article),
  DEV-A11-I3-B (Health), DEV-A11-I3-C (Farm/Address/Phone) e DEV-A11-I3-D
  (Audit) reduziram a baseline atual para 4 pares legados fora destas
  boundaries.
- A10 isolou limites de principal autenticado, autorização por fazenda,
  validação crítica, publicação de eventos e emissão de JWT.
- A11-I1 reforçou o guard global que impede o domínio de depender de
  `application`.
- GoatId técnico foi introduzido e propagado estruturalmente pelas migrations
  V39–V43. FKs locais críticas usam identidade técnica; RG permanece
  identificador registral/ABCC e snapshot de negócio.
- V44 reforçou a regra de uma única lactação ativa por animal/fazenda.
- A retificação registral preserva GoatId, é administrativa e mantém histórico
  imutável; o `PUT` comum não altera identidade.
- DEV-A11-I2 (Password Hashing, Authentication/Token e Account/Role Persistence)
  foi concluída e integrada no merge `adb025d` (PR #267). O core Authority não
  depende de infraestrutura Spring Security nem de entidades JPA de Authority.

## Dívida arquitetural conhecida

- O baseline `ApplicationPortPersistenceBoundaryArchUnitTest` contém atualmente
  4 pares explícitos de ports de aplicação ainda acoplados a entidades JPA.
  É dívida de migração conhecida, não aceitação permanente: será removida
  progressivamente em DEV-A11-I3 e pode diminuir, nunca crescer sem revisão.
- A boundary DEV-A11-I3-C removeu `FarmUserPersistencePort` e o adapter de User:
  o onboarding de fazenda usa o contrato de aplicação `UserManagementUseCase`
  e `AuthorityAccount`, enquanto o adapter de Farm resolve a entidade JPA.
- Consumidores legados de Commercial e Finance ainda usam projeções mínimas de
  fazenda durante a migração; permanecem na allowlist de 4 pares e não devem
  ganhar novos acoplamentos. Audit já usa `OperationalAuditRecord` e está
  isolado de entidades JPA no core (DEV-A11-I3-D).
- Há dependências legadas do core a JPA entities, `Page`/`Pageable`/`Sort` e,
  no contexto Authority, a APIs de autenticação. Guards globais para essas
  dívidas permanecem planejados até a remoção incremental.
- A transição de identidade ainda contém compatibilidades de API/token por RG e
  `String goatId`. Esse trabalho chama-se **Goat Identity Transition Closure**;
  não é a criação de GoatId.
- O antigo plano estrutural **ID4-B0/ID4** é obsoleto e já foi implementado nas
  migrations V39–V43 e no modelo técnico atual. Não há trabalho futuro para
  criar GoatId, promover `cabras.id` ou repetir a migração estrutural; somente
  resíduos comprovados de **Goat Identity Transition Closure** permanecem.

## Wave ativa e trabalho adiado

- DEV-A11-I2-A (Password Hashing Boundary) foi concluída e integrada na
  `develop`. O core Authority depende de `PasswordHashingPort`; o
  `PasswordEncoder` do Spring permanece somente no adapter de infraestrutura.
- DEV-A11-I2-B (Authentication and Token Boundaries) foi concluída e integrada
  na `develop`. `AuthBusiness` depende de `CredentialAuthenticationPort` e
  `AuthTokenPort`; as APIs concretas de autenticação e JWT permanecem confinadas
  aos adapters de infraestrutura. Login, refresh, rotação, replay, logout,
  claims e contratos HTTP são preservados.
- DEV-A11-I2-C (Authority Account & Role Persistence Boundary) foi concluída e
  integrada. `AuthorityAccount`, `AuthorityRole`, `RefreshSessionRecord` e
  `PasswordResetTokenRecord` são modelos da aplicação; JPA permanece nos
  adapters/mapper. O onboarding de fazenda usa uma ponte explícita e transitória
  fora do core Authority para a relação legada com `User`.
- I2-D e I2-E, como descritas no roadmap antigo, são obsoletas como waves
  arquiteturais independentes: seus limites de persistência e UserDetails já
  foram cobertos pela I2-C. A implementação de `User implements UserDetails` é
  apenas limpeza opcional futura.
- A atomicidade concorrente do consumo de token de recuperação de senha é dívida
  de hardening de segurança separada, não dívida hexagonal. As boundaries Article,
  Health e Farm/Address/Phone foram implementadas; a revisão arquitetural da
  I3-C permanece pendente.
- DEV-A11-I2-P0 foi integrada na `develop`: as duas rotas HTTP de limpeza global
  foram removidas, assim como a credencial hard-coded e a orquestração sem
  consumidores; o bootstrap administrativo continua externo e desabilitado por
  padrão.
- A boundary Health da DEV-A11-I3-B e a boundary Audit da DEV-A11-I3-D foram
  concluídas em suas branches de implementação; o merge de I3-D aguarda
  revisão arquitetural.
- Adiado: remoção da dívida I3 restante, hardening separado de recuperação de senha,
  mudanças adicionais de contrato/API,
  mudanças de schema, reset DEV, HML e `main`.

## Compatibilidade e operações

- A base DEV contém dados descartáveis para teste, mas reset continua operação
  explícita e futura. Migrations publicadas não são reescritas.
- RG é lookup registral e pode coexistir com GoatId técnico durante a transição;
  aliases/fallbacks não devem ser removidos sem decisão documentada de contrato.
- Antes de HML, o projeto exigirá instalação limpa PostgreSQL → Flyway
  V1..latest → startup → bootstrap/smoke tests.

## Onde continuar

Use [ARCHITECTURE.md](../01-architecture/ARCHITECTURE.md) para target e dívida,
[QUALITY_GATES.md](../01-architecture/QUALITY_GATES.md) para o que CI já impõe,
e os documentos de módulo/API para mudanças funcionais. Roadmaps e planos
históricos ajudam a entender decisões, mas não definem o estado atual.
