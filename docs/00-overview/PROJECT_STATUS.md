# Status do Projeto CapriGestor Backend

Última atualização: 2026-09-12
Escopo: único estado humano versionado e conciso do backend. Código, migrations,
testes, configuração e CI são a fonte técnica primária.

Links: [Portal](../INDEX.md), [Arquitetura](../01-architecture/ARCHITECTURE.md),
[Quality Gates](../01-architecture/QUALITY_GATES.md),
[Contratos API](../03-api/API_CONTRACTS.md).

## Baseline atual

- A baseline integrada de `develop` é `f46c375` (merge da PR #262).
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
  14 pares legados de ports de aplicação para entidades JPA foram identificados
  e classificados como dívida arquitetural conhecida, a ser removida
  progressivamente em DEV-A11-I3.
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

## Dívida arquitetural conhecida

- O baseline `ApplicationPortPersistenceBoundaryArchUnitTest` contém os 14 pares
  explícitos já classificados pela DEV-A11-R como violações legadas de ports de
  aplicação ainda acoplados a entidades JPA. É dívida de migração conhecida,
  não aceitação permanente: será removida progressivamente em DEV-A11-I3 e pode
  diminuir, nunca crescer sem revisão.
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

- Ativa: **harness de arquitetura e governança** — procedures versionadas,
  documentação ativa coerente e um guard zero-baseline para `JpaRepository` no
  core.
- Próximo gate, após a estabilização/merge do harness: **DEV-A11-I2-P0 — Admin Maintenance
  Security Containment**. Os endpoints administrativos destrutivos e credencial
  hard-coded precisam de decisão de uso/isolamento antes de evoluir Authority.
- Adiado: remoção de dívida A11-I2/I3, mudanças de contrato/API, mudanças de
  schema, reset DEV, HML e `main`.

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
