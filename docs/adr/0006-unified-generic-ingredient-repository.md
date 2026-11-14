# 0006. Unified Generic Ingredient Architecture, Catalog Aggregation Seam, and Modular Domain Seeders

## Context
Across Features 002 (Hops), 003 (Malts), 006 (Honey), 007 (Pomes), 008 (Yeast), and 009 (Favorites), five ingredient disciplines and four beverage style guidelines (BJCP 2021, BA 2026, BJCP Mead, BJCP Cider) were introduced.

While each ingredient has specialized pomological, chemical, kilning, or fermentation metrics, their architectural behavior across the persistence, repository, ViewModel, and Compose UI layers is identical:
- Sanitizing multi-word FTS match queries (`query.split("\\s+".toRegex()).joinToString(" ") { "$it*" }`).
- Mapping raw database entities to domain models on background threads via `flowOn(Dispatchers.Default)`.
- Resolving style cross-references across all four guidelines with live `Favorite` state (`FavoriteStyleDao`) and canonical ordering.
- Mapping `BjcpStyleEntity`, `BaStyleEntity`, `MeadStyleEntity`, and `CiderStyleEntity` to `BeerStyle`.
- Managing catalog list state (`filterCriteria`, filtered `items` via `flatMapLatest(repository::filter)`, `onQueryChange`, `onFilterCriteriaChange`, `resetFilters()` preserving `query`, and distinct sorted filter option streams).
- Managing detail screen state (`item`, `isFavorite`, `substitutes`, `commonStyles`, and optional `relatedItems` such as child hop cultivars) and toggling favorites.
- Aggregating cross-discipline ingredients for `StyleDetailViewModel` and `FavoritesViewModel`.
- Rendering 8-axis organoleptic radar polygons (`SensoryRadarChart`), 4-row catalog cards (`IngredientCardScaffold`), catalog list screens (`IngredientCatalogScaffold`), and detail screens (`IngredientDetailScaffold`).
- Seeding Room tables from bundled JSON assets (`bjcp_styles.json`, `ba_styles.json`, `mead_styles.json`, `cider_styles.json`, `hops.json`, `malts.json`, `honeys.json`, `pomes.json`, `yeasts.json`).

Attempting to make Room DAOs completely generic (e.g. `interface GenericDao<T>`) is problematic because Room's `@Query` and `@Fts4` annotations require static, compile-time verified SQL statements. Using raw queries (`SupportSQLiteQuery`) sacrifices Room's automatic reactive Flow change notifications and type safety.

## Decision
We decided to establish deep modules across seven architectural seams:

1. **Polymorphic Domain, Entity, Filter & Sensory Contracts (`Ingredient`, `StyleEntity`, `IngredientFilterCriteria`, `SensoryProfile`)**:
   - Define `@Immutable interface Ingredient` modeling common catalog identity, geographic provenance, and search behavior (`id`, `name`, `origin`, `countryCode`, `description`, `descriptors`, `substituteIds`, default `countryFlag`, and polymorphic `matchesBaseQuery(query)` / `matchesQuery(query)`).
   - Define `interface StyleEntity` implemented by `BjcpStyleEntity`, `BaStyleEntity`, `MeadStyleEntity`, and `CiderStyleEntity` with a single default `toDomainModel(isFavorite: Boolean): BeerStyle` implementation, eliminating duplicate style-mapping functions across `BeerStyleRepository` and `BaseIngredientRepository`.
   - Define `@Immutable interface IngredientFilterCriteria<Self : IngredientFilterCriteria<Self>>` implemented by `HopFilterCriteria`, `MaltFilterCriteria`, `HoneyFilterCriteria`, `PomeFilterCriteria`, and `YeastFilterCriteria` with `query`, `isFilterActive`, `withQuery(String)`, and `resetKeepingQuery()`.
   - Define `@Immutable interface SensoryProfile` implemented by `SensoryRadarProfile`, `MaltSensoryProfile`, `HoneySensoryProfile`, `PomeSensoryProfile`, and `YeastSensoryProfile` with `defaultStrokeColorHex`, `hasData()`, and `toAxisMap(): List<Pair<String, Float>>`.

2. **Deep Generic Repository Seam (`IngredientRepository` & `BaseIngredientRepository`)**:
   - Establish `interface IngredientRepository<T : Ingredient, C : Any>` and `abstract class BaseIngredientRepository<Entity : Any, Domain : Ingredient, Criteria : Any>` centralizing:
     - FTS query term sanitization and `.flowOn(Dispatchers.Default)` background execution.
     - Substitute resolution (`getSubstitutesForItem`) and multi-guideline style resolution (`getCommonStylesForItem`) combined with live `FavoriteStyleDao` state and sorted by `(guidelineType.ordinal, categoryNumber, number)`.
     - Reverse style-to-ingredient lookup (`getItemsForStyle(styleId, guidelineType)`).
     - Ingredient favorite state (`isFavorite`, `getFavoriteItems`, `toggleFavorite`) backed by `FavoriteIngredientDao` and `ingredientType`.
   - Retain `HopRepository`, `MaltRepository`, `HoneyRepository`, `PomeRepository`, and `YeastRepository` as concise concrete adapters (~50 lines each) delegating Room DAO queries via constructor lambdas and `fetchForStyle`.

3. **Generic List & Detail ViewModels (`IngredientListViewModel` & `IngredientDetailViewModel`)**:
   - Introduce `open class IngredientListViewModel<T : Ingredient, C : IngredientFilterCriteria<C>>` centralizing `filterCriteria`, `items`, `onQueryChange`, `onFilterCriteriaChange`, `updateCriteria`, `resetFilters()` (consistently preserving `query`), and `distinctSortedOptionFlow(defaultOptions, selector)` across `HopsViewModel`, `MaltViewModel`, `HoneyViewModel`, `PomesViewModel`, and `YeastsViewModel`.
   - Replace the five shallow pass-through `*DetailViewModel` classes (`HopDetailViewModel`, `MaltDetailViewModel`, `HoneyDetailViewModel`, `PomeDetailViewModel`, `YeastDetailViewModel`) with a single `IngredientDetailViewModel<T : Ingredient>` that accepts `IngredientRepository<T, *>`, `BeerStyleRepository`, `itemId`, and an optional `relatedItemsProvider: (String) -> Flow<List<T>>` (used by Hops for child cultivar lineage).

4. **Cross-Discipline Catalog Aggregator (`IngredientCatalogRepository`)**:
   - Introduce `IngredientCatalogRepository` and `@Immutable data class StyleIngredients` to aggregate all five ingredient repositories at the data seam (`getIngredientsForStyle`, `getFavoriteIngredients`, `removeFavoriteIngredient`), simplifying `StyleDetailViewModel` and `FavoritesViewModel` and collapsing the five duplicate `CollapsibleCommon*Card` composables in `StyleDetailScreen` into a single generic `CollapsibleIngredientSectionCard<T : Ingredient>`.

5. **Shared Compose Scaffolds (`IngredientCardScaffold`, `IngredientCatalogScaffold`, `IngredientDetailScaffold`)**:
   - Unify the 5 profile-specific `SensoryRadarChart` overloads into a single `SensoryRadarChart(profile: SensoryProfile, ...)`.
   - Introduce `IngredientCardScaffold` and `IngredientBadge` enforcing the strict 4-row `GEMINI.md` layout (Header Row -> Italicized Sub-Header -> 3-line Narrative Description -> Bottom `SingleRowChipLayout`) across `HopCard`, `MaltCard`, `HoneyCard`, `PomeCard`, and `YeastCard`.
   - Introduce `IngredientCatalogScaffold<T : Ingredient>` encapsulating the top bar, `FilterActionIcon`, borderless pill search bar, optional quick-filter strip, scroll-to-top reset, focus-clearing nested scroll, conditional `"Reset Filters"` empty state, keyed `LazyColumn`, and filter sheet across `HopsScreen`, `MaltScreen`, `HoneyScreen`, `PomesScreen`, and `YeastsScreen`.
   - Introduce `IngredientDetailScaffold`, `IngredientDetailLoadingOrContent`, `IngredientSensoryProfileCard`, `IngredientSubstitutesCard<T>`, and `AssociatedStylesSection` across all 5 `*DetailScreen.kt` files.

6. **Application Dependency Container & Factory Seam (`AppContainer` & `viewModelFactory`)**:
   - Extract `AppContainer` to own the application singleton dependency graph (`AppDatabase`, `PreferencesManager`, `BeerStyleRepository`, `HopRepository`, `MaltRepository`, `HoneyRepository`, `PomeRepository`, `YeastRepository`, `IngredientCatalogRepository`, `ReviewRepository`).
   - Provide `inline fun <reified VM : ViewModel> viewModelFactory(crossinline create: () -> VM)` and screen-level `*Screen(viewModel)` entrypoints so `MainActivity.kt` focuses purely on route declarations and navigation callbacks.

7. **Modular, Idempotent Domain Seeders (`DomainSeeder`)**:
   - Decompose `DatabaseInitializer` into `fun interface DomainSeeder`, `StyleGuidelineResolver`, and six self-contained seeder modules in `com.bc.beerworks.data.local.seeder` (`StyleSeeder`, `HopSeeder`, `MaltSeeder`, `HoneySeeder`, `PomeSeeder`, `YeastSeeder`).
   - Encapsulate raw JSON DTOs privately inside each seeder, guard asset parsing with fast-path DAO count checks (`getHopCount() > 0`, etc.), and execute all six seeders concurrently via `coroutineScope { seeders.map { async { ... } }.awaitAll() }`.

## Consequences
- Eliminates over 3,000 lines of duplicated ViewModel, UI scaffold, card, radar chart, style-mapping, navigation factory, and test boilerplate across the codebase.
- Establishes deep modules with narrow, type-safe interfaces (`IngredientRepository`, `IngredientCatalogRepository`, `IngredientListViewModel<T, C>`, `IngredientDetailViewModel<T>`, `SensoryProfile`, `IngredientCardScaffold`, `IngredientCatalogScaffold<T>`, `IngredientDetailScaffold`, `AppContainer`, `DomainSeeder`).
- Fixes live `Favorite` state propagation on ingredient detail pages and fixes empty-state `"Reset Filters"` button visibility on `MaltScreen` and `HoneyScreen`.
- Makes startup database initialization idempotent and concurrent on warm launches.
