> ⚠️ DOCUMENTO HISTÓRICO
> Este arquivo reflete decisões ou análises de fases anteriores do projeto.
> **Não representa o estado atual da arquitetura ou do código.**

# ADR-001: Atomic GoatFarm Registration Flow

## Status
Accepted

## Context
The system allows farm registration (`GoatFarm`) by both authenticated users and new users (anonymous). Previously, the creation of related entities (User, Address, Phones) could occur in a fragmented or non-transactional manner, leading to data inconsistency and potential security flaws (such as *Privilege Escalation* or *IDOR* - Insecure Direct Object References).

The business requirement defines that `GoatFarm` is the **Aggregate Root** and its existence is intrinsically linked to a `User` (owner), an `Address`, and `Phones`.

## Decision
We decided to implement a **single public atomic creation method** (`createGoatFarm`) that orchestrates the entire process within a single `@Transactional` transaction.

### Implementation Details

1.  **Single Entry Point:** Only the `POST /api/goatfarms` endpoint (mapped to `GoatFarmBusiness.createGoatFarm`) is responsible for creation. There are no public endpoints to create addresses or users in isolation within the farm registration context.
2.  **Accumulated Validation:** The system validates the presence of all mandatory components (`Farm`, `Address`, `Phones`, and `User` conditionally) before starting any persistence, returning all errors at once.
3.  **Owner Resolution Strategy:**
    *   **Authenticated:** The `owner` is resolved via `OwnershipService.getCurrentUser()`. Any user data sent in the request body is **ignored**.
    *   **Anonymous:** The system requires user data.
        *   Creates a new user.
        *   Forces the role `ROLE_USER`.
        *   Explicitly rejects privilege fields (`roles`, `admin`, etc).
        *   If the email already exists, throws `DuplicateEntityException` with a generic message to avoid enumeration.
4.  **Architectural Layer:** The atomic creation flow is implemented in the **Application/Business Layer**, and is not a direct responsibility of JPA entities.
5.  **Transaction & Rollback:** Any failure during the creation of User, Farm, Address, or Phones results in a **complete transaction rollback**.
6.  **Test Coverage:** This decision is protected by unit tests (`GoatFarmBusinessTest`), ensuring that the contract is not violated in future refactorings.

## Consequences

### Positive
*   **Consistency:** Guarantees that there will be no "orphan farms" or "farm-less users" created halfway due to failures in the middle of the process.
*   **Security:** Eliminates common attack vectors in multi-part registrations. The backend retains full control over assigned permissions.
*   **Maintainability:** Logic centralized in a single business method, facilitating testing and auditing.

### Negative/Trade-offs
*   **Payload Complexity:** The frontend needs to send a nested and complete JSON (`GoatFarmFullRequestDTO`), which can be more complex to assemble than simple sequential calls.
*   **Coupling:** User creation is coupled to farm creation in this specific flow, which is acceptable given the domain, but requires care if we want to allow users without farms in the future.

## Alternatives Considered

### Step-by-Step Creation (Wizard)
Allow creating User -> get ID -> create Farm with ID.
*   **Rejected due to:** Risk of creating "zombie" users if the user gives up at step 2. Security risk when allowing association via ID (IDOR).

### Separate User Registration Endpoint
*   **Rejected due to:** The domain requires that, in the `GoatFarm` context, the user is born with the farm (or already exists). Separating registration would complicate the atomic transaction.
