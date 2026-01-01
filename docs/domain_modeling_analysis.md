# Domain Modeling Analysis: User × Owner × Farm

This analysis evaluates the domain modeling of the CapriGestor application, specifically focusing on the relationship between system actors (Users) and their roles (Owners) in the context of Farms.

## 1. Domain Understanding

The application implements a model where **Owner** is a contextual role rather than a separate entity:

*   **User as System Actor:** The `User` entity (in the `authority` module) handles authentication, authorization, and global identity. It implements `UserDetails` to integrate with Spring Security.
*   **Owner as Contextual Role:** There is no `Owner` class. A User becomes an "Owner" when associated with a `GoatFarm` via a `@ManyToOne` relationship.
*   **Contextual Ownership:** Ownership logic is managed by the `OwnershipService`, which validates if the currently authenticated `User` is the one assigned to a specific `GoatFarm`.

## 2. Farm as Aggregate Root

The `GoatFarm` (Farm) serves as the aggregate root for most domain entities:

*   **Goat → Depends on Farm (YES):** Linked via `capril_id`.
*   **Address → Depends on Farm (NO):** Linked via `@OneToOne`, but exists as a standalone entity.
*   **Phone → Depends on Farm (YES):** Linked via `goat_farm_id`.
*   **Event → Depends on Farm (YES):** Linked to a `Goat`, which belongs to a `Farm`.

## 3. User as Actor vs. Owner as Role

*   **User Responsibilities:** Handles login credentials (`email`, `password`) and global roles (`ROLE_ADMIN`, etc.).
*   **Ownership Implementation:** Treated as a business relationship within the domain model. During Farm creation, the current `User` is assigned as the owner.
*   **Consistency:** Avoids duplication of personal data (CPF, name) by keeping it strictly within the `User` entity.

## 4. Creation Flow

1.  **Authenticated User:** The system identifies the actor from the `SecurityContext`.
2.  **Farm Creation:** During `GoatFarm` creation, the mapper/service associates the `currentUser` with the new `GoatFarm` entity.
3.  **Entity Dependency:** Once a Farm is established with an Owner, dependent entities like Goats and Phones can be created and linked to that Farm.

## 5. Architectural Consistency

| Criterion | Answer | Explanation |
| :--- | :--- | :--- |
| **Domain depends on Spring Security?** | **YES** | `User` entity imports Spring Security classes. |
| **User implements framework interfaces?** | **YES** | Implements `UserDetails`. |
| **Ownership treated in domain or security?** | **BOTH** | Relationship is in the model; logic is in a security service. |
| **Leaking of auth concepts to domain?** | **YES** | `UserDetails` implementation in the core entity is a leak. |

## 6. Repository Security & Organization

*   **Sensitive Files (.gitignore):**
    *   `.env` and `docker/.env` are correctly ignored.
    *   **WARNING:** `application.properties` and environment-specific properties are currently versioned.
    *   **WARNING:** Encryption keys (`app.key`, `app.pub`) are currently versioned in `src/main/resources`.

---

## Conclusion
The modeling is consistent with a practical approach to ownership, treating it as a relationship rather than a separate identity. While this reduces entity duplication, it introduces a direct dependency between the domain core and the authentication framework.
