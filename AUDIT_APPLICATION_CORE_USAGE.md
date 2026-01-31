# Audit Report: Application Core Usage

**Date:** 2026-01-31
**Scope:** `src/main/java/com/devmaster/goatfarm/application/**`

## Executive Summary
An audit was performed on the shared application core package to identify unused or misplaced classes. The package contains 2 classes, both of which are actively used across multiple modules, justifying their placement in the shared core. No dead code was found.

## Inventory & Usage Analysis

| Class | Status | Usage Count (Files) | Modules Using It | Observation |
| :--- | :--- | :---: | :--- | :--- |
| `EntityFinder` | **KEEP** | 9 | `farm`, `phone`, `goat`, `address` | Standardizes entity retrieval and exception handling (`findOrThrow`). Critical for consistency across CRUD services. |
| `GoatGenderValidator` | **KEEP** | 7 | `reproduction`, `milk` (lactation/production) | Centralizes "require female" logic. Shared by both reproduction and milk domains, preventing logic duplication. |

## Detailed Findings

### 1. `EntityFinder.java`
*   **Location:** `application.core.business.common`
*   **Usage Evidence:**
    *   `GoatFarmBusiness.java`
    *   `PhoneBusiness.java`
    *   `GoatBusiness.java`
    *   `AddressBusiness.java`
    *   Associated Test classes.
*   **Architectural Check:**
    *   Dependencies: `java.util.*`, `Spring Component`, `ResourceNotFoundException`.
    *   Violation: None. Purely infrastructure-agnostic business utility.
*   **Recommendation:** **KEEP**. It is a reusable utility effectively decoupling business services from repetitive `Optional` handling logic.

### 2. `GoatGenderValidator.java`
*   **Location:** `application.core.business.validation`
*   **Usage Evidence:**
    *   `ReproductionBusiness.java` (Reproduction domain)
    *   `MilkProductionBusiness.java` (Milk domain)
    *   `LactationBusiness.java` (Milk domain)
    *   Associated Test classes.
*   **Architectural Check:**
    *   Dependencies: `GoatPersistencePort` (Port), `Goat` (Entity), `Gender` (Enum), `Exceptions`.
    *   Violation: None. Depends correctly on Ports and Domain objects.
*   **Recommendation:** **KEEP**. It serves as a shared domain service ensuring consistency (only females can have milk/reproduction events) across distinct bounded contexts (Milk vs Reproduction).

## Conclusion
The `com.devmaster.goatfarm.application` package is lean and contains only genuinely shared, high-value components. No "overengineering" detected in the current state. No removals or moves are necessary.
