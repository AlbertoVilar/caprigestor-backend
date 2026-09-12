---
name: caprigestor-documentation
description: Maintain CapriGestor backend documentation so active state, architecture, decisions, plans, and runbooks remain distinct and synchronized with the repository. Do not rewrite historical records as current fact.
---

# CapriGestor documentation

Use this procedure when a change alters facts represented by active documents or
when existing documentation must be classified.

## Types and responsibilities

- **CURRENT STATE**: concise human state of the current repository;
  `docs/00-overview/PROJECT_STATUS.md` is the sole versioned reference.
- **ARCHITECTURE REFERENCE**: target, boundaries, and current debt; use
  `docs/01-architecture/ARCHITECTURE.md`.
- **ADR**: historical decision and context; preserve the record.
- **DESIGN DOCUMENT**: proposal or study that may be superseded.
- **MIGRATION PLAN**: planned sequence, not execution proof.
- **ROADMAP**: future work, never current state.
- **HISTORICAL RECORD**: retained past evidence.
- **RUNBOOK**: reproducible environment or process operation.

## Flow

1. Confirm the fact in code, migrations, tests, and CI.
2. Update only the active document with the matching responsibility.
3. Preserve ADRs and historical plans; mark status or supersession when needed,
   rather than rewriting decisions as though they never existed.
4. Avoid parallel documents duplicating architecture, state, or roadmap.
5. Validate links and dates; do not introduce local paths, secrets, or personal
   data.

## Completion

An architecture change is incomplete until affected active state, architecture,
and quality-gate documents are synchronized. Update `docs/INDEX.md` only when
official navigation truly changes.
