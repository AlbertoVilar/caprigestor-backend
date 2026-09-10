# ID4-B2 — Goat Hexagonal Core

**Status:** IMPLEMENTED (first bounded core)

This wave establishes the first usable hexagonal boundary for the Goat module
without resetting the development database or rewriting published Flyway
migrations.

## Implemented boundary

```text
Goat domain aggregate
        ↓
GoatPersistencePort (application contract)
        ↓
GoatPersistenceAdapter
        ↓
GoatEntity / GoatRepository (transitional JPA)
```

`Goat`, `GoatId` and `RegistrationIdentity` are framework-free domain types.
`GoatId` represents the technical identity introduced by V39; `registrationNumber`
remains the human/ABCC registration identity during the transition. Local
parents are represented by technical id plus display data, while external ABCC
parents remain registration references.

The clean persistence port exposes only domain objects and application-owned
records (`GoatPage`, `GoatPageQuery`, `GoatHerdSnapshot` and
`GoatBreedCount`). Spring `Page`, `Pageable`, JPA entities and repository
projections stay inside the adapter. A dedicated ArchUnit test protects the
domain and clean port from infrastructure dependencies.

## Transitional compatibility

The existing modules (events, genealogy, health, reproduction and other
consumers) still use `LegacyGoatPersistencePort` and `GoatEntity`. They were
kept operational so this PR does not combine every module migration into one
change. `GoatPersistenceAdapter` implements both contracts and maps between
the clean aggregate and the transitional entity.

The public use-case signatures and current RG-based URLs are preserved. The
application service resolves a numeric path value as `GoatId` when available
and falls back to the RG compatibility lookup. No API or frontend route is
silently reinterpreted in this wave.

## Integrity decisions

- V39/V40 remain the source of the technical id and shadow parent references;
  no published migration was edited or squashed.
- Legacy JPA self-references remain populated for compatibility, while the
  adapter also writes the technical parent shadow when a local parent has an
  id.
- The current development database was not reset. Upgrade/backfill and later
  module waves remain required before a persistent HML/production database can
  rely exclusively on technical references.
- The existing business rules for farm ownership, genealogy validation,
  controlled exit and operational audit are preserved in the migrated
  application service.

## Next gates

1. Add module-specific technical references and migrate consumers in small
   waves (events, genealogy, reproduction, health, milk and commercial).
2. Add API/frontend dual identity payloads and versioned technical-id routes.
3. Exercise the adapter against a clean PostgreSQL V1→latest installation and
   an upgrade fixture before retiring the legacy port or RG structural columns.
