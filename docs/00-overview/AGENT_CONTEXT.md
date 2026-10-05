# Agent operational context

Last updated: 2026-09-12
Scope: versioned backend context that complements `AGENTS.md` for safe work.

## First reading

Start with [Project status](./PROJECT_STATUS.md), then
[Architecture](../01-architecture/ARCHITECTURE.md),
[Quality gates](../01-architecture/QUALITY_GATES.md), and the
[Documentation portal](../INDEX.md). Code, Flyway, tests, configuration, and CI
remain the primary technical truth.

Before changing a module, read its documentation under
[`docs/02-modules`](../02-modules), its relevant
[API contracts](../03-api/API_CONTRACTS.md), and security rules whenever access,
users, or farms are affected.

## Stable backend rules

- The backend is a modular monolith evolving toward pragmatic hexagonal
  architecture: controllers call use cases; the core knows ports, not concrete
  adapters.
- Every farm-scoped operation preserves `farmId` isolation; roles are
  `ROLE_ADMIN`, `ROLE_FARM_OWNER`, and `ROLE_OPERATOR`.
- `SecurityContextHolder` stays in the current-principal adapter; consumers use
  `CurrentPrincipalQueryUseCase` and `FarmAuthorizationUseCase`.
- Published Flyway migrations are never rewritten. DEV data is disposable, but
  database reset and schema changes remain explicitly gated operations.
- Technical `GoatId` is structurally implemented. RG retains business/ABCC
  semantics and compatibility during its transition.

## Work procedures

Repository procedures are under `.agents/skills/`:

- `caprigestor-architecture` for boundaries and architecture decisions;
- `caprigestor-safe-refactor` for invariant-preserving refactors;
- `caprigestor-audit` for read-only analysis;
- `caprigestor-documentation` for active and historical documentation.

They are process guidance. Concrete authorization is still required for
mutations, external operations, and Git actions.

## Navigation by task

| Task | Required sources |
|---|---|
| Business rule | `BUSINESS_DOMAIN.md` and the relevant module documentation |
| HTTP/API | `API_CONTRACTS.md` and affected controller/use case |
| Security | Authority module, authorization tests, and Security configuration |
| Persistence | ports/adapters, entities, migrations, and integration tests |
| Architecture | `ARCHITECTURE.md`, `QUALITY_GATES.md`, and ArchUnit tests |
| State/planning | `PROJECT_STATUS.md`; roadmap only as historical planning |

## Validation and documentation

Start with focused tests and run `mvnw.cmd -B clean verify` when scope and
environment allow it. Review `git diff` and `git status` when done. An
architecture change is incomplete until the active documents that describe it
are synchronized.
