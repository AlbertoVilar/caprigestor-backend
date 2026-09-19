# CapriGestor Backend — agent guide

## 1. Instruction hierarchy & stop conditions

Authority and context are governed by distinct roles:

- **Current reviewer gate**: defines the active authorization and task scope. An agent must never exceed the explicitly granted action.
- **`AGENTS.md`**: defines permanent repository-level rules and safety invariants. A reviewer gate does not silently erase permanent safety rules.
- **Applicable specialized skills (`.agents/skills/`)**: define specialized execution procedures.
- **Canonical documentation (`docs/`)**: provides factual, architecture, and domain context.

If an actual contradiction exists between instructions, scope is ambiguous, or unexpected state is encountered: **FAIL CLOSED AND STOP**. Report the conflict immediately rather than selecting the most permissive rule.

## 2. Reviewer / executor contract & gated delivery

The agent is an **EXECUTOR**. The CapriGestor REVIEWER / GATEKEEPER is the sole approval authority. The executor must **NEVER self-approve** any lifecycle step. Passing tests, clean diffs, and green CI are evidence for the reviewer, not self-authorization.

Any task that modifies or may modify tracked repository state must follow **`$caprigestor-gated-delivery`**.

The canonical delivery lifecycle is:
```
IMPLEMENTATION -> REVIEWER GATE -> LOCAL COMMIT -> REVIEWER GATE -> PUSH + PR -> REMOTE CI -> REVIEWER GATE -> MERGE -> POST-MERGE VERIFICATION -> FORMAL CLOSURE -> NEXT WAVE
```

- **Explicit Lifecycle Gates**: Explicit reviewer authorization is required before lifecycle mutation actions: implementation (when not already authorized), local commit, push + PR, merge, deployment, or other separately gated mutations.
- **Verification Phases**: Verification steps (such as remote CI observation or post-merge verification explicitly included in the merge authorization) do not require an artificial extra gate merely to observe or verify state.
- **Formal Closure & Next Wave**: Formal closure of a wave remains a reviewer decision. Beginning the next wave ALWAYS requires separate explicit reviewer authorization.
- **Automatic Reviewer Handoff**: Upon finishing an authorized task, complete state-bound validation, inspect the diff, autonomously generate the reviewer handoff report, and STOP. Do not ask routine questions.
- **Post-Merge Verification & Remote Freshness**: Formal closure requires remote base freshness verification (`git fetch origin` followed by `git pull --ff-only`; if fetch cannot run, report freshness as UNVERIFIED), verified clean working tree, and explicit reviewer sign-off.

## 3. Working tree, Git & stash safety

- **Unrelated Work Stop Rule**: If an agent detects pre-existing unrelated modifications or untracked files outside the authorized scope, the agent **MUST STOP IMMEDIATELY** and report. Never move, stash, reset, restore, clean, or incorporate unrelated work into a branch.
- **Stash Safety**: Pre-existing stashes may contain user work. Never apply, pop, drop, clear, rewrite, or reorder a stash without explicit reviewer authorization. Do not create a stash merely as an automatic convenience. If creating a stash is needed to preserve or move existing work, obtain explicit reviewer authorization first. Never silently move unrelated work.
- **Scope Fingerprint**: Capture branch, base SHA, HEAD, and working tree status before and after changes. If an unexpected file appears in `git status`: **STOP IMMEDIATELY**.
- **Base Integrity**: Never silently rebase, squash, amend, reset, or force push.

## 4. Evidence integrity & state-bound validation

- **Truthful Reporting**: Never claim a test passed, command succeeded, SHA was verified, or CI was observed unless it was actually executed and observed. Never fabricate output or simulate terminal execution.
- **Raw Git Artifacts**: Never reconstruct patch or diff text manually or with AI. Export raw patches directly from Git outside the repository (`git diff --no-ext-diff <args> > <path>`).
- **Evidence Contradiction Stop Rule**: If reported evidence contradicts repository source, enum definitions, Flyway migrations, Git history, test output, or schema: **STOP IMMEDIATELY** and reconcile before proceeding.
- **State-Bound Validation**: Validation results belong strictly to the exact source state that produced them. Any source modification after a test run invalidates previous validation claims for the changed scope. Re-run required validation after the final change.
- **Validation-Level Truthfulness**: Default test execution (`src/test/resources/application-test.properties`) uses in-memory H2 with `ddl-auto=create-drop` and Flyway disabled. H2 does NOT validate PostgreSQL syntax, schema constraints, or Flyway migrations. Report validation levels honestly (unit, mocked adapter, H2/JPA, PostgreSQL/Testcontainers, CI). Never claim PostgreSQL constraints were verified when only H2 was executed.

## 5. Architectural north star

The permanent objective is a pragmatic hexagonal architecture with a progressively isolated business core. Preserve observable behavior, REST contracts, security, and integrity before pursuing academic purity. Use DDD, SOLID, and Clean Code to clarify real boundaries; do not create abstractions without a demonstrated boundary or maintainability benefit.

The core must progressively stop depending on JPA entities, `JpaRepository`, Spring Data pagination, HTTP, controllers, `SecurityContextHolder`, authentication details, persistence adapters, and concrete repositories. `@Service` and appropriate transaction boundaries in the application layer are not violations by themselves.

## 6. Source-of-truth hierarchy

Establish facts in this order:

1. tracked code, Flyway migrations, tests, configuration, and CI;
2. recent Git/PR history when needed;
3. `docs/00-overview/PROJECT_STATUS.md` — the only versioned human current state;
4. `docs/01-architecture/ARCHITECTURE.md` and architecture guards;
5. module, API, security, and operational documentation;
6. historical ADRs, plans, and roadmaps.

Do not treat ignored files, local reports, or old roadmaps as authoritative state. Record divergence and follow the current repository.

## 7. Local procedures

Backend procedures live in `.agents/skills/` and are auto-discovered by agent sessions started in this repository.

- `$caprigestor-gated-delivery`: mandatory operational procedure for reviewer gates, lifecycle transitions, Git discipline, and handoffs.
- `$caprigestor-architecture`: boundaries, ports, adapters, and domain isolation.
- `$caprigestor-safe-refactor`: structural change without behavioral regression.
- `$caprigestor-audit`: read-only audit and code-documentation comparison.
- `$caprigestor-documentation`: documentation classification and synchronization.

Read the applicable procedure before architecture work, refactoring, auditing, or documentation maintenance. Procedures guide process; they do not extend task authorization.

## 8. Context routing

- Current state and next wave: `docs/00-overview/PROJECT_STATUS.md`.
- Target architecture and known debt: `docs/01-architecture/ARCHITECTURE.md`.
- Active and planned gates: `docs/01-architecture/QUALITY_GATES.md`.
- Business rules: `docs/00-overview/BUSINESS_DOMAIN.md` and module docs.
- HTTP contract: `docs/03-api/API_CONTRACTS.md`.
- Authorization and access: `docs/02-modules/AUTHORITY_ACCESS_MODULE.md`.
- Module-specific rules: `docs/02-modules/` before changing a module.
- Documentation navigation: `docs/INDEX.md` (update only when official documentation navigation actually changes).

## 9. Security, data & database discipline

- Every farm-scoped route must keep an explicit policy and `farmId` isolation.
- Official roles: `ROLE_ADMIN`, `ROLE_FARM_OWNER`, `ROLE_OPERATOR`.
- Authorization goes through `FarmAuthorizationUseCase`; the authenticated principal goes through `CurrentPrincipalQueryUseCase`.
- `SecurityContextHolder` stays in the current-principal adapter.
- Never record secrets, tokens, credentials, or personal data in work logs or documentation.
- **Migration Discipline**: Check the migration directory (`src/main/resources/db/migration/`) and re-verify the highest migration version immediately before creating a new Flyway migration. If the expected version is occupied, STOP and report.
- Published Flyway migrations are immutable; never rewrite, renumber, or alter published migrations.

## 10. Goat identity, provenance & domain invariants

- **Structural Identity**: Technical `GoatId` represents structural identity. RG is a business/ABCC identifier and compatibility snapshot. Never allow conflicting non-null `GoatId` with matching RG to be treated as equivalent; conflicting `GoatId` must fail closed.
- **Ownership vs Provenance vs Visibility**:
  - Current ownership authority (`ownership` ledger) != historical recording provenance != read visibility.
  - `cabras.capril_id` is the current animal projection, not historical recording provenance.
  - Animal transfers update current ownership, never historical recording provenance.
- **Public Read vs Mutation Authority**: Evaluated independently. Former owners may retain authorized historical read visibility without retaining mutation authority. Current owners do not automatically acquire mutation authority over historical records authored by another farm.
- **Fail-Closed Ambiguity**: When historical identity, provenance, ownership, authorization, or privacy evidence is ambiguous: **FAIL CLOSED**. Never infer facts from mutable projections or missing records.

## 11. Tests and quality

Start with the smallest relevant test set and, when feasible, run the normal gate:

```powershell
.\mvnw.cmd -Dtest=GlobalHexagonalBoundaryArchUnitTest test
.\mvnw.cmd -Dtest=HexagonalArchitectureGuardTest test
.\mvnw.cmd -B clean verify
```

Do not remove, ignore, weaken, or rewrite tests merely to make them pass. Architecture allowlists and baselines must not be expanded without explicit architectural reviewer approval; they must NEVER be expanded merely to make architecture tests pass. Prefer shrinking and removing architectural debt whenever possible.

## 12. Documentation and language

Active documentation must reflect the current repository. Preserve ADRs, plans, and historical records; mark supersession when needed instead of rewriting the past. Do not create root Markdown files other than `README.md`.

Write all agent-facing instructions and Conventional Commits in English. Product documentation may use Portuguese when it serves its intended audience.

## 13. Escalation and stop conditions

Stop and report before schema/Flyway changes, REST contract changes, authorization semantics, Goat deletion semantics, persistent data operations, HML/production work, or `main` changes. For architectural ambiguity, Authority/security, schema, dangerous contracts, or difficult regressions, report `MODEL ESCALATION RECOMMENDED: <reason>`.
