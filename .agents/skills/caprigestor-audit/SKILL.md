---
name: caprigestor-audit
description: Audit CapriGestor code, migrations, tests, and documentation in read-only mode, classifying actual state and divergences. Do not use to implement discovered fixes in the same task.
---

# CapriGestor audit

Audits are **read-only by default**. Code, migrations, tests, configuration,
and CI outrank old documentation. Do not implement a finding in the same task
without new, explicit authorization.

## Method

1. Record entry state: repository, branch, HEAD, `git status`, migrations, and
   scope boundaries.
2. Gather code, test, and configuration evidence before reaching a conclusion.
3. Compare active documents with the current repository; treat roadmaps and
   plans as historical evidence, never as implementation proof.
4. For each item, report evidence, impact, risk, classification, and the wave
   responsible for a possible correction.
5. Separate structural work from schema, API, data, security, or behavioral
   changes. Do not mix waves.

## Preferred classifications

- Implementation: `DONE`, `PARTIAL`, `LEGACY`, `PENDING`, `NOT_NEEDED`.
- Documentation: `CURRENT`, `NEEDS_UPDATE`, `HISTORICAL_KEEP`, `SUPERSEDED`,
  `OBSOLETE_CANDIDATE`.

## Output

Finish with proven facts, divergences, deferred items, executed tests, and a
`GO` or `NO-GO` limited to the requested next step. Do not create, edit, move,
delete, stage, commit, merge, reset, deploy, or touch a database during a
read-only audit.
