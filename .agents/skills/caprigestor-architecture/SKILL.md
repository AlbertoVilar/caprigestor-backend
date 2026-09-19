---
name: caprigestor-architecture
description: Decide CapriGestor backend architectural boundaries, dependency direction, and ports/adapters when a change or review affects the business core. Do not use for ordinary localized edits without a boundary decision.
---

# CapriGestor architecture

Use this procedure when a decision affects boundaries between domain, application, API, persistence, security, integration, or modules.
For lifecycle transitions, Git safety, and reviewer gates, refer to `$caprigestor-gated-delivery`.

## Expected outcome

Choose the smallest design that preserves observable behavior and makes the dependency correct and verifiable. State explicitly whether each finding or conclusion represents CURRENT STATE, TARGET, or KNOWN DEBT; never present debt or aspirational architecture as completed state.

## Dependency direction & boundaries

- **API / Web**: Calls application use cases and maps transport DTOs. Never accesses persistence repositories or JPA entities directly.
- **Application**: Orchestrates domain logic, enforces use case policies, and defines outbound ports. Adapters implement ports and isolate database, framework, and integration mechanics. Spring `@Service` and declarative transaction boundaries (`@Transactional`) are permitted in the application layer where already established.
- **Domain**: Contains pure business rules, domain entities, and value types. Strictly independent of Web, Spring Security, JPA/Hibernate annotations, controllers, persistence adapters, and HTTP contracts.
- **Security Edge**: Technical security mechanics stay at the edge. Principal and farm authorization reach the core exclusively through `CurrentPrincipalQueryUseCase` and `FarmAuthorizationUseCase`.
- **Cross-Module Boundaries**: Cross-module communication uses neutral contracts, ports, read snapshots, or a stable shared kernel. Never cross module boundaries with JPA entities, and never allow cross-module direct repository access.

## Domain identity & authority invariants

- **Structural Identity**: `GoatId` represents technical structural identity. `RG` is a public, business, and ABCC registration snapshot and compatibility identifier. Conflicting non-null `GoatId` with identical `RG` must fail closed; never infer structural identity equivalence from `RG`.
- **Ownership vs Provenance vs Visibility**:
  - Current ownership authority (`ownership` ledger) != historical recording provenance (immutable recorded attribution) != read visibility.
  - Current animal projection (`cabras.capril_id`) is not historical provenance.
  - Do not rewrite historical recording provenance upon animal transfer.
- **Public Read vs Mutation Authority**: Evaluated independently. Former owners may retain authorized historical read visibility without retaining mutation authority. Current owners do not automatically acquire mutation authority over historical records authored by another farm.
- **Fail-Closed Ambiguity**: When historical identity, provenance, ownership, authority, or visibility evidence is ambiguous: **FAIL CLOSED**. Never infer authorization from mutable projections or missing records.

## Pragmatic decision process

1. Read relevant code, Flyway migrations, tests, and active documentation.
2. Identify the business rule and the real consumer of the dependency.
3. Check whether an existing port, contract, or query already serves the boundary. Prefer neutral ports over ad-hoc cross-module access.
4. Create or alter an abstraction only for a demonstrated boundary or maintainability benefit. Do not abstract framework usage for academic purity.
5. Preserve transactions, serialization, Spring proxies, authorization, and external contracts; treat each risk as evidence to verify, not an assumption.
6. Architecture guards must reflect actual architecture, not desired architecture. Allowlists may only shrink; never expand an allowlist or weaken a guard to make tests pass.
7. Update guards and active documentation only after authorized changes have established the real state in code.

## Containment & legacy debt rules

- Technical `GoatId` already exists (migrations V39–V43). Identity migration, schema alterations, and RG compatibility adjustments belong to explicitly approved waves, never casual refactoring.
- Legacy debt (e.g. residual `JpaRepository` or Spring Data pagination in specific modules) must be addressed incrementally within authorized scope. Do not declare a global zero-tolerance guard before removing and testing its debt.
- Every farm-scoped policy preserves `farmId` and current `ROLE_ADMIN`, `ROLE_FARM_OWNER`, and `ROLE_OPERATOR` semantics.

Read `docs/01-architecture/ARCHITECTURE.md` and `docs/01-architecture/QUALITY_GATES.md` before proposing a design.
