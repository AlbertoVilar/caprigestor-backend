---
name: caprigestor-gated-delivery
description: Operate CapriGestor changes through reviewer-controlled implementation, validation, commit, PR, CI, merge, and post-merge gates. Use for any task that can modify tracked repository state. Does not grant authorization for any lifecycle action.
---

# CapriGestor gated delivery

This procedure governs any task that modifies or has the potential to modify tracked repository state.
Repository-changing lifecycle actions are controlled by explicit reviewer authorization. Verification phases remain mandatory but do not require an artificial gate merely to observe or verify state.

## 1. Reviewer / Executor Contract

- **Role of the Agent**: The agent is an EXECUTOR.
- **Approval Authority**: The CapriGestor REVIEWER / GATEKEEPER is the sole approval authority.
- **No Self-Approval**: The executor must NEVER self-approve any lifecycle action. Passing tests, clean diffs, and green CI are evidence for the reviewer, NOT authorization to proceed to mutations.
- **Reviewer Gate Vocabulary**:
  - `PASS` — Explicitly authorizes the next specific lifecycle action.
  - `CORRECTION` — Requires specific adjustments before re-evaluating the current step.
  - `BLOCKER` — Execution halts immediately; no further changes permitted without direction.
- **Strict Authorization Boundaries**: Explicit reviewer authorization is required before lifecycle actions such as:
  - implementation when not already authorized (`PASS — IMPLEMENTATION AUTHORIZED`);
  - local commit (`PASS — LOCAL COMMIT AUTHORIZED`);
  - push + PR (`PASS — PUSH + PR AUTHORIZED`);
  - merge (`PASS — MERGE AUTHORIZED`);
  - deployment or other separately gated mutations.
  A gate authorizes ONLY the explicitly stated action (e.g. implementation authorization does NOT authorize commit).
- **Verification Phases**: Verification steps (such as remote CI observation or post-merge verification explicitly included in the merge authorization) do not require an artificial extra gate merely to observe or verify state.
- **Formal Closure & Next Wave**: FORMAL CLOSURE remains a reviewer decision. Beginning the NEXT WAVE always requires separate explicit authorization.

## 2. Canonical Delivery Lifecycle

```
IMPLEMENTATION
  -> REVIEWER GATE
  -> LOCAL COMMIT
  -> REVIEWER GATE
  -> PUSH + PR
  -> REMOTE CI
  -> REVIEWER GATE
  -> MERGE
  -> POST-MERGE VERIFICATION
  -> FORMAL CLOSURE
  -> NEXT WAVE
```

## 3. Scope Fingerprint & Working Tree Safety

### Pre-Implementation Fingerprint
Before editing files, capture and record:
- Current branch: `git branch --show-current`
- HEAD commit: `git rev-parse HEAD`
- Remote tracking: `git rev-parse origin/<integration-branch>`
- Working tree state: `git status --short`
- Explicitly record approved base SHA, target branch, expected files/modules, and out-of-scope boundaries.

### Remote Base Freshness Rule
Before claiming that a local integration branch matches current remote state:
- Run `git fetch origin` to synchronize remote tracking references.
- Compare the local HEAD against the verified remote ref (`origin/<integration-branch>`).
- If a remote fetch cannot be performed: report remote freshness explicitly as **UNVERIFIED**.
- Never claim the remote integration branch is current merely because a stale local remote-tracking ref exists. Keep this rule generic to any integration branch.

### Unrelated Work Stop Rule (General Safety Rule)
If an agent detects pre-existing unrelated local modifications or untracked files outside the authorized scope:
- **MUST STOP IMMEDIATELY** and report the working tree state to the reviewer.
- **MUST NOT** move, stash, reset, restore, clean, checkout, or silently incorporate unrelated changes into the task branch.

### Post-Implementation Scope Verification
After implementation and validation:
- Run `git status --short`, `git diff --name-only`, `git diff --stat`, and `git diff --check`.
- Compare changed files against the authorized file set.
- If any unexpected file appears: **STOP IMMEDIATELY**. Do not silently include, delete, reset, restore, stash, format, or clean it up. Report it in the handoff.

### Stash Safety & Preservation
- Pre-existing stashes may contain user work.
- **NEVER** apply, pop, drop, clear, rewrite, or reorder an existing stash without explicit reviewer authorization.
- **DO NOT** create a stash merely as an automatic convenience.
- If creating a stash is needed to preserve or move existing work, **obtain explicit reviewer authorization first**.
- Never silently move, stash, or clean unrelated work.

## 4. Evidence Integrity

- **Truthful Evidence**: Never claim a test passed, command succeeded, SHA was verified, or CI was observed if it was not actually executed and observed.
- **No Fabricated Output**: Never fabricate terminal output, execution logs, or test reports.
- **Raw Git Artifacts**: Never reconstruct patch or diff text manually or with AI and present it as Git output. When a patch is requested for review, export it directly from Git outside the repository:
  ```powershell
  git diff --no-ext-diff <args> > <path-outside-repository>
  ```
- **Evidence Contradiction Stop Rule**: If reported evidence contradicts repository source, enum definitions, Flyway migrations, Git history, test output, or schema:
  - **STOP IMMEDIATELY**.
  - Do not silently "fix" the contradiction.
  - Reconcile working tree vs reported evidence vs executed validation and report the discrepancy to the reviewer.

## 5. State-Bound Validation & Validation Levels

### State-Bound Validation Rule
- Validation results belong strictly to the exact source state that produced them.
- Any source modification after validation invalidates previous validation claims for the modified scope.
- After the LAST source change, re-run all required validation before requesting review.
- Distinguish and report explicitly:
  - Pre-change baseline tests;
  - Focused implementation tests;
  - Final clean verify (`mvnw clean verify`);
  - Remote CI results.

### Validation Level Truthfulness (H2 vs PostgreSQL/Flyway)
- State the exact validation level: unit test, mocked adapter test, H2/JPA integration test, PostgreSQL/Testcontainers test, Flyway migration test, or remote GitHub CI.
- **CapriGestor Fact**: Default test execution (`src/test/resources/application-test.properties`) uses in-memory H2 with `ddl-auto=create-drop` and Flyway disabled.
- An H2 test does NOT validate PostgreSQL syntax, schema constraints, or Flyway migrations.
- Never claim "PostgreSQL constraint verified" unless a real PostgreSQL/Testcontainers or staging database execution was actually performed.

## 6. Database & Flyway Discipline

Before creating or modifying any database migration:
1. Verify the approved base SHA and pull the latest integration branch as authorized.
2. Inspect the migration directory (`src/main/resources/db/migration/`) directly.
3. Identify the current highest migration number.
4. Detect whether another branch or commit occupied the expected version number.
5. If the expected version is already occupied: **STOP IMMEDIATELY** and report the collision.
6. Never reuse, alter, or renumber published Flyway migrations.
7. Never infer persistent data provenance or perform destructive data mutations without explicit reviewer authorization.

## 7. Lifecycle Gate Procedures

### A. Pre-Commit & Commit Gate
1. Complete all authorized implementation and state-bound validation.
2. Verify `git diff --check` passes with zero whitespace issues.
3. Automatically generate the reviewer handoff and STOP.
4. Only upon receiving `PASS — LOCAL COMMIT AUTHORIZED`:
   - Stage strictly the authorized files.
   - Commit with the approved commit message.
   - Run `git status --short`, `git log -1 --oneline`, `git show --stat --oneline HEAD`.
   - Report commit SHA and status, then STOP.

### B. Push & PR Gate
1. Only upon receiving `PASS — PUSH + PR AUTHORIZED`:
   - Confirm local HEAD matches the approved commit SHA.
   - Push to origin without force push (`git push -u origin <branch>`). Never force push unless explicitly directed.
   - Open PR against the approved target branch (e.g. `develop`).
   - Report PR number, URL, base, head branch, and HEAD SHA, then STOP.

### C. Remote CI Observation
1. Green local validation does NOT replace remote CI.
2. Observe remote GitHub Actions CI for the exact approved HEAD SHA.
3. Report check status and wait for reviewer evaluation. Observation of CI does not require an extra gate.

### D. Merge Gate
1. Only upon receiving `PASS — MERGE AUTHORIZED`:
   - Verify PR is open, mergeable, checks are green, and approved HEAD SHA has not shifted.
   - Execute only the authorized merge method (e.g. standard merge / PR merge).
   - Capture merge commit SHA, target branch, and timestamp.

### E. Post-Merge Verification & Formal Closure
1. Checkout the target integration branch (e.g. `develop`).
2. Update remote state: `git fetch origin`. If fetch cannot be performed, report remote freshness as UNVERIFIED.
3. Synchronize: `git pull --ff-only origin <integration-branch>`.
4. Verify local HEAD matches `origin/<integration-branch>` and working tree is clean.
5. Request FORMAL CLOSURE from the reviewer.
6. The executor must NEVER declare a wave or task closed by itself.

### F. Next-Wave Isolation
- Closing a task or wave NEVER authorizes starting the next wave.
- Never start subsequent waves without explicit reviewer authorization.

## 8. Automatic Reviewer Handoff Protocol

When the executor completes the authorized scope of work:
1. Complete all state-bound validation;
2. Inspect the final diff and status;
3. Immediately prepare and output the structured REVIEWER HANDOFF;
4. **STOP and wait for the reviewer gate**.
- Do NOT wait for the user to ask "finished?" or "report status".
- Do NOT prompt the user with routine questions ("Should I commit?", "Should I push?", "Should I open PR?", "Would you like me to continue?").
- Produce the handoff autonomously and wait.
