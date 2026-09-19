---
name: caprigestor-audit
description: Audit CapriGestor code, migrations, tests, and documentation in read-only mode, classifying actual state and divergences. Do not use to implement discovered fixes in the same task.
---

# CapriGestor audit

Audits are **strictly read-only by default**. Code, migrations, tests, configuration, and CI outrank old documentation.
For lifecycle gates, Git safety, and reviewer handoff rules, refer to `$caprigestor-gated-delivery`.

## Strict Read-Only Boundaries

An audit MUST NOT:
- Stage files (`git add`), create commits, push, or open pull requests;
- Modify, delete, or create source code, configuration, or Flyway migrations;
- Execute destructive database commands or reset a database;
- Implement discovered fixes, refactorings, or migrations within the audit task.

If a raw diff or patch is needed for reviewer inspection, generate it directly from Git outside the repository (`git diff --no-ext-diff <args> > <path-outside-repo>`); never reconstruct diffs manually or with AI.

## Evidence Truthfulness & Rigor

- **Distinguish Source Truth from Inference**: Facts must be grounded in tracked code, executed migrations, verified tests, and observed CI. Do not treat assumptions as facts.
- **Evidence Contradiction Rule**: If observed repository state contradicts active documentation, commit history, or reported metrics, STOP and reconcile the discrepancy. Report the contradiction explicitly.
- **Projections vs Provenance**: Mutable entity dates and current owner projections (`cabras.capril_id`) do not constitute immutable historical provenance.
- **Fail-Closed on Ambiguity**: Never convert absence of evidence into positive provenance or authorization. Unresolved or unknown facts must be explicitly identified as UNKNOWN or UNRESOLVED.
- **Validation-Level Truthfulness**: Report test execution at its exact fidelity level (unit, mocked adapter, H2/JPA integration, PostgreSQL/Testcontainers integration, Flyway migration, or remote CI). Default test execution (`src/test/resources/application-test.properties`) uses in-memory H2 with `ddl-auto=create-drop` and Flyway disabled; H2 test runs never prove PostgreSQL constraints or Flyway schema integrity.

## Audit Method

1. **Capture Baseline Fingerprint**: Record repository, branch, HEAD SHA, `git status --short`, active Flyway migrations, and scope boundaries.
2. **Inspect Concrete Code & Tests**: Inspect tracked implementation, entities, ports, adapters, and tests before forming conclusions.
3. **Compare with Versioned Documentation**: Evaluate active documents against actual repository facts; treat roadmaps, ADRs, and plans as historical context, not implementation proof.
4. **Item Classification**:
   - Implementation: `DONE`, `PARTIAL`, `LEGACY`, `PENDING`, `NOT_NEEDED`.
   - Documentation: `CURRENT`, `NEEDS_UPDATE`, `HISTORICAL_KEEP`, `SUPERSEDED`, `OBSOLETE_CANDIDATE`.
   - State: explicitly mark each topic as `CURRENT STATE`, `TARGET`, or `KNOWN DEBT`.
5. **Recommend, Do Not Implement**: If the audit uncovers defects, debt, or blockers, formulate actionable recommendations for future gated waves. Do not execute them in the audit task.

## Output

Produce a structured report containing:
- Entry state and baseline fingerprint;
- Proven facts vs identified divergences;
- Test execution summary with exact validation levels;
- Identified blockers, risks, and recommended next wave (without implementation);
- Read-only confirmation: no files staged, committed, or altered.
