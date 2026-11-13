# Feature Spec: 008 Yeast Strains Database & Fermentation Profiles

---

## 🎯 High-Level Goal
Provide an offline-first, technically detailed reference database of canonical brewing, cider, and mead **Yeast Strains** with multi-attribute filtering, 8-axis fermentation flavor radar profiles, essential physiological metrics (attenuation %, flocculation, alcohol tolerance, optimal temperature), clean genetic status markers (`STA1+/-`, `POF+/-`), commercial laboratory cross-referencing (White Labs, Wyeast, Fermentis, Lallemand, Imperial, Omega, etc.), and bidirectional relational links with Beer, Mead, and Cider styles.

---

## 👤 User Story
**As a** Brewer, Meadmaker, or Cidermaker  
**I want to** look up canonical yeast strains, search by commercial manufacturer codes (e.g. "US-05", "WLP001", "1056", "EC-1118"), inspect attenuation ranges, flocculation tiers, optimal fermentation temperatures, and 8-axis sensory radar profiles, and see which styles each strain pairs with  
**So that** I select the ideal fermentation culture for my recipe, find interchangeable strain substitutes, and understand the sensory impact of yeast on finished beverages.

---

## 📚 Domain & Architecture References
- **Domain Vocabulary**: Strictly adhere to `GLOSSARY.md` (`Yeast Strain`, `Microorganism Classification`, `Attenuation`, `Flocculation`, `Alcohol Tolerance`, `Optimum Temperature Range`, `Diastaticus (STA1)`, `Phenolic Off-Flavor (POF)`, `Commercial Yeast Equivalents`, `Yeast Sensory Radar Chart`, `Yeast Substitute`).
- **Architectural Decisions**:
  - `docs/adr/0001-relational-hop-style-cross-reference.md`: Dedicated Room relational join table for sub-millisecond bidirectional queries between ingredients and styles.
  - `docs/adr/0002-canonical-malt-varieties.md`: Canonical ingredient varieties focused on underlying biology and science rather than separate entries for commercial SKUs.
  - `docs/adr/0006-unified-generic-ingredient-repository.md`: Shared `Ingredient` interface, `IngredientRepository`, and `BaseIngredientRepository` with automated FTS query sanitization and background thread dispatching.
  - `docs/adr/0007-canonical-yeast-strains-fermentation-sensory-model.md`: Canonical yeast strain modeling with embedded commercial lab equivalents, tri-discipline beverage scope, uncluttered genetic markers, and configurable temperature units.
- **Performance Standards**: Follow `GEMINI.md` (Compose `@Immutable` models, `flowOn(Dispatchers.Default)`, SQLite FTS4 subquery indexing, asset preloading via `DatabaseInitializer`, no placeholder hint text in search fields, no "All" filter chips, no trailing navigation arrows on card items).

---

## 📋 Requirements & Acceptance Criteria

### 1. Data Pipeline & Persistence
- [ ] **Bundled Asset (`yeasts.json`)**: Curate and bundle `app/src/main/assets/yeasts.json` (~45–50 canonical strains across American Ales, English/Irish, Belgian/French, German/Lagers, Kveik, Wild/Bacteria, Mead/Wine, and Cider) containing:
  - `id`: Unique kebab-case identifier (e.g. `chico-american-ale`, `weihenstephan-weizen`, `lalvin-ec-1118`, `voss-kveik`).
  - `name`: Canonical strain name (e.g. "Chico / American Ale", "Weihenstephan Weizen").
  - `scientificName`: Genus & species (e.g. *Saccharomyces cerevisiae*, *Saccharomyces pastorianus*, *Brettanomyces bruxellensis*, *Lactobacillus plantarum*).
  - `origin`, `countryCode`: Geographic provenance (e.g. "Chico, California, USA", "US").
  - `discipline`: Primary beverage discipline (`Beer`, `Mead`, `Cider`, or `Multi-Discipline`).
  - `classification`: Biological/functional class (`Ale`, `Lager`, `Wheat`, `Kveik`, `Wild / Brett`, `Bacteria`, `Wine / Mead / Cider`).
  - `form`: Available formats (`Liquid`, `Dry`, `Both`).
  - `attenuationMin`, `attenuationMax`: Apparent attenuation percentage range (e.g. 73.0 to 80.0).
  - `flocculation`: Flocculation tier (`Low`, `Medium`, `Medium-High`, `High`, `Very High`).
  - `tempMinC`, `tempMaxC`: Minimum and maximum optimum fermentation temperature in Celsius (e.g. 18.0 to 22.0).
  - `tempMinF`, `tempMaxF`: Minimum and maximum optimum fermentation temperature in Fahrenheit (e.g. 64.0 to 72.0).
  - `alcoholTolerance`: Maximum alcohol tolerance in % ABV (e.g. 11.0).
  - `isDiastaticus`: Boolean flag for *STA1* gene presence.
  - `isPhenolic`: Boolean flag for *POF* (Phenolic Off-Flavor) 4-VG production.
  - `description`: Narrative background, history, and fermentation characteristics.
  - `pitchingDynamics`: Technical guidance on lag time, attenuation pace, oxygen needs, and nutrient requirements.
  - `descriptors`: Characteristic flavor and aroma notes (e.g., "clean", "crisp", "banana", "clove", "tart", "pear", "leather").
  - `commercialEquivalents`: List of commercial laboratory products containing `laboratory`, `productCode`, `productName`, and `form` (e.g. WLP001 California Ale, Wyeast 1056 American Ale, SafAle US-05, Imperial A07 Flagship, Omega OYL-004 West Coast Ale).
  - `substituteIds`: Interchangeable alternative strain IDs.
  - `commonStyleIds`: Associated style identifiers across BJCP Beer, BA 2026, BJCP Mead, and BJCP Cider guidelines.
  - `radarProfile`: 8 organoleptic dimensions rated 0.0 to 5.0 (Clean/Neutral, Fruity Esters, Spicy Phenols, Tart/Acidity, Funky/Earthy, Sulfury/Crisp, Alcohol/Warmth, Mouthfeel/Sweetness Retention).
- [ ] **Room Database Version 9**:
  - `YeastStrainEntity`: Primary table storing complete yeast strain attributes.
  - `YeastStrainFtsEntity`: SQLite FTS4 virtual table indexing `name`, `scientificName`, `origin`, `description`, `pitchingDynamics`, `descriptors`, and serialized `commercialEquivalents` for sub-millisecond full-text search (including instant manufacturer code matching).
  - `YeastStyleCrossRef`: Relational join table keyed on `(yeastId, styleId, guidelineType)` with composite index for bidirectional style queries.
- [ ] **Room Database Migration (v8 -> v9)**: Automated migration creating new tables and FTS virtual tables without losing existing style/hop/malt/honey/pome/favorites data.
- [ ] **Database Preloading**: `DatabaseInitializer` seeds Room tables from `yeasts.json` on startup in the background without main-thread blocking.
- [ ] **Repository Layer (`YeastRepository`)**: Concrete subclass of `BaseIngredientRepository<YeastStrainEntity, YeastStrain, YeastFilterCriteria>` executing Room queries on `Dispatchers.Default`.

### 2. Temperature Preference & Unit Utility
- [ ] **Preference Storage**: Add `temperatureUnit: Flow<TemperatureUnit>` (enum `FAHRENHEIT`, `CELSIUS`, defaulting to `FAHRENHEIT`) and `setTemperatureUnit(TemperatureUnit)` to `PreferencesManager` and `DefaultPreferencesManager`.
- [ ] **Settings Screen UI**: Add a "Temperature Unit" card in `SettingsScreen` with a clean toggle / segmented control between Fahrenheit (°F) and Celsius (°C).
- [ ] **`TemperatureConverter` Utility**: Clean conversion helper formatting temperature ranges according to active `TemperatureUnit` (e.g. `"64°F – 72°F"` or `"18°C – 22°C"`).

### 3. Yeast Catalog List Screen (`YeastsScreen` / route: `"yeasts"`)
- [ ] **Top App Bar**: Title `"Yeast"`, drawer menu navigation icon, and top-right filter action button (`Icons.Default.FilterList`) tinted with `MaterialTheme.colorScheme.primary` when filters are active.
- [ ] **Borderless Pill Search Bar**: Single-line search input with leading search icon, clear trailing icon (when query non-blank), no placeholder hint text, keyboard search IME action, and auto-dismiss keyboard on list scroll.
- [ ] **Quick-Filter Strip**: Horizontal scrollable chips for **Microorganism Classification** (toggle on/off, without 'All' chip):
  - `Ale`, `Lager`, `Wheat`, `Kveik`, `Wild / Brett`, `Bacteria`, `Wine / Mead / Cider`
- [ ] **Filter Sheet (`YeastFilterSheet`)**: Bottom modal sheet with header `"Filter Yeast"`, `"Reset All"` action button, and `"Apply Filters"` primary button containing:
  1. **Microorganism Classification** (toggle chips: Ale, Lager, Wheat, Kveik, Wild / Brett, Bacteria, Wine / Mead / Cider)
  2. **Flocculation Tier** (toggle chips: Low, Medium, High, Very High)
  3. **Minimum Attenuation %** (Slider from 60% to 90%)
  4. **Minimum Alcohol Tolerance** (Slider from 5% to 18% ABV)
  *(Note: No Form chips and no STA1/POF toggles, keeping filter sheet uncluttered).*
- [ ] **Glanceable `YeastCard` Items**:
  - `RoundedCornerShape(16.dp)` card with subtle elevation and container tint.
  - Strain name in bold typography.
  - Scientific name in italicized typography.
  - Origin country flag & provenance.
  - Discipline badge (`Beer`, `Mead`, `Cider`, `Multi-Discipline`).
  - Classification badge (`Ale`, `Lager`, etc.).
  - Fermentation metrics: Attenuation % range, Flocculation, Alcohol tolerance.
  - Uncluttered genetic markers: minimal `STA1+` / `STA1-` and `POF+` / `POF-` chips without alarmist warning styling.
  - Top flavor descriptors.
  - Clickable card without trailing arrow.
- [ ] **Zero-Result View**: Clean empty state with fermentation icon (`Icons.Default.BubbleChart`), bold headline `"No yeast strains found"`, subtitle `"Try adjusting your search query or filters."`, and `"Reset Filters"` action button.

### 4. Yeast Detail Screen (`YeastDetailScreen` / `yeast_detail/{yeastId}`)
Ordered precisely according to resolved design hierarchy using `RoundedCornerShape(16.dp)` cards:
1. **Hero Header**:
   - Strain name, scientific name, origin flag & country, discipline badge, classification badge.
   - Narrative overview description.
2. **Genetic Traits Bar**:
   - Clean, uncluttered `STA1+` / `STA1-` and `POF+` / `POF-` badges.
   - Form availability (`Liquid`, `Dry`, or `Liquid & Dry`).
3. **Fermentation Chemistry Specs Card**:
   - **Attenuation Range**: Formatted range (e.g. `"73% – 80%"`).
   - **Flocculation Tier**: Badge (e.g. `"Medium-High"`).
   - **Optimum Temperature Band**: Displayed in preferred temperature unit (°F or °C) formatted via `TemperatureConverter`.
   - **Alcohol Tolerance**: Gauge/badge (e.g. `"11% ABV"`).
4. **8-Axis Yeast Sensory Radar Chart**:
   - Radial polygon plotting Clean/Neutral, Fruity Esters, Spicy Phenols, Tart/Acidity, Funky/Earthy, Sulfury/Crisp, Alcohol/Warmth, and Mouthfeel/Sweetness Retention (0.0 to 5.0).
   - Flavor and aroma descriptor chip cloud.
5. **Commercial Laboratory Equivalents Card**:
   - List of commercial products grouped by manufacturer (White Labs, Wyeast, Fermentis, Lallemand, Imperial, Omega, etc.) displaying product code, brand trade name, and Liquid/Dry badge.
6. **Fermentation & Pitching Dynamics Card**:
   - Narrative guidance on lag time, attenuation pace, oxygenation, and nutrient requirements.
7. **Interchangeable Substitute Strains Card**:
   - Clickable cards of alternative yeast strains with matching performance. Tapping navigates to that strain's detail.
8. **Associated Beverage Styles Card**:
   - Clickable cards showing Beer, Mead, or Cider styles linked to this strain. Tapping navigates to `style_detail/{id}/{guideline}`.

### 5. Cross-Cutting Integrations
- [ ] **Navigation Drawer**: Add `Yeast` destination in `AppDrawer` with route `"yeasts"`, label `"Yeast"`, and icon `Icons.Default.BubbleChart`.
- [ ] **Style Detail Screen (`StyleDetailScreen`)**: Add `CollapsibleCommonYeastsCard` displaying associated yeast strains with tap-through navigation to `YeastDetailScreen`.

---

## 🧪 Verification & Testing Strategy
- **Unit Tests**:
  - `YeastRepositoryTest`: Test FTS multi-word query sanitization, commercial code search matching, filtering by classification/flocculation/attenuation/ABV, substitute resolution, and multi-guideline style mapping.
  - `TemperatureConverterTest`: Test °C to °F and °F to °C conversions, range formatting, and preference handling.
  - `YeastListViewModelTest` & `YeastDetailViewModelTest`: Verify state emissions, filter application, and substitute navigation.
- **Database Migration Test**:
  - Verify migration from Version 8 to Version 9 preserves existing styles, hops, malts, honeys, pomes, and user favorites.
- **Instrumented / Screenshot Tests**:
  - Verify `YeastsScreen` rendering, filter sheet behavior, and `YeastDetailScreen` layout on mobile devices.
