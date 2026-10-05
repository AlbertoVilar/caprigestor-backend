---
name: caprigestor-safe-refactor
description: Refactor CapriGestor backend structure safely when code must move behind ports, adapters, or domain boundaries without changing observable behavior. Do not use to authorize API, schema, or business changes.
---

# CapriGestor safe refactor

Use this procedure for an approved structural refactor.
A structural refactor moves code behind ports, adapters, or domain boundaries without altering observable functional behavior, REST contracts, security policies, or database schemas.
For the complete commit, push, PR, CI, and merge lifecycle, refer to `$caprigestor-gated-delivery`.
For architectural boundary decisions, refer to `$caprigestor-architecture`.

## Scope Fingerprint & Change Containment

1. **Pre-Refactor Fingerprint**:
   - Record starting branch, HEAD commit SHA, and `git status --short`.
   - Run baseline tests relevant to the refactored area.
2. **Post-Refactor Scope Verification**:
   - Run `git status --short`, `git diff --name-only`, `git diff --stat`, and `git diff --check`.
   - **Unexpected File Stop Rule**: If any file outside the authorized refactoring scope is modified or created, **STOP IMMEDIATELY**. Do not silently include, delete, reset, restore, stash, format, or clean it up. Report it.
3. **No Opportunistic Cleanup**:
   - Do not perform sweeping formatting, reordering, or cosmetic edits in unrelated files.
   - Do not expand scope to "fix" adjacent issues without explicit authorization.

## Invariants to Preserve

- **Observable Behavior**: REST contracts, error codes, HTTP status codes, and JSON response shapes must remain unchanged.
- **Security & Multi-Tenancy**: Farm isolation, `farmId` checks, role permissions (`ROLE_ADMIN`, `ROLE_FARM_OWNER`, `ROLE_OPERATOR`), and token extraction must not be altered.
- **Runtime Semantics**: Preserve transactional boundaries (`@Transactional`), propagation, rollback behavior, Spring proxy contracts, and Jackson serialization semantics.
- **Architecture Guards**: Architecture allowlists and baselines must not be expanded without explicit architectural reviewer approval; they must NEVER be expanded merely to make architecture tests pass. Prefer shrinking and removing architectural debt whenever possible.
- **Domain Invariants**: Preserve technical `GoatId` structural identity, fail-closed ambiguity handling, and separation between ownership, recording provenance, and visibility.

## Validation & State-Bound Rigor

- **State-Bound Rule**: Validation belongs strictly to the exact source state that produced it. Any source modification after a test run invalidates that test result. Run the final validation suite after the last edit.
- **Validation-Level Truthfulness**: Report test execution truthfully (unit, mocked adapter, H2/JPA integration, PostgreSQL/Testcontainers integration). In-memory H2 tests (`src/test/resources/application-test.properties`) do not validate PostgreSQL dialect or Flyway migrations.

## Prohibited Actions & Stop Conditions

**STOP IMMEDIATELY and report** if the refactor requires:
- Flyway migrations or database schema changes;
- Modifications to database tables, columns, constraints, or persistent data;
- Public API contract changes or DTO property additions/removals;
- Changes to authorization rules or security policies;
- Silent Git lifecycle operations: rebase, stash manipulation, commit amend, reset, or force push.

## Completion

Inspect the raw diff with `git diff --check` and `git diff`. Update relevant active documentation if package paths or component responsibilities changed. Prepare the reviewer handoff per `$caprigestor-gated-delivery` and wait for reviewer authorization before creating a commit.
