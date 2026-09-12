---
name: caprigestor-safe-refactor
description: Refactor CapriGestor backend structure safely when code must move behind ports, adapters, or domain boundaries without changing observable behavior. Do not use to authorize API, schema, or business changes.
---

# CapriGestor safe refactor

Use this procedure for an approved structural refactor. It does not authorize
functional, API, schema, data, or infrastructure changes.

## Required flow

1. Establish the baseline: branch, base, `git status`, tests, and current behavior.
2. Inspect implementation and identify business, security, persistence,
   transaction, and serialization invariants.
3. Map relevant consumers, contracts, and compatibility layers.
4. Make the smallest coherent change for the intended boundary.
5. Compile and run focused tests, then architecture guards and risk-proportionate
   regressions.
6. Inspect `git diff`, update active documentation, and prepare a PR only when
   authorized.

## Prohibited changes

Do not make giant rewrites, unrelated opportunistic cleanup, hidden functional
or API changes, silent compatibility removal, schema changes for convenience,
historic migration rewrites, or changes to unrelated modules merely for
stylistic consistency.

## Stop conditions

Stop and report if the smallest refactor requires Flyway/schema, persistent
data, identity changes, REST changes, authorization redefinition, or a larger
architecture baseline/allowlist. Do not weaken tests to make a refactor pass.

Use `caprigestor-architecture` for boundary decisions and
`caprigestor-documentation` to synchronize active documents.
