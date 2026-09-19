---
name: caprigestor-documentation
description: Maintain CapriGestor backend documentation so active state, architecture, decisions, plans, and runbooks remain distinct and synchronized with the repository. Do not rewrite historical records as current fact.
---

# CapriGestor documentation

Use this procedure when a change alters facts represented by active documents or when existing documentation must be classified or maintained.
For commit, PR, and lifecycle gates, refer to `$caprigestor-gated-delivery`.

## Truthfulness & Authority Invariants

- **Fact Hierarchy**: Tracked code, Flyway migrations, verified tests, and CI outrank documentation for factual state. Documentation must reflect reality, never the reverse.
- **No Predictive Status**: Never document planned, intended, or in-progress changes as completed state.
- **No Fabricated Evidence**: Never invent or estimate commit SHAs, PR numbers, Flyway migration numbers, execution dates, or CI results.
- **Historical Integrity**: Never rewrite historical ADRs, architecture decisions, or migration plans to make history look cleaner. Mark past records as `SUPERSEDED` or `HISTORICAL_KEEP` while preserving original context.
- **Current State Updates**: Versioned current-state documentation (`docs/00-overview/PROJECT_STATUS.md`) is updated only after changes are actually implemented, validated, and approved or merged.
- **Local Artifacts vs Canonical Docs**: Generated local reports and scratch logs are ephemeral; they do not represent canonical documentation unless explicitly approved and committed into `docs/`.
- **Sensitive Data Prohibition**: Never include passwords, tokens, API keys, credentials, machine-specific paths, or personal user data in documentation.

## Types and Responsibilities

- **CURRENT STATE**: Concise human-readable status of the repository; `docs/00-overview/PROJECT_STATUS.md` is the sole versioned reference.
- **ARCHITECTURE REFERENCE**: Target architecture, boundaries, and active debt; maintained in `docs/01-architecture/ARCHITECTURE.md`.
- **QUALITY GATES**: Active and planned quality criteria; maintained in `docs/01-architecture/QUALITY_GATES.md`.
- **ADR (Architecture Decision Record)**: Permanent record of an architectural choice; retained as immutable historical evidence.
- **DESIGN DOCUMENT**: Deep-dive technical specification or study; subject to supersession.
- **MIGRATION PLAN**: Planned sequence of database or code transitions, not proof of execution.
- **ROADMAP**: Future horizons, strictly non-authoritative for current behavior.
- **HISTORICAL RECORD**: Retained past evidence, never altered to reflect modern state.
- **RUNBOOK**: Reproducible operational steps for environment setup or diagnostics.

## Maintenance Protocol

1. **Verify Reality First**: Ground every proposed documentation change in verified source code, migrations, or test execution.
2. **Targeted Updates**: Update only the active document directly responsible for the changed fact. Avoid redundant parallel documentation.
3. **Link & Navigation Integrity**: Ensure relative Markdown links resolve correctly. Update `docs/INDEX.md` only when official documentation navigation actually changes.
4. **Language**: Agent-facing governance and Conventional Commits are in English. Domain and product documentation may use Portuguese to serve project stakeholders.
