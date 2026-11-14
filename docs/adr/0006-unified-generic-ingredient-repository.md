# 0006. Unified Generic Ingredient Architecture, Catalog Aggregation Seam, and Modular Domain Seeders

## Context
Across Features 002 (Hops), 003 (Malts), 006 (Honey), 007 (Pomes), 008 (Yeast), and 009 (Favorites), five ingredient disciplines and four beverage style guidelines (BJCP 2021, BA 2026, BJCP Mead, BJCP Cider) were introduced.

While each ingredient has specialized pomological, chemical, kilning, or fermentation metrics, their architectural behavior across the persistence, repository, and presentation layers is identical:
- Sanitizing multi-word FTS match queries (`query.split("\\s+".toRegex()).joinToString(" ") { "$it*" }`).
- Mapping raw database entities to domain models on background threads via `flowOn(Dispatchers.Default)`.
- Resolving style cross-references across all four guidelines with live `Favorite` state (`FavoriteStyleDao`) and canonical ordering.
- Mapping `BjcpStyleEntity`, `BaStyleEntity`, `MeadStyleEntity`, and `CiderStyleEntity` to `BeerStyle`.
- Managing detail screen state (`item`, `isFavorite`, `substitutes`, `commonStyles`, and optional `relatedItems` such as child hop cultivars) and toggling favorites.
- Aggregating cross-discipline ingredients for `StyleDetailViewModel` and `FavoritesViewModel`.
- Seeding Room tables from bundled JSON assets (`bjcp_styles.json`, `ba_styles.json`, `mead_styles.json`, `cider_styles.json`, `hops.json`, `malts.json`, `honeys.json`, `pomes.json`, `yeasts.json`).

Attempting to make Room DAOs completely generic (e.g. `interface GenericDao<T>`) is problematic because Room's `@Query` and `@Fts4` annotations require static, compile-time verified SQL statements. Using raw queries (`SupportSQLiteQuery`) sacrifices Room's automatic reactive Flow change notifications and type safety.

## Decision
We decided to establish deep modules across four architectural seams:

1. **Polymorphic Domain & Entity Contracts (`Ingredient` & `StyleEntity`)**:
   - Define `@Immutable interface Ingredient` modeling common catalog identity, geographic provenance, and search behavior (`id`, `name`, `origin`, `countryCode`, `description`, `descriptors`, `substituteIds`, default `countryFlag`, and polymorphic `matchesBaseQuery(query)` / `matchesQuery(query)`).
   - Define `interface StyleEntity` implemented by `BjcpStyleEntity`, `BaStyleEntity`, `MeadStyleEntity`, and `CiderStyleEntity` with a single default `toDomainModel(isFavorite: Boolean): BeerStyle` implementation, eliminating duplicate style-mapping functions across `BeerStyleRepository` and `BaseIngredientRepository`.

2. **Deep Generic Repository Seam (`IngredientRepository` & `BaseIngredientRepository`)**:
   - Establish `interface IngredientRepository<T : Ingredient, C : Any>` and `abstract class BaseIngredientRepository<Entity : Any, Domain : Ingredient, Criteria : Any>` centralizing:
     - FTS query term sanitization and `.flowOn(Dispatchers.Default)` background execution.
     - Substitute resolution (`getSubstitutesForItem`) and multi-guideline style resolution (`getCommonStylesForItem`) combined with live `FavoriteStyleDao` state and sorted by `(guidelineType.ordinal, categoryNumber, number)`.
     - Reverse style-to-ingredient lookup (`getItemsForStyle(styleId, guidelineType)`).
     - Ingredient favorite state (`isFavorite`, `getFavoriteItems`, `toggleFavorite`) backed by `FavoriteIngredientDao` and `ingredientType`.
   - Retain `HopRepository`, `MaltRepository`, `HoneyRepository`, `PomeRepository`, and `YeastRepository` as concise concrete adapters (~50 lines each) delegating Room DAO queries via constructor lambdas and `fetchForStyle`.

3. **Generic Detail ViewModel & Cross-Discipline Catalog Aggregator (`IngredientDetailViewModel` & `IngredientCatalogRepository`)**:
   - Replace the five shallow pass-through `*DetailViewModel` classes (`HopDetailViewModel`, `MaltDetailViewModel`, `HoneyDetailViewModel`, `PomeDetailViewModel`, `YeastDetailViewModel`) with a single `IngredientDetailViewModel<T : Ingredient>` that accepts `IngredientRepository<T, *>`, `BeerStyleRepository`, `itemId`, and an optional `relatedItemsProvider: (String) -> Flow<List<T>>` (used by Hops for child cultivar lineage).
   - Introduce `IngredientCatalogRepository` and `@Immutable data class StyleIngredients` to aggregate all five ingredient repositories at the data seam (`getIngredientsForStyle`, `getFavoriteIngredients`, `removeFavoriteIngredient`), simplifying `StyleDetailViewModel` and `FavoritesViewModel` and collapsing the five duplicate `CollapsibleCommon*Card` composables in `StyleDetailScreen` into a single generic `CollapsibleIngredientSectionCard<T : Ingredient>`.

4. **Modular, Idempotent Domain Seeders (`DomainSeeder`)**:
   - Decompose `DatabaseInitializer` into `fun interface DomainSeeder`, `StyleGuidelineResolver`, and six self-contained seeder modules in `com.bc.beerworks.data.local.seeder` (`StyleSeeder`, `HopSeeder`, `MaltSeeder`, `HoneySeeder`, `PomeSeeder`, `YeastSeeder`).
   - Encapsulate raw JSON DTOs privately inside each seeder, guard asset parsing with fast-path DAO count checks (`getHopCount() > 0`, etc.), and execute all six seeders concurrently via `coroutineScope { seeders.map { async { ... } }.awaitAll() }`.

## Consequences
- Eliminates over 1,500 lines of duplicated ViewModel, UI card, style-mapping, and test boilerplate across the codebase.
- Establishes deep modules with narrow, type-safe interfaces (`IngredientRepository`, `IngredientCatalogRepository`, `IngredientDetailViewModel<T>`, `DomainSeeder`).
- Fixes live `Favorite` state propagation on ingredient detail pages when beverage styles are favorited or unfavorited.
- Makes startup database initialization idempotent and concurrent on warm launches.
