# Feature Spec: 006 Honey Varieties Database & Sensory Profiles

---

## 🎯 High-Level Goal
Provide an offline-first, visually rich reference database of canonical varietal honeys and fermentables with multi-attribute search and filtering, custom 8-axis honey sensory radar visualizations, optical Pfund/USDA color standards, sugar crystallization and fermentation metrics, and bidirectional relational cross-referencing between honey varieties and BJCP Mead, Cider, and Beer style guidelines.

---

## 👤 User Story
**As a** BJCP Mead Judge, Home Meadmaker, or Craft Brewer  
**I want to** look up varietal honeys, inspect their botanical sources, Pfund color scales (mm) and USDA color classifications, crystallization tendencies, moisture content, and 8-axis sensory flavor radar profiles, understand their fermentation characteristics and delicate aroma retention, and discover which mead styles and substitutes they correspond to  
**So that** I accurately evaluate honey character in competition judging, select optimal varietals for traditional meads, melomels, metheglins, and braggots, and formulate balanced recipes.

---

## 📚 Domain & Architecture References
- **Domain Vocabulary**: Strictly adhere to `GLOSSARY.md` (`Honey Variety`, `Floral Classification`, `Pfund Scale`, `USDA Honey Color Classification`, `Crystallization Tendency`, `Honey Sensory Radar Chart`, `Honey Substitute`, `Mead Style`).
- **Architectural Decisions**:
  - `docs/adr/0001-relational-hop-style-cross-reference.md`: Dedicated Room relational join table for sub-millisecond bidirectional queries between ingredients and styles.
  - `docs/adr/0002-canonical-malt-varieties.md`: Canonical ingredient varieties focused on science and style relationships rather than commercial apiary brand SKUs.
  - `docs/adr/0004-canonical-honey-varieties-pfund-sensory-model.md`: Canonical honey varieties with optical Pfund scale, USDA color classifications, and 8-axis sensory radar.
- **Performance Standards**: Follow `GEMINI.md` (Compose `@Immutable` models, `flowOn(Dispatchers.Default)`, SQLite FTS4 subquery indexing, asset preloading via `DatabaseInitializer`).

---

## 📋 Requirements & Acceptance Criteria

### 1. Data Pipeline & Persistence
- [x] **Bundled Asset (`honeys.json`)**: Curate and bundle `app/src/main/assets/honeys.json` (~35–45 canonical honey varieties across North American, European, Oceanic, and global origins) containing:
  - `id`, `name`, `floralClassification` (`Monofloral`, `Polyfloral`, `Honeydew`)
  - `botanicalSource` (Latin genus/species, e.g. *Citrus sinensis*, *Trifolium repens*, *Nyssa ogeche*, *Limnanthes alba*, *Fagopyrum esculentum*)
  - `origin`, `countryCode` (e.g. `US`, `FR`, `NZ`, `ES`, `CA`, `DE`, `AU`)
  - `pfundMin`, `pfundMax` (optical density in millimeters, 1–140 mm)
  - `usdaColor` (`Water White`, `Extra White`, `White`, `Extra Light Amber`, `Light Amber`, `Amber`, `Dark Amber`)
  - `moistureTypicalPercent` (numeric %, typically 15.5% – 19.0%)
  - `fgRatio` (typical Fructose-to-Glucose ratio, e.g. 1.05 – 1.45)
  - `crystallizationTendency` (`Slow`, `Moderate`, `Rapid`)
  - `extractPotentialSg` (typical specific gravity yield per lb/gal, e.g. 1.035 – 1.038)
  - `flavorIntensity` (`Delicate`, `Moderate`, `Robust`)
  - `description` (narrative botanical and regional background)
  - `fermentationBehavior` (fermentation dynamics, nutrient demand, volatile aroma retention, heat sensitivity)
  - `pairingNotes` (recommended fruit, spice, or oak pairings for melomels/metheglins)
  - `descriptors` (flavor and aroma notes: citrus blossom, marshmallow, earthy, barnyard, herbal, caramel)
  - `substituteIds` (interchangeable alternative honey IDs with similar profile)
  - `commonStyleIds` (associated BJCP Mead styles: e.g. `M1A`, `M1B`, `M1C`, `M2A`, `M2E`, `M3A`, `M4A`, `C2B`)
  - `radarProfile` (8 organoleptic dimensions: Floral, Fruity, Herbal/Fresh, Spicy/Warm, Sweet/Confectionary, Nutty/Woody, Earthy/Mineral, Animal/Pungent rated 0.0 – 5.0)
- [x] **Room Database Version 7**:
  - `HoneyVarietyEntity`: Primary table storing complete honey attributes.
  - `HoneyVarietyFtsEntity`: SQLite FTS4 virtual table indexing `name`, `botanicalSource`, `origin`, `description`, `fermentationBehavior`, `descriptors`, and `pairingNotes` for sub-millisecond full-text search.
  - `HoneyStyleCrossRef`: Relational join table keyed on `(honeyId, styleId, guidelineType)` with composite index for bidirectional style queries.
- [x] **Room Database Migration (v6 -> v7)**: Automated migration creating new tables and FTS virtual tables without losing existing style/hop/malt/favorites data.
- [x] **Database Preloading**: `DatabaseInitializer` seeds Room tables from `honeys.json` on startup in the background without main-thread blocking.
- [x] **Repository Layer (`HoneyRepository`)**: Expose distinct flows for catalog search/filtering, individual honey details, and style cross-references, dispatched on `Dispatchers.Default`.

### 2. Honey Catalog List Screen (`HoneyScreen` / route: `"honey"`)
- [x] **Uniform Layout & Polish**: Replicate the responsive pattern established by `HopsScreen` and `MaltScreen`.
- [x] **Top App Bar**: TopAppBar with title `"Honey"`, drawer menu navigation icon, and top-right filter action button (`Icons.Default.FilterList`) with active filter tinting (`MaterialTheme.colorScheme.primary` when active).
- [x] **Borderless Pill Search Bar**: Single-line search container with leading search icon, clear button (visible only when query is non-blank), and IME search action without placeholder hint text. Focus auto-clears on list scroll or background tap.
- [x] **Quick-Filter Strip**: Horizontal scrollable chips for **Floral Classification** (toggle on/off without 'All' chip):
  - `Monofloral`, `Polyfloral`, `Honeydew`
- [x] **Advanced Filter Sheet (`HoneyFilterSheet`)**: Bottom modal sheet with top header row containing `"Filter Honeys"` title and top-right `"Reset All"` text button, plus full-width `"Apply Filters"` button at the bottom. Options ordered as:
  1. **Floral Classification** (toggle chips: Monofloral, Polyfloral, Honeydew)
  2. **Color** (multi-select chips: Water White, Extra White, White, Extra Light Amber, Light Amber, Amber, Dark Amber)
  3. **Flavor Intensity** (toggle chips: Delicate, Moderate, Robust)
  4. **Crystallization Tendency** (toggle chips: Slow, Moderate, Rapid)
- [x] **Glanceable `HoneyCard` Items**:
  - Standardized `RoundedCornerShape(16.dp)` card with subtle elevation and container tint.
  - Honey name with bold typography.
  - Botanical source (*italicized Latin name*).
  - Origin country flag & name tag (🇺🇸 US, 🇳🇿 NZ, 🇫🇷 FR, 🇩🇪 DE, etc.).
  - Floral classification pill badge.
  - Color swatch pill with Pfund range and USDA color tier (e.g., `15–30 mm (White)`).
  - Flavor Intensity indicator badge (`Delicate`, `Moderate`, or `Robust`).
  - Top 3 flavor descriptors.
  - Standard card clickability to detail without trailing navigation arrow.
- [x] **Zero-Result Filter Recovery**: Centered empty state with honeycomb icon (`Icons.Default.Hive`), bold headline `"No honeys found"`, subtitle `"Try adjusting your search query or filters."`, and single-tap primary `"Reset Filters"` action button.

### 3. Honey Detail Screen (`HoneyDetailScreen` / `honey_detail/{honeyId}`)
Ordered precisely according to resolved design hierarchy using standardized `RoundedCornerShape(16.dp)` cards:
1. **Hero Header**:
   - Honey name, botanical source, origin country flag and name tag, floral classification pill badge.
   - Narrative overview description.
2. **Organoleptic Flavor Profile Section**:
   - 8-axis **Honey Sensory Radar Chart** (Floral, Fruity, Herbal & Fresh, Spicy & Warm, Sweet & Confectionary, Nutty & Woody, Earthy & Mineral, Animal & Pungent).
   - Flavor intensity rating badge (`Delicate`, `Moderate`, or `Robust`).
   - Flavor & aroma descriptor chip cloud.
3. **Technical Specs & Sugar Profile Card**:
   - **Pfund & USDA Color**: Dynamic visual color swatch displaying Pfund millimeter range (e.g. `28 – 35 mm`) and USDA tier badge (`White` / `Extra Light Amber`).
   - **Moisture Content**: Percentage badge (e.g. `17.2%`) with quality indicator (< 18.6% Grade A).
   - **Extract Potential**: Specific gravity yield badge (e.g. `~1.037 SG`).
   - **Crystallization Tendency**: Badge showing granulation propensity (`Slow`, `Moderate`, or `Rapid`) with an official **Info icon (`Icons.Default.Info`)**. Tapping displays an educational dialog explaining the Fructose/Glucose ratio (F/G > 1.3 resists granulation like Tupelo/Sage; F/G < 1.1 granulates rapidly like Clover/Canola/Dandelion).
4. **Fermentation & Recipe Guidance Section**:
   - Comprehensive narrative on fermentation behavior, nutrient requirements (YAN management in mead), delicate floral volatile preservation (cold fermentation vs. warmth), and heat sensitivity (avoid boiling varietal honey).
   - Recommended fruit, spice, and wood pairings for melomels, metheglins, and braggots.
5. **Honey Substitutes Section**:
   - Chips displaying interchangeable alternative varietal honeys with similar flavor/aroma profile. Tapping navigates directly to that honey's detail screen.
6. **Associated Mead & Beverage Styles Section**:
   - Clickable cards showing mead (and cider/beer) styles where this honey is traditionally or ideally featured (e.g., Orange Blossom in M1A Dry Mead / M1B Semi-Sweet Mead, Buckwheat in M1C Sweet Mead / M4A Braggot). Tapping navigates directly to `style_detail/{id}/{guideline}`.

### 4. Bidirectional Style Guide Integration (`StyleDetailScreen`)
- [x] **Collapsible "Recommended Honeys" Section**: On `StyleDetailScreen`, add an expansion card titled `"Recommended Honeys (N)"` below guidelines for Mead styles (and applicable Cider/Beer styles such as Braggots).
- [x] **Collapsed by Default**: Starts collapsed to preserve reading flow.
- [x] **Deep Linking**: Expanding displays honey chips with color swatches and classification tags. Tapping any honey navigates directly to `honey_detail/{honeyId}`.

### 5. Navigation Drawer Integration (`AppDrawer`)
- [x] **Drawer Destination**: Add `DrawerDestination.Honey("honey", "Honey", Icons.Default.Hive)` between `Malt` and `ReviewHistory` in `AppDrawer.kt`.
- [x] **Navigation Handling**: Update `MainActivity.kt` with composable routes for `"honey"` and `"honey_detail/{honeyId}"`.

---

## 📱 User Interface & Visuals
- **HoneyScreen**: Top borderless search bar + horizontal classification quick-filter row + lazy column of `HoneyCard` items.
- **HoneyDetailScreen**: Clean vertical scrolling layout following Hero -> Sensory Radar -> Technical Specs -> Fermentation Guidance -> Substitutes -> Associated Styles.
- **StyleDetailScreen**: Collapsible "Recommended Honeys" expansion card with deep links.
- **AppDrawer**: Navigation item with honeycomb glyph (`Icons.Default.Hive`).

---

## 📦 Assets & Attachments
- `app/src/main/assets/honeys.json` (~35–45 curated canonical varietal honeys).
- Room migration: Database version 6 -> 7.
