---
name: caprigestor-architecture
description: Decide CapriGestor backend architectural boundaries, dependency direction, and ports/adapters when a change or review affects the business core. Do not use for ordinary localized edits without a boundary decision.
---

# CapriGestor architecture

Use this procedure when a decision affects boundaries between domain,
application, API, persistence, security, integration, or modules.

## Expected outcome

Choose the smallest design that preserves observable behavior and makes the
dependency correct and verifiable. State whether the conclusion is target,
current state, or known debt; never present debt as completed architecture.

## Dependency direction

- API/Web calls use cases and maps transport; it does not access persistence
  repositories or entities directly.
- Application orchestrates and defines ports. Adapters implement ports and hold
  database, framework, and external-integration details.
- Domain contains business rules and types. It does not depend on Web, Spring
  Security, JPA/Hibernate, controllers, adapters, or HTTP DTOs.
- Technical security stays at the edge. Principal and authorization reach the
  core through `CurrentPrincipalQueryUseCase` and `FarmAuthorizationUseCase`.
- Cross-module references use minimal contracts, snapshots, or a stable shared
  kernel; do not cross the boundary with a JPA entity.

## Pragmatic decision process

1. Read relevant code, migrations, tests, and active documentation.
2. Identify the business rule and the real consumer of the dependency.
3. Check whether an existing port or contract already serves the boundary.
4. Create or alter an abstraction only for a demonstrated boundary or
   maintainability benefit. Do not abstract framework use for academic purity.
5. Preserve transactions, serialization, Spring proxies, authorization, and
   external contracts; treat each risk as evidence to verify, not an assumption.
6. Update guards and active documentation only after authorized changes have
   altered the real state.

## Important containment rules

- Technical `GoatId` already exists. Identity migration, schema, and RG
  compatibility belong to an explicitly approved wave, never casual refactor.
- JPA entities, `JpaRepository`, `Page`/`Pageable`/`Sort`, and authentication
  components still have legacy debt in specific modules. Do not declare a
  global zero-tolerance guard before removing and testing its debt.
- Every farm-scoped policy preserves `farmId` and current `ADMIN`,
  `FARM_OWNER`, and `OPERATOR` semantics.

Read `docs/01-architecture/ARCHITECTURE.md` and
`docs/01-architecture/QUALITY_GATES.md` before proposing a design.
