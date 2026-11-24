# Feature Spec: 002 Hop Profile Database & Style Cross-Reference

---

## 🎯 High-Level Goal
Provide an offline-first, visually rich reference database of global hop varieties (*Humulus lupulus*) with multi-attribute search and filtering, custom sensory radar visualizations, interactive lineage genetics traversal, and bidirectional relational cross-referencing between hop varieties and beer style guidelines.

---

## 👤 User Story
**As a** BJCP Beer Judge or Homebrewer  
**I want to** look up hop varieties, inspect their alpha/beta acid potential, view their 8-axis sensory radar profiles, explore their botanical lineage, and see what beer styles they are commonly used in  
**So that** I know exactly what aromas, bitterness, and flavors to evaluate in a flight or design into a recipe.

---

## 📚 Domain & Architecture References
- **Domain Vocabulary**: Strictly adhere to `GLOSSARY.md` (`Hop Variety`, `Common Hops`, `Lineage`, `Purpose`, `Sensory Category`, `Sensory Descriptor`, `Sensory Radar Chart`, `Alpha Acid`, `Beta Acid`, `Hop Substitute`).
- **Architectural Decision**: `docs/adr/0001-relational-hop-style-cross-reference.md` (Dedicated Room join table `HopStyleCrossRef` for sub-millisecond bidirectional queries).
- **Performance Standards**: Follow `GEMINI.md` (Compose `@Immutable` models, `flowOn(Dispatchers.Default)`, SQLite FTS4 subquery indexing, asset preloading via `DatabaseInitializer`).

---

## 📋 Requirements & Acceptance Criteria

### 1. Data Pipeline & Persistence
- [x] **Bundled Asset (`hops.json`)**: Curate and bundle `app/src/main/assets/hops.json` (~80–100 prominent global cultivars) containing IDs, names, country/origin, purpose, alpha/beta acid ranges, sensory categories, aroma descriptors, descriptions, parent hop IDs, substitute IDs, common style IDs, and optional 8-axis radar profiles.
- [x] **Room Database Version 3**:
  - `HopVarietyEntity`: Primary table storing complete hop metadata.
  - `HopVarietyFtsEntity`: SQLite FTS4 table for sub-millisecond full-text search over names, origins, descriptors, and descriptions.
  - `HopStyleCrossRef`: Many-to-many relational join table keyed on `(hopId, styleId, guidelineType)` for bidirectional querying.
- [x] **Database Preloading (`HopSeeder`)**: `DatabaseInitializer` delegates to `HopSeeder` (`DomainSeeder`) to seed Room tables from `hops.json` on first launch (with idempotent count checks) without main-thread blocking.
- [x] **Repository Layer (`HopRepository`)**: Concrete subclass of `BaseIngredientRepository<HopVarietyEntity, HopVariety, HopFilterCriteria>` exposing catalog search/filtering, individual hop details (`IngredientDetailViewModel<HopVariety>` with `relatedItemsProvider = hopRepository::getChildHops`), dynamic child descendants, and style cross-references (`IngredientCatalogRepository`), dispatched on `Dispatchers.Default`.

### 2. Hop Catalog List Screen (`HopsScreen`)
- [x] **Shared Catalog Scaffold (`IngredientCatalogScaffold<HopVariety>`) & ViewModel (`HopsViewModel : IngredientListViewModel<HopVariety, HopFilterCriteria>`)**:
  - Borderless pill search bar (`OutlinedTextField` with leading search icon, trailing clear button, no placeholder hint text per `GEMINI.md`, and auto-clearing focus on scroll or background tap).
  - Top bar with drawer menu button and `FilterActionIcon` (tap to open `HopFilterSheet`, long-press to reset active filters while preserving search query).
- [x] **Advanced Filter Sheet (`HopFilterSheet`)**: Bottom modal sheet with:
    - Purpose selection chips (`Aroma`, `Bittering`, `Dual-Purpose`).
    - Sensory Profile selection chips (`Citrus`, `Tropical`, `Floral`, `Herbal`, `Spicy`, `Piney`,
      `Earthy`, `Grassy`, `Other`), matching primary 8-axis radar scores at `>= 3.5` (`>= 2.5` for
      `Grassy`) sorted strongest-first, and word-boundary non-8-axis descriptors for `Other`.
    - Alpha Acid % range slider (0% to 25%).
  - Origin country multi-select chips.
    - Common Beer Style category selection chips.
- [x] **Glanceable `HopCard` Items (`IngredientCardScaffold`)**:
  - Strict 4-row vertical layout (`GEMINI.md`):
    1. Header row: Origin flag emoji + bold hop name on the left, color-coded `PurposeBadge` (`IngredientBadge`) on the top right.
    2. Sub-header: Italicized `"Humulus lupulus • ${hop.origin}"`.
    3. Narrative description (max 3 lines with ellipsis).
    4. Bottom strip: Single-row horizontal descriptor chip strip (`SingleRowChipLayout`).
  - Clickable card without trailing navigation chevron.
- [x] **Zero-Result Filter Recovery**: Friendly empty state with a single-tap "Reset Filters" action button shown when `criteria.isFilterActive` is true.

### 3. Hop Detail Screen (`HopDetailScreen` / `hop_detail/{hopId}`)
- [x] **Shared Detail Scaffold (`IngredientDetailScaffold`)**: Standard top bar (back navigation + favorite heart toggle) and scrollable card column backed by `IngredientDetailViewModel<HopVariety>`.
- [x] **Acid Range Gauges**: Custom horizontal graphical range meters for Alpha Acid (warm amber gradient) and Beta Acid (sage green gradient) highlighting the min-max span against a standard scale.
- [x] **8-Axis Sensory Radar Chart (`IngredientSensoryProfileCard` & `SensoryProfile`)**: Custom Jetpack Compose `Canvas` radial polygon (`SensoryRadarChart(profile: SensoryProfile)`) plotting quantitative intensity (0.0–5.0) across *Citrus, Tropical, Floral, Herbal, Spicy, Piney, Earthy, Grassy*. Semi-transparent fill and labeled vertices.
- [x] **Graceful Radar Fallback**: When numeric radar profile data is absent for a hop, hide the radar chart and display sensory category chips alongside the full aroma descriptor cloud.
- [x] **Lineage & Genetics Section**: Displays parent hop chips and dynamically resolved child descendant chips. Tapping any lineage chip navigates directly to that hop's detail screen.
- [x] **Hop Substitutes Section (`IngredientSubstitutesCard`)**: Displays alternative hop chips with comparable brewing profiles. Tapping any substitute chip navigates directly to that hop's profile.
- [x] **Common Beer Styles Section (`AssociatedStylesSection`)**: Displays clickable style cards
  associated with the hop (auto-collapsed when more than 4 styles are linked to keep staple
  ingredients uncluttered). Tapping a style navigates directly to `style_detail/{id}/{guideline}`.

### 4. Bidirectional Style Guide Integration (`StyleDetailScreen`)
- [x] **Collapsible "Common Hops" Section**: On `StyleDetailScreen`, add a card titled `"Common Hops (N)"` below the guideline sensory sections.
- [x] **Collapsed by Default**: Starts collapsed by default to preserve the clean BJCP/BA guideline reading flow.
- [x] **Deep Linking**: Expanding the card displays interactive hop chips with purpose tags. Tapping any hop chip navigates directly to `hop_detail/{hopId}`.

---

## 📱 User Interface & Visuals
- **HopsScreen**: Top borderless search bar + horizontal quick-filter row + lazy column of `HopCard`s.
- **HopDetailScreen**:
  - Top bar with back navigation arrow.
  - Hero header with origin flag, purpose pill, and description.
  - Visual Alpha & Beta acid range bars.
  - Custom Canvas Sensory Radar Chart or descriptor chip cloud.
  - Lineage & Breeding chips.
  - Substitute chips.
  - Common Beer Styles card list.
- **StyleDetailScreen**: Collapsible "Common Hops" expansion card at bottom of style detail.

---

## 📦 Assets & Attachments
- `app/src/main/assets/hops.json` (~80–100 curated global hop profiles).
- Room migrations: Database version 2 -> 3.
