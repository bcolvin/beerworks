# Feature Spec: 003 Malt Profile Database & Flavor Characteristics

---

## 🎯 High-Level Goal
Provide an offline-first, visually rich reference database of canonical brewing malt varieties and fermentables with multi-attribute search and filtering, custom 8-axis malt sensory radar visualizations, educational diastatic power conversion tools, commercial brand/maltster examples, and bidirectional relational cross-referencing between malt varieties and beer style guidelines.

---

## 👤 User Story
**As a** BJCP Beer Judge or Homebrewer  
**I want to** look up brewing malts, inspect their color ratings (°L/EBC), extract potential, and diastatic power, explore their 8-axis flavor radar profiles, understand their typical usage and recipe roles, and see which commercial brands and beer styles they correspond to  
**So that** I understand the grain bill foundation, evaluate malt character in beer judging, and formulate accurate grist bills.

---

## 📚 Domain & Architecture References
- **Domain Vocabulary**: Strictly adhere to `GLOSSARY.md` (`Malt Variety`, `Malt Category`, `Diastatic Power`, `Extract Potential`, `Typical Usage`, `Functional Usage Tag`, `Malt Sensory Radar Chart`, `Malt Substitute`, `Commercial Malts`).
- **Architectural Decisions**:
  - `docs/adr/0001-relational-hop-style-cross-reference.md`: Dedicated Room relational join table for sub-millisecond bidirectional queries between ingredients and beer styles.
  - `docs/adr/0002-canonical-malt-varieties.md`: Canonical brewing grain varieties with embedded commercial brand/maltster examples full-text indexed for search.
- **Performance Standards**: Follow `GEMINI.md` (Compose `@Immutable` models, `flowOn(Dispatchers.Default)`, SQLite FTS4 subquery indexing, asset preloading via `DatabaseInitializer`).

---

## 📋 Requirements & Acceptance Criteria

### 1. Data Pipeline & Persistence
- [ ] **Bundled Asset (`malts.json`)**: Curate and bundle `app/src/main/assets/malts.json` (~70–90 prominent canonical malts across American, British, German, Belgian, and global traditions) containing:
  - `id`, `name`, `category` (Base Malt, Kilned & Toasted, Caramel & Crystal, Roasted & Dark, Adjunct & Raw Grain)
  - `grainType` (Barley, Wheat, Rye, Oats, Corn, Rice, Other)
  - `origin`, `countryCode`
  - `colorMin`, `colorMax` (in Degrees Lovibond °L)
  - `diastaticPowerLintner` (numeric °Lintner)
  - `extractPotentialDbfg` (DBFG percentage, e.g. 80.5)
  - `maxGristPercentage` (recommended max % in grist, e.g. 100%, 15%, 5%)
  - `description` (historical and malting overview)
  - `typicalUsage` (descriptive narrative of recipe roles)
  - `functionalTags` (standardized functional tags: `Base Grist`, `Head Retention`, `Body & Mouthfeel`, `Color Adjustment`, `Caramel Sweetness`, `Toasty Aroma`, `Roast & Coffee`, `Haze & Cloudiness`)
  - `descriptors` (flavor/aroma notes)
  - `commercialExamples` (notable brand names: Weyermann, Dingemans, Briess, Crisp, Simpsons, Bairds, etc.)
  - `substituteIds` (interchangeable alternative malt IDs)
  - `commonStyleIds` (associated BJCP/BA beer style IDs)
  - `radarProfile` (8 organoleptic dimensions: Malty/Sweet, Bready/Biscuit, Toasty, Caramel/Toffee, Nutty, Chocolate, Roast/Coffee, Grainy/Husky)
- [ ] **Room Database Version 4**:
  - `MaltVarietyEntity`: Primary table storing complete malt attributes.
  - `MaltVarietyFtsEntity`: SQLite FTS4 virtual table indexing `name`, `origin`, `description`, `typicalUsage`, `functionalTags`, `descriptors`, and `commercialExamples` for sub-millisecond full-text queries.
  - `MaltStyleCrossRef`: Relational join table keyed on `(maltId, styleId, guidelineType)` with composite index for bidirectional style queries.
- [ ] **Room Database Migration (v3 -> v4)**: Automated migration creating new tables and FTS virtual tables without losing existing style/hop/favorites data.
- [ ] **Database Preloading**: `DatabaseInitializer` seeds Room tables from `malts.json` on startup in the background without main-thread blocking.
- [ ] **Repository Layer (`MaltRepository`)**: Expose distinct flows for catalog search/filtering, individual malt details, and style cross-references, dispatched on `Dispatchers.Default`.

### 2. Malt Catalog List Screen (`MaltScreen`)
- [ ] **Uniform Layout & Polish**: Replicate the responsive pattern established by `HopsScreen`.
- [ ] **Borderless Pill Search Bar**: Single-line search container with search icon, clear button, and IME action. Matches against malt name, origin, maltster brands, descriptors, and functional tags. Focus auto-clears on list scroll.
- [ ] **Quick-Filter Strip**: Horizontal scrollable chips for **Malt Category**:
  - `All`, `Base Malt`, `Kilned & Toasted`, `Caramel & Crystal`, `Roasted & Dark`, `Adjunct`
- [ ] **Advanced Filter Sheet (`MaltFilterSheet`)**: Bottom modal sheet with active filter badge and reset action, ordered as:
  1. **Malt Category** (multi-select chips)
  2. **Grain Type** (multi-select: Barley, Wheat, Rye, Oats, Corn, Rice, Other)
  3. **Diastatic Power** (chips: All, High [≥100°L], Moderate [30–99°L], None [0°L])
  4. **Color Range** (segmented Lovibond tiers: Pale <10°L, Amber 10–50°L, Brown 50–150°L, Dark >150°L)
  5. **Functional Usage Tags** (multi-select chips: Base Grist, Head Retention, Body & Mouthfeel, etc.)
  6. **Associated Base Beer Styles** (multi-select based on base style groups)
- [ ] **Glanceable `MaltCard` Items**:
  - Malt name with bold typography.
  - Origin country flag & name tag (🇺🇸 US, 🇩🇪 DE, 🇬🇧 UK, 🇧🇪 BE, etc.).
  - Malt Category pill badge (color-coded).
  - Grain type chip (Wheat, Rye, Oats, etc., if non-barley).
  - Color swatch pill with Lovibond range (e.g. `1.8 – 2.5 °L`) rendered via `SrmColorConverter`.
  - Diastatic Power badge (`High`, `Moderate`, or `None`).
  - Top 3 functional tags / flavor descriptors.
  - Chevron indicator indicating clickable navigation to detail.
- [ ] **Zero-Result Filter Recovery**: Empty state with friendly copy and single-tap "Reset Filters" action button.

### 3. Malt Detail Screen (`MaltDetailScreen` / `malt_detail/{maltId}`)
Ordered precisely according to resolved design hierarchy:
1. **Hero Header**:
   - Malt name, origin country flag and name tag, Malt Category pill badge, grain type chip.
   - Narrative overview description.
2. **Typical Brewing Usage Section**:
   - Comprehensive narrative on recipe roles, mash performance, and grist limits.
   - Standardized functional usage tags as pill chips (`Base Grist`, `Head Retention`, etc.).
3. **Organoleptic Flavor Profile Section**:
   - 8-axis **Malt Sensory Radar Chart** (Malty/Sweet, Bready/Biscuit, Toasty, Caramel/Toffee, Nutty, Chocolate, Roast/Coffee, Grainy/Husky).
   - Graceful fallback: when numeric radar data is absent, display full flavor descriptor chip cloud.
4. **Technical Brewing Specs Card**:
   - **Color Rating**: Dynamic Lovibond swatch, displaying both `°L` and calculated `EBC` (EBC ≈ 1.97 × °L).
   - **Extract Potential**: Dry Basis Fine Grind (DBFG %) badge.
   - **Max Recommended Grist %**: Meter / badge displaying max percentage in recipe.
   - **Diastatic Power**: Badge showing conversion tier (`High`, `Moderate`, or `None`) with an official **Info icon (`Icons.Default.Info`)**. Tapping the info icon displays an educational dialog explaining Degrees Lintner (°L), enzyme self-conversion rules (≥ 30–35 °L to convert self; ≥ 100 °L to convert unmalted adjuncts), and exact numeric value.
5. **Malt Substitutes Section**:
   - Chips displaying interchangeable alternative grains with comparable color/flavor. Tapping navigates to that malt's detail screen.
6. **Notable Maltsters & Commercial Examples Section**:
   - Card listing representative commercial brands (Weyermann, Dingemans, Briess, Crisp, Simpsons, Castle, etc.).
7. **Associated Beer Styles Section**:
   - Clickable beer style cards where this malt is a signature ingredient. Tapping navigates directly to `style_detail/{id}/{guideline}`.

### 4. Bidirectional Style Guide Integration (`StyleDetailScreen`)
- [ ] **Collapsible "Common Malts & Grains" Section**: On `StyleDetailScreen`, add an expansion card titled `"Common Malts & Grains (N)"` below guidelines and next to "Common Hops".
- [ ] **Collapsed by Default**: Starts collapsed to preserve reading flow.
- [ ] **Deep Linking**: Expanding displays malt chips with color swatches and category tags. Tapping any malt navigates directly to `malt_detail/{maltId}`.

### 5. Reusable Component Generalization
- [ ] **Generalized `SensoryRadarChart`**: Refactor `SensoryRadarChart.kt` to accept generic dimension labels, normalized values (0.0 to 5.0), and configurable theme colors (warm malt amber/copper for malts, herbal green for hops) to eliminate duplication.

---

## 📱 User Interface & Visuals
- **MaltScreen**: Top borderless search bar + horizontal category quick-filter row + lazy column of `MaltCard`s.
- **MaltDetailScreen**: Clean vertical scrolling layout following the 1 -> 3 -> 4 -> 2 -> 6 -> 5 -> 7 section order.
- **StyleDetailScreen**: Paired collapsible "Common Hops" and "Common Malts & Grains" cards.

---

## 📦 Assets & Attachments
- `app/src/main/assets/malts.json` (~70–90 curated global malt varieties).
- Room migration: Database version 3 -> 4.
