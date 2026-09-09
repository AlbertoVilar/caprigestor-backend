# ADR-001 - Atomic GoatFarm registration

**Status:** Accepted (historical decision; current endpoint facts retained)

**Date:** 2026-09-09

**Scope:** Canonical record of the decision to create a farm and its initial owner atomically.

**Related documents:** [Documentation portal](../../INDEX.md), [Architecture](../ARCHITECTURE.md), [Portuguese bridge](./ADR-001-atomic-goatfarm-registration-pt-br.md), [archived English record](../../_archive/2026-02/history/adr/ADR-001-atomic-goatfarm-registration.md), [archived Portuguese record](../../_archive/2026-02/history/adr/ADR-001-atomic-goatfarm-registration-pt-br.md)

## Context

Farm registration creates the farm aggregate and the authenticated account that owns it. A partial registration would leave unusable data or require a manual repair. The decision recorded here is to keep the operation atomic at the application boundary.

## Decision

- `GoatFarmBusiness.createGoatFarm` is the application entry point for the atomic registration flow.
- The current public route is `POST /api/v1/goatfarms`.
- The backend resolves the authenticated owner from the security context instead of accepting an arbitrary owner identifier from the client.
- The current registration flow creates the default `ROLE_OPERATOR` authority for the newly created account, as documented by the current Authority module and implemented code. It must not be confused with the historical role names preserved in archived copies of this ADR.
- Farm and owner creation execute in one transaction. A failure rolls back the complete operation.
- Privilege-bearing fields are not client-controlled during registration.

## Consequences

Positive consequences:

- no farm is committed without its initial account;
- the ownership invariant is established in one transaction;
- retries and failures are observable as one application operation;
- tests can verify the complete registration boundary.

Trade-offs:

- the use case coordinates more than one persistence aggregate;
- a future change to onboarding or role bootstrap must preserve the transaction and ownership invariants;
- external side effects must remain outside the transaction or be made idempotent.

## Alternatives considered

- Creating the farm first and the owner later would expose partial state.
- Letting the client submit `ownerId` would permit ownership confusion and is not compatible with the security model.
- Splitting the flow into asynchronous commands would require an explicit saga/compensation design that is not present in the current system.

## Verification evidence

The current implementation and tests are the source of truth for route and role behavior. The module and API documents link to the corresponding controllers, business service, and tests. The historical archived copies are retained for forensic context only; their old `/api` examples and older role terminology are not current contracts.

## Maintenance rule

Update this ADR only when the registration decision or its invariants change. Routine endpoint details belong in [API_CONTRACTS](../../03-api/API_CONTRACTS.md) and the [Goat/Farm module](../../02-modules/GOAT_FARM_MODULE.md).
