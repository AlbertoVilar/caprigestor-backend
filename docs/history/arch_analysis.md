# Architectural Analysis: GoatFarm (CapriGestor)

This document provides a faithful description of the current project structure, following the principles of Hexagonal Architecture / Clean Architecture as implemented in the codebase.

## 1. Package Structure (src/main/java)

The project is organized into functional modules under `com.devmaster.goatfarm`.

```text
com.devmaster.goatfarm
├── address
│   ├── api (controller, dto)
│   ├── business (bo)
│   ├── mapper
│   └── model (entity, repository)
├── application
│   ├── core (business)
│   └── ports
│       ├── in (UseCases)
│       └── out (PersistencePorts)
├── authority
│   ├── api (controller, dto, projection)
│   ├── business (bo, usersbusiness)
│   ├── mapper
│   └── model (entity, repository)
├── config
│   ├── exceptions (custom)
│   └── security
├── events
│   ├── api (controller, dto)
│   ├── business (bo)
│   ├── mapping
│   └── messaging (config, consumer, publisher)
├── farm
│   ├── api (controller, dto)
│   ├── business (bo)
│   ├── mapper
│   └── model (entity)
├── genealogy
│   └── business
├── goat
│   ├── api (controller, dto)
│   ├── business (bo, goatbusiness)
│   ├── mapper
│   └── model (entity, repository)
├── infrastructure
│   ├── adapters (out.persistence)
│   └── config
└── phone
    └── ...
```

---

## 2. Modules and Responsibilities

### Module: `goat`
*   **Responsability:** Management of goats (registration, updates, queries), including zootechnical attributes.
*   **Packages:**
    *   `api`: REST controllers and DTOs.
    *   `business.bo`: Business Objects (VOs) for internal logic.
    *   `business.goatbusiness`: Implementation of business rules and use cases.
    *   `model.entity`: JPA entities (`Goat`).
    *   `model.repository`: Spring Data JPA interfaces.
    *   `mapper`: Conversions between DTO ↔ VO ↔ Entity.

### Module: `farm`
*   **Responsability:** Management of farm properties (capris) and their relationships with users and addresses.
*   **Packages:** Follows the same pattern as `goat` (`api`, `business`, `model`).

### Module: `authority`
*   **Responsability:** Authentication, authorization, user management, and roles (Spring Security integration).
*   **Packages:** `api`, `business`, `model` (contains `User` and `Role`).

---

## 3. Ports & Adapters

### Input Ports (Use Cases)
*   **Interface:** `GoatManagementUseCase` (package: `com.devmaster.goatfarm.application.ports.in`)
*   **Implementation:** `GoatBusiness` (package: `com.devmaster.goatfarm.goat.business.goatbusiness`)

### Output Ports (Repositories/External Services)
*   **Interface:** `GoatPersistencePort` (package: `com.devmaster.goatfarm.application.ports.out`)
*   **Adapter:** `GoatPersistenceAdapter` (package: `com.devmaster.goatfarm.infrastructure.adapters.out.persistence`)
    *   *Implementation:* Uses `GoatRepository` (Spring Data JPA).

---

## 4. Data Flow (Request → Domain → Response)

Example: **Creation of a Goat**

1.  **Controller:** `GoatController.createGoat` receives `GoatRequestDTO`.
2.  **Mapper (Request):** `GoatMapper.toRequestVO` converts DTO to `GoatRequestVO`.
3.  **Use Case:** Controller calls `GoatManagementUseCase.createGoat(farmId, requestVO)`.
4.  **Service/Business:** `GoatBusiness` handles logic (ownership verification, parent mapping).
5.  **Mapper (Domain):** `GoatMapper.toEntity` converts VO to `Goat` (Entity).
6.  **Output Port:** `GoatPersistencePort.save(goat)` is called.
7.  **Adapter:** `GoatPersistenceAdapter` calls `GoatRepository.save(goat)`.
8.  **Mapping Back:** The saved entity is mapped to `GoatResponseVO` (Service level) and then to `GoatResponseDTO` (Controller level) for the final response.

---

## 5. Tests (Architecture)

Tests are located in `src/test/java`.

*   **Business Tests:** `GoatBusinessTest`
    *   Uses `MockitoExtension`.
    *   Mocks `GoatPersistencePort` and other dependencies.
    *   Does **not** use `@SpringBootTest` (Unit test focus).
*   **Controller Tests:** `GoatControllerTest` or `AuthControllerIntegrationTest`.
    *   Some use `@WebMvcTest` or `@SpringBootTest` for integration validation.

---

## 6. Architectural Dependencies

| Criterion | Answer | Explanation |
| :--- | :--- | :--- |
| **Domain imports Spring?** | **YES** | `User` entity implements `UserDetails` from Spring Security. |
| **DTOs outside domain?** | **YES** | DTOs are in the `api.dto` package of each module. |
| **Controllers depend on interfaces?** | **YES** | They inject `UseCase` interfaces from `application.ports.in`. |
| **Mappers know domain?** | **YES** | They perform mapping between VOs/DTOs and Entities. |
| **Entity returned by API?** | **NO** | All endpoints return DTOs. |

---

## 7. Important Observations

*   **MapStruct:** Heavily used for all layer conversions.
*   **JPA in Entities:** Entities contain persistence annotations (`@Entity`, `@Table`, etc.).
*   **Spring Security:** Integrated directly into the `authority` module and `User` entity.
*   **Cross-cutting Concerns:** `OwnershipService` handles resource access validation across modules.
*   **Messaging:** Uses RabbitMQ for event-driven logic in the `events` module.
