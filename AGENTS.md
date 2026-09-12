# CapriGestor Backend — agent guide

## Architectural north star

The permanent objective is a pragmatic hexagonal architecture with a
progressively isolated business core. Preserve observable behavior, REST
contracts, security, and integrity before pursuing academic purity. Use DDD,
SOLID, and Clean Code to clarify real boundaries; do not create abstractions
without a demonstrated boundary or maintainability benefit.

The core must progressively stop depending on JPA entities, `JpaRepository`,
Spring Data pagination, HTTP, controllers, `SecurityContextHolder`,
authentication details, persistence adapters, and concrete repositories.
`@Service` and appropriate transaction boundaries are not violations by
themselves.

## Source-of-truth hierarchy

Establish facts in this order:

1. tracked code, Flyway migrations, tests, configuration, and CI;
2. recent Git/PR history when needed;
3. `docs/00-overview/PROJECT_STATUS.md` — the only versioned human current state;
4. `docs/01-architecture/ARCHITECTURE.md` and architecture guards;
5. module, API, security, and operational documentation;
6. historical ADRs, plans, and roadmaps.

Do not treat ignored files, local reports, or old roadmaps as authoritative
state. Record divergence and follow the current repository.

## Local procedures

Backend procedures live in `.agents/skills/` and are auto-discovered by Codex
sessions started in this repository.

- `$caprigestor-architecture`: boundaries, ports, adapters, and architecture.
- `$caprigestor-safe-refactor`: structural change without behavioral regression.
- `$caprigestor-audit`: read-only audit and code-documentation comparison.
- `$caprigestor-documentation`: documentation classification and synchronization.

Read the applicable procedure before architecture work, refactoring, auditing,
or documentation maintenance. Procedures guide process; they do not extend task
authorization.

## Context routing

- Current state and next wave: `docs/00-overview/PROJECT_STATUS.md`.
- Target architecture and known debt: `docs/01-architecture/ARCHITECTURE.md`.
- Active and planned gates: `docs/01-architecture/QUALITY_GATES.md`.
- Business rules: `docs/00-overview/BUSINESS_DOMAIN.md` and module docs.
- HTTP contract: `docs/03-api/API_CONTRACTS.md`.
- Authorization and access: `docs/02-modules/AUTHORITY_ACCESS_MODULE.md`.
- Module-specific rules: `docs/02-modules/` before changing a module.
- Documentation navigation: `docs/INDEX.md`.

## Safe-change protocol

1. Establish branch, base, `git status`, scope, and affected consumers.
2. Identify domain, API, security, database, and compatibility invariants.
3. Make the smallest coherent change. Do not hide functional, contract, schema,
   or migration changes inside a structural refactor.
4. Keep dependencies leaving the core behind ports and concrete adapters outside
   it. Do not bypass use cases or authorization policies.
5. Review the diff and update active documentation that represents changed facts.

Never rewrite published Flyway migrations. Do not remove compatibility, alter
persistent data, reset a database, deploy, commit, push, merge, or open a PR
without explicit authorization for that action.

## Security and data

- Every farm-scoped route must keep an explicit policy and `farmId` isolation.
- Official roles: `ROLE_ADMIN`, `ROLE_FARM_OWNER`, `ROLE_OPERATOR`.
- Authorization goes through `FarmAuthorizationUseCase`; the authenticated
  principal goes through `CurrentPrincipalQueryUseCase`.
- `SecurityContextHolder` stays in the current-principal adapter.
- Never record secrets, tokens, credentials, or personal data in work logs or
  documentation.

## Goat identity and persistence

Technical `GoatId` and structural migrations V39–V43 already exist. Remaining
work is *Goat Identity Transition Closure*, not GoatId creation. RG is a
business/ABCC identifier and compatibility layer; do not remove aliases or
fallbacks without an explicit contract decision.

The old ID4-B0/ID4 structural implementation plan is obsolete and already
implemented. Do not schedule work to create GoatId, promote `cabras.id`, or
repeat V39–V43. Only proven transition residues may be planned.

The DEV database contains disposable data, but reset, schema change, and new
Flyway migration remain explicit gates. They are never automatic refactor work.

## Tests and quality

Start with the smallest relevant test set and, when feasible, run the normal
gate:

```powershell
.\mvnw.cmd -Dtest=GlobalHexagonalBoundaryArchUnitTest test
.\mvnw.cmd -Dtest=HexagonalArchitectureGuardTest test
.\mvnw.cmd -B clean verify
```

Do not remove, ignore, weaken, or rewrite tests merely to make them pass.
Architecture baselines may only shrink; never expand an allowlist without
explicit architectural approval.

## Documentation and language

Active documentation must reflect the current repository. Preserve ADRs, plans,
and historical records; mark supersession when needed instead of rewriting the
past. Do not create root Markdown files other than `README.md`.

Write all agent-facing instructions and Conventional Commits in English. Product
documentation may use Portuguese when it serves its intended audience.

## Escalation and stop conditions

Stop and report before schema/Flyway changes, REST contract changes,
authorization semantics, Goat deletion semantics, persistent data operations,
HML/production work, or `main` changes. For architectural ambiguity,
Authority/security, schema, dangerous contracts, or difficult regressions,
report `MODEL ESCALATION RECOMMENDED: <reason>`.
