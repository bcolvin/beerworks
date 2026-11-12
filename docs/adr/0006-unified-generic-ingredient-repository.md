# 0006. Unified Generic Ingredient Repository with Lambda-Delegated DAO Queries

## Context
Across Features 002 (Hops), 003 (Malts), 006 (Honey), and 007 (Pomes), four separate repositories (`HopRepository`, `MaltRepository`, `HoneyRepository`, `PomeRepository`) were implemented. 

While each ingredient has specialized pomological, chemical, or kilning metrics, the architectural behavior across the repositories is identical:
- Sanitizing multi-word FTS match queries (`query.split("\\s+".toRegex()).joinToString(" ") { "$it*" }`).
- Mapping raw database entities to domain models on background threads via `flowOn(Dispatchers.Default)`.
- Resolving style cross-references across multiple guidelines (BJCP 2021, BA 2026, BJCP Mead, BJCP Cider).
- Duplicating identical `toDomainModel(isFavorite)` extension functions for `BjcpStyleEntity`, `BaStyleEntity`, `MeadStyleEntity`, and `CiderStyleEntity` across every repository file (~120 lines of duplicate mapping code per repository).

Attempting to make Room DAOs completely generic (e.g. `interface GenericDao<T>`) is problematic because Room's `@Query` and `@Fts4` annotations require static, compile-time verified SQL statements. Using raw queries (`SupportSQLiteQuery`) sacrifices Room's automatic reactive Flow change notifications and type safety.

## Decision
We decided to:
1. Define a shared `@Immutable interface Ingredient` modeling common identity, botanical, geographic, and substitute properties (`id`, `name`, `origin`, `countryCode`, `description`, `descriptors`, `substituteIds`, `countryFlag`).
2. Establish a generic contract `interface IngredientRepository<T : Ingredient, C : Any>` defining common catalog and style-linking operations.
3. Provide an abstract base implementation `abstract class BaseIngredientRepository<Entity : Any, Domain : Ingredient, Criteria : Any>` that centralizes:
   - FTS query term sanitization.
   - Background dispatcher offloading (`flowOn(Dispatchers.Default)`).
   - Multi-guideline style resolution and `BeerStyle` domain mapping in a single authoritative location.
4. Delegate entity-specific queries to concrete subclasses via function references/lambdas (`performSearch(criteria: Criteria): Flow<List<Entity>>`, `entityToDomain: (Entity) -> Domain`, and `getStyleIds(id: String, guidelineType: String): Flow<List<String>>`).
5. Retain `HopRepository`, `MaltRepository`, `HoneyRepository`, and `PomeRepository` as focused concrete subclasses to preserve full source compatibility with existing ViewModels, `MainActivity`, and test suites.

## Consequences
- Eliminates over 800 lines of duplicate boilerplate across the data layer.
- Establishes a true **deep module** at the repository seam: complex style mapping and reactive flow transformations are consolidated in one place.
- Concrete ingredient repositories become concise adapters (~30–50 lines) focused strictly on their specialized Room queries.
- New ingredient additions (e.g., Yeast, Water profiles, Wood/Oak varieties) require zero duplicated style-mapping or FTS-plumbing code.
