# 📋 Beerworks Backlog & Idea Board

This backlog stores feature ideas, user requests, and future capabilities before they are flushed out into formal Spec-Driven Development specs (`features/00X-feature-name/spec.md`).

---

## ✅ Completed Ideas & Enhancements (Implemented in Feature 001)

- [x] **Collapse Beer Styles by Category**: Expandable/collapsible base style headers (`1. Standard American Beer`, `21. IPA`) with press-and-hold inline sub-style expansion and context-aware back navigation state.
- [x] **Multi-Column & Commercial Examples Search**: Search across style numbers (`1A`, `7B`), style names, commercial examples (`Bud Light`, `Guinness`, `Pliny`), and hop/malt/yeast descriptors (`Citra`, `Cascade`, `Pilsner`).
- [x] **SRM Color Gradient Visual Enhancements**: Custom vertical SRM gradient scrollbar with dynamic foam head position thumb indicator, full-width horizontal bottom SRM gradient banner, and smart N/A range filter exclusion.
- [x] **Left Slide-Out Navigation & Settings**: Slide-out navigation drawer (`Style Guide`, `Hops`, `Malt`, `Review History`, `Settings`) and dedicated `SettingsScreen` for BJCP 2021 / BA 2026 guideline selection.

---

## 🎯 Planned Roadmap Items (For Future Breakdown)

- [x] **Hop Profile Database & Style Cross-Reference (Feature 002)**
  - Search hops by name, origin, or flavor descriptor (citrus, dank, noble).
  - Cross-reference hop profiles directly with BJCP / BA style guidelines.

- [x] **Malt Profile Database & Flavor Characteristics (Feature 003)**
  - Explore base & specialty malts, EBC/Lovibond color ratings, extract potential, and diastatic power.
  - Typical usage roles, functional tags, and 8-axis malt flavor radar chart.
  - Notable maltsters & commercial examples (Briess, Weyermann, Crisp, Dingemans, etc.).
  - Cross-reference malt profiles directly with BJCP / BA style guidelines.

- [ ] **Side-by-Side Comparison Matrix (Beer Styles, Hops & Malts)**
  - Compare 2–3 beer styles side-by-side across vital statistics (ABV, IBU, SRM), appearance, and sensory profiles.
  - Compare 2–3 hop varieties side-by-side with overlaid/contrasting 8-axis sensory radar charts, alpha/beta acid ranges, and common beer styles.
  - Future expansion: Compare 2–3 malt profiles side-by-side (color Lovibond/EBC, extraction %, flavor contributions).

- [x] **Digital BJCP Beer Scoresheet Entry (Feature 011)**
  - Record BJCP Beer scoresheets (Aroma, Appearance, Flavor, Mouthfeel, Overall Impression) with per-section descriptor chips, optional notes, Bottle Inspection, 0–10 impressions, and canonical Off-Flavor fault chips.
  - Discipline switcher in `ScoresheetEditorScreen` (`Beer` | `Mead` | `Cider`) and discipline toggle filter chips on `ReviewsScreen`.

- [x] **Digital BJCP Mead Scoresheet Entry (Feature 012)**
  - Record BJCP Mead scoresheets (Aroma 10, Appearance 6, Flavor 24, Overall Impression 10) backed by polymorphic `Scoresheet` (`BeerScoresheet` / `MeadScoresheet`) in `scoresheets` (`MIGRATION_13_14`).
  - Mead-scoped style linking (`M1`–`M4`), collapsible `"Additional Information"` (Carbonation, Sweetness, Strength chips + Special Ingredients), Mead sensory descriptors, and 24 Mead-relevant Off-Flavor chips.
  - Future expansion: Cider and BA scoresheet formats plus judge/location auto-population from user profile.

- [ ] **AI / OCR Photo Digitizer for Handwritten Scoresheets**
  - Capture a photo of a physical scoresheet and parse it into a digital scoresheet automatically.

- [ ] **Searchable Judging Log & Firebase Sync**
  - Search past judging entries by date, competition, style, or score.
  - Persist scoresheets safely in Firebase DataStore.

- [ ] **Social Judge Comparison & Flight Consensus**
  - Compare scoring sheets side-by-side with co-judges during competition flights.

- [x] **BJCP Mead Style Guidelines Support (Feature 004)**
  - Incorporate the official BJCP Mead Guidelines (Categories M1–M4: Traditional Mead, Fruit Mead, Spiced Mead, Specialty Mead).
  - Support mead-specific attributes: sweetness (Dry, Semi-Sweet, Sweet), carbonation (Still, Pétillant, Sparkling), and strength (Hydromel, Standard, Sack).
  - Integrate into guideline switching alongside BJCP 2021 Beer and BA 2026 Beer with dynamic discipline icons.

- [x] **BJCP Cider Style Guidelines Support (Feature 005)**
  - Incorporate the official BJCP Cider Guidelines (Categories C1–C2: Standard Cider & Perry, Specialty Cider & Perry).
  - Support cider-specific attributes (5-tier sweetness levels, carbonation, strength tiers, apple/pear varieties, ABV).
  - Integrate into guideline switching alongside Beer and Mead with dynamic discipline icon (🍏 / Nature).

- [x] **Honey Profile Database & Varietal Sensory Profiles (Feature 006)**
  - Comprehensive reference of canonical varietal honeys (Orange Blossom, Clover, Tupelo, Meadowfoam, Buckwheat, etc.).
  - Pfund scale (mm) and USDA color classifications (Water White to Dark Amber) with visual color swatches.
  - Fructose/Glucose ratios, crystallization tendencies (Slow, Moderate, Rapid), moisture %, and gravity yields.
  - Custom 8-axis honey sensory radar chart (Floral, Fruity, Herbal, Spicy, Sweet, Nutty, Earthy, Animal/Pungent).
  - Nutrient demand and volatile aroma preservation in meadmaking.
  - Bidirectional relational cross-referencing between honey varieties and BJCP Mead styles (M1–M4), Braggots, and Cysers.

- [x] **Pome Varieties Database & Sensory Profiles (Feature 007)**
  - Comprehensive reference of canonical cider apple and perry pear varieties (Dabinett, Kingston Black, Yarlington Mill, Harrison, Thorn, etc.).
  - Long Ashton 4-tier classification (Bittersweet, Bittersharp, Sharp, Sweet) with visual 2×2 matrix gauge.
  - Pomological & chemical metrics: Acidity (% malic acid), Tannins (% polyphenols and Low/Med/High tiers), Specific Gravity (SG) range, and ripening season.
  - Perry pear sorbitol content, pressing dynamics, and milling behavior.
  - Custom 8-axis pome sensory radar chart (Acidity/Sharpness, Tannin/Bitterness, Fresh Fruit, Baked/Ripe Fruit, Citrus & Tropical, Floral & Herbal, Earthy & Farmhouse, Sweet & Confectionary).
  - Navigation drawer integration with `Nature` icon for Pomes and `Grass` (wheat/stalks) icon for Malt.
  - Bidirectional relational cross-referencing between pome varieties and BJCP Cider styles (C1A–C2F) and Mead Cysers (M2B).

- [x] **Yeast Strains Database & Fermentation Profiles (Feature 008)**
  - Comprehensive catalog of ~50 canonical brewing, cider, and mead yeast strains (American Ale, English, Belgian, German/Lager, Kveik, Wild/Bacteria, Mead/Wine, Cider).
  - Commercial lab cross-referencing (White Labs, Wyeast, Fermentis, Lallemand, Imperial, Omega, etc.) with sub-millisecond FTS search on vendor codes (e.g. "US-05", "WLP001", "1056").
  - Physiological metrics: Attenuation % range, Flocculation tier, Alcohol tolerance (% ABV), and optimum fermentation temperature range.
  - User-configurable temperature unit preference (Fahrenheit / Celsius) in Settings.
  - Clean genetic status tags (`STA1+/-`, `POF+/-`) without alarmist clutter.
  - Custom 8-axis yeast sensory radar chart (Clean, Fruity, Spicy, Tart, Funky, Sulfury, Alcohol, Mouthfeel).
  - Navigation drawer integration with `BubbleChart` icon.
  - Bidirectional relational cross-referencing between yeast strains and Beer, Mead, and Cider styles.

- [x] **Favorites Catalog & Cross-Discipline Reference (Feature 009)**
  - Unified Favorites screen (`"favorites"`) aggregating favorited Beverage Styles (BJCP 2021, BA 2026, BJCP Mead, BJCP Cider) and Ingredients (Hops, Malts, Honey, Pomes, Yeast) in canonical drawer order.
  - Instant cross-discipline keyword search (`Ingredient.matchesQuery`) and dynamic discipline filter chips with item counts.
  - Right-to-left swipe-to-remove (`SwipeToDismissBox`) with optional `"Remove from Favorites?"` confirmation modal and `"Always remove without asking"` setting in `SettingsScreen`.

- [x] **Off-Flavors & Sensory Faults Database (Feature 010)**
  - Curated catalog of 28 canonical judging off-flavors distilled from Thomas Barnes' *Complete Beer Fault Guide v1.4* and BJCP sensory standards, placed directly above Favorites in the navigation drawer (`"off_flavors"`, `Icons.Default.Science`).
  - Five root-cause **Off-Flavor Categories** (`Fermentation & Yeast`, `Mashing & Boil`, `Sanitation & Microbial`, `Oxidation & Aging`, `Water & Packaging`) with sensory cue emojis on every card.
  - Sub-millisecond SQLite FTS4 search across off-flavor names, chemical compounds, sensory cues, root causes, remedies, and Meilgaard Flavor Wheel codes, plus multi-attribute filtering (`Category`, `Detected In`, `Beverage Discipline`, `Style Permissibility`).
  - Judging-first detail page featuring a **Sensory Detection & Thresholds Card**, diagnostic **How to Tell Apart** contrast notes (distinguishing faults from normal ingredients and lookalike faults), clickable lookalike `OffFlavorCard` links, root-cause & scoresheet remedy bullets, and one-way links to permissible `BeerStyle` cards.
  - Full integration with **Favorites** (`FavoriteDiscipline.OFF_FLAVORS`).

---

## 🏗️ Planned Architectural Deepening (Completed Enhancements)

- [x] **Consolidate Shallow Ingredient Detail ViewModels (Architecture Candidate 1)**
  - Replace five separate pass-through view models (`HopDetailViewModel`, `MaltDetailViewModel`, `HoneyDetailViewModel`, `PomeDetailViewModel`, `YeastDetailViewModel`) with a single deep generic `IngredientDetailViewModel<T : Ingredient>` (with optional `relatedItemsProvider` for child hop cultivars).
  - Maximize leverage from the unified `IngredientRepository` seam (ADR 0006) across all ingredient disciplines.
  - Unify detail lifecycle state emission, substitute resolution, and style favorite toggling into one thoroughly tested interface.

- [x] **Deepen Style-to-Ingredient Association & Favorites Aggregation (Architecture Candidate 2)**
  - Consolidate five separate ingredient repositories and five separate StateFlow streams in `StyleDetailViewModel` and `FavoritesViewModel` into a unified `StyleIngredients` stream aggregated at the data seam (`IngredientCatalogRepository`).
  - Collapse the 5 duplicate `CollapsibleCommon*Card` composables in `StyleDetailScreen.kt` into a single generic `CollapsibleIngredientSectionCard<T : Ingredient>`.
  - Isolate ingredient cross-referencing, polymorphic `Ingredient.matchesQuery(query)`, and `countryFlag` defaults within the domain/data layer so future ingredients require zero ViewModel or navigation wiring changes.

- [x] **Unify Style Entity Domain Mapping & Live Favorite Style Resolution (Architecture Candidate 3)**
  - Extract `interface StyleEntity` implemented by `BjcpStyleEntity`, `BaStyleEntity`, `MeadStyleEntity`, and `CiderStyleEntity` with a single default `toDomainModel(isFavorite: Boolean): BeerStyle` implementation across `BeerStyleRepository` and `BaseIngredientRepository`.
  - Combine live `FavoriteStyleDao` state inside `BaseIngredientRepository.getCommonStylesForItem(id)` so associated style cards on ingredient detail pages reflect real-time favorite status across all four guidelines.

- [x] **Decompose Monolithic DatabaseInitializer into Focused Domain Seeders (Architecture Candidate 4)**
  - Decompose the 752-line procedural seeder into isolated, self-contained `DomainSeeder` modules (`StyleSeeder`, `HopSeeder`, `MaltSeeder`, `HoneySeeder`, `PomeSeeder`, `YeastSeeder`) and `StyleGuidelineResolver`.
  - Encapsulate raw JSON DTOs and Gson mapping logic privately within each seeder and execute seeders concurrently with idempotent DAO count checks.
  - Coordinate seeding through a lightweight asynchronous runner executing independent datasets in parallel on the background dispatcher pool.

- [x] **Unify Catalog List ViewModels & Filter Criteria Contract (Round 2 Candidate 1)**
  - Introduce `@Immutable interface IngredientFilterCriteria<Self>` and `open class IngredientListViewModel<T : Ingredient, C : IngredientFilterCriteria<C>>`.
  - Refactor `HopsViewModel`, `MaltViewModel`, `HoneyViewModel`, `PomesViewModel`, and `YeastsViewModel` to inherit shared reactive search/filter state, `distinctSortedOptionFlow`, and `resetFilters()` (preserving active search query).

- [x] **Unify SensoryProfile, IngredientCardScaffold & IngredientCatalogScaffold (Round 2 Candidate 2)**
  - Introduce `@Immutable interface SensoryProfile` across all 5 sensory radar models and collapse 5 `SensoryRadarChart` overloads into a single `SensoryRadarChart(profile: SensoryProfile)`.
  - Extract `IngredientCardScaffold` and `IngredientBadge` enforcing the strict 4-row `GEMINI.md` layout across `HopCard`, `MaltCard`, `HoneyCard`, `PomeCard`, and `YeastCard`.
  - Extract `IngredientCatalogScaffold<T : Ingredient>` across `HopsScreen`, `MaltScreen`, `HoneyScreen`, `PomesScreen`, and `YeastsScreen`, fixing unconditional `"Reset Filters"` button rendering in empty states.

- [x] **Extract IngredientDetailScaffold & Shared Detail Section Cards (Round 2 Candidate 3)**
  - Extract `IngredientDetailScaffold`, `IngredientDetailLoadingOrContent`, `IngredientSensoryProfileCard`, `IngredientSubstitutesCard<T>`, and `AssociatedStylesSection` across all 5 `*DetailScreen.kt` files.

- [x] **Extract AppContainer & Streamline MainActivity Navigation Wiring (Round 2 Candidate 5)**
  - Extract `AppContainer` and `inline fun <reified VM : ViewModel> viewModelFactory` to eliminate 13 anonymous `ViewModelProvider.Factory` blocks and inline `collectAsState()` calls in `MainActivity.kt`.

---

## 💡 Unsorted / Fresh Ideas

- [ ] **Scoresheet Descriptor Guideline Quick-Peek (Long-Press)**: When a BJCP style is linked on a Scoresheet, press-and-holding any section descriptor chip (e.g., Aroma `Malt` for `5D`) opens a bottom sheet showing the exact excerpt from that style's guideline section (e.g., *"Low to medium grainy, sweet, or doughy malt character, often with a light honey and toasted cracker quality"*).
