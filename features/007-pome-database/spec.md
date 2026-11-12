# Feature Spec: 007 Pome Varieties Database & Sensory Profiles

---

## 🎯 High-Level Goal
Provide an offline-first, visually rich reference database of canonical cider apple and perry pear varieties (collectively **Pomes**) with multi-attribute search and filtering, interactive Long Ashton 2×2 matrix gauges, 8-axis sensory radar visualizations, pomological and pressing chemistry (acidity %, tannin %, SG range, and perry sorbitol), and bidirectional relational cross-referencing between pome varieties and BJCP Cider and Mead styles.

---

## 👤 User Story
**As a** BJCP Cider Judge, Cidermaker, or Brewer  
**I want to** look up cider apple and perry pear varieties, inspect their Long Ashton classifications (*Bittersweet*, *Bittersharp*, *Sharp*, *Sweet*), Specific Gravity potential, acidity and tannin percentages, perry sorbitol levels, pressing behavior, and 8-axis sensory flavor radar profiles, and discover which cider styles they correspond to  
**So that** I accurately evaluate fruit character in competition judging, formulate balanced single-varietal and blended cider/perry recipes, and explore interchangeable variety substitutes.

---

## 📚 Domain & Architecture References
- **Domain Vocabulary**: Strictly adhere to `GLOSSARY.md` (`Pome Variety`, `Pome Classification`, `Fruit Use`, `Bittersweet`, `Bittersharp`, `Sharp`, `Sweet`, `Long Ashton Matrix Gauge`, `Sorbitol`, `Pome Sensory Radar Chart`, `Pome Substitute`, `Cider Style`).
- **Architectural Decisions**:
  - `docs/adr/0001-relational-hop-style-cross-reference.md`: Dedicated Room relational join table for sub-millisecond bidirectional queries between ingredients and styles.
  - `docs/adr/0002-canonical-malt-varieties.md`: Canonical ingredient varieties focused on science and style relationships rather than commercial brand SKUs.
  - `docs/adr/0004-canonical-honey-varieties-pfund-sensory-model.md`: Canonical ingredient modeling with custom sensory radar and metric converters.
  - `docs/adr/0005-canonical-pome-varieties-long-ashton-sensory-model.md`: Canonical pome varieties with Long Ashton 4-tier classification, 2×2 matrix gauge, and 8-axis sensory model.
- **Performance Standards**: Follow `GEMINI.md` (Compose `@Immutable` models, `flowOn(Dispatchers.Default)`, SQLite FTS4 subquery indexing, asset preloading via `DatabaseInitializer`).

---

## 📋 Requirements & Acceptance Criteria

### 1. Data Pipeline & Persistence
- [ ] **Bundled Asset (`pomes.json`)**: Curate and bundle `app/src/main/assets/pomes.json` (~40–45 canonical varieties across English, French, American Heritage & Culinary, and Perry Pears) containing:
  - `id`, `name`, `fruitType` (`Apple` vs. `Pear`)
  - `classification` (`Bittersweet`, `Bittersharp`, `Sharp`, `Sweet`)
  - `fruitUse` (`Heritage Cider/Perry` vs. `Culinary / Multi-Purpose`)
  - `botanicalName` (*Malus domestica* or *Pyrus communis*)
  - `origin`, `countryCode` (e.g. `GB`, `FR`, `US`, etc.)
  - `acidityMalicPercent` (numeric % titratable acid, e.g. 0.22% – 1.10%)
  - `tanninPolyphenolPercent` (numeric % tannins, e.g. 0.12% – 0.55%)
  - `tanninLevel` (`Low`, `Medium`, `High`)
  - `sgMin`, `sgMax` (Specific Gravity range, e.g. 1.050 – 1.065)
  - `harvestSeason` (`Early`, `Mid`, `Late`)
  - `sorbitolGramsPerLiter` (numeric g/L, e.g. 25.0 – 45.0 for perry pears; `null` for apples)
  - `description` (narrative pomological and historical background)
  - `blendingGuidance` (recommendations for single-varietal cider vs. blending role for tannins/acid)
  - `pressingCharacteristics` (milling timing, pectin levels, oxidation tendency, juice yield)
  - `descriptors` (flavor and aroma notes: crisp green apple, quince, leather, spice, citrus, honey, astringent tannin)
  - `substituteIds` (interchangeable alternative pome variety IDs)
  - `commonStyleIds` (associated BJCP Cider styles: e.g. `C1A`, `C1B`, `C1C`, `C1D`, `C1E`, `C2A`, `C2C`, `M2B`)
  - `radarProfile` (8 organoleptic dimensions: Acidity/Sharpness, Tannin/Bitterness, Fresh Fruit, Baked/Ripe Fruit, Citrus & Tropical, Floral & Herbal, Earthy & Farmhouse, Sweet & Confectionary rated 0.0 – 5.0)
- [ ] **Room Database Version 8**:
  - `PomeVarietyEntity`: Primary table storing complete pome fruit attributes.
  - `PomeVarietyFtsEntity`: SQLite FTS4 virtual table indexing `name`, `botanicalName`, `origin`, `description`, `blendingGuidance`, `pressingCharacteristics`, and `descriptors` for sub-millisecond full-text search.
  - `PomeStyleCrossRef`: Relational join table keyed on `(pomeId, styleId, guidelineType)` with composite index for bidirectional style queries.
- [ ] **Room Database Migration (v7 -> v8)**: Automated migration creating new tables and FTS virtual tables without losing existing style/hop/malt/honey/favorites data.
- [ ] **Database Preloading**: `DatabaseInitializer` seeds Room tables from `pomes.json` on startup in the background without main-thread blocking.
- [ ] **Repository Layer (`PomeRepository`)**: Expose distinct flows for catalog search/filtering, individual pome details, and style cross-references, dispatched on `Dispatchers.Default`.

### 2. Pome Catalog List Screen (`PomesScreen` / route: `"pomes"`)
- [ ] **Top App Bar**: TopAppBar with title `"Pomes"`, drawer menu navigation icon, and top-right filter action button (`Icons.Default.FilterList`) with active filter tinting (`MaterialTheme.colorScheme.primary` when active).
- [ ] **Borderless Pill Search Bar**: Single-line search container with leading search icon, clear button (visible only when query is non-blank), and IME search action without placeholder hint text. Focus auto-clears on list scroll or background tap.
- [ ] **Quick-Filter Strip**: Horizontal scrollable chips for **Fruit Type** and **Classification** (toggle on/off without 'All' chip):
  - `Apple`, `Pear`
  - `Bittersweet`, `Bittersharp`, `Sharp`, `Sweet`
- [ ] **Advanced Filter Sheet (`PomeFilterSheet`)**: Bottom modal sheet with top header row containing `"Filter Pomes"` title and top-right `"Reset All"` text button, plus full-width `"Apply Filters"` button at the bottom. Options ordered as:
  1. **Fruit Type** (toggle chips: Apple, Pear)
  2. **Classification** (toggle chips: Bittersweet, Bittersharp, Sharp, Sweet)
  3. **Harvest Season** (toggle chips: Early, Mid, Late)
  4. **Tannin Level** (toggle chips: Low, Medium, High)
- [ ] **Glanceable `PomeCard` Items**:
  - Standardized `RoundedCornerShape(16.dp)` card with subtle elevation and container tint.
  - Variety name with bold typography.
  - Botanical name (*italicized Latin name*).
  - Origin country flag & name tag (🇬🇧 UK, 🇫🇷 FR, 🇺🇸 US, etc.).
  - Fruit Type badge (`Apple` / `Pear`).
  - Long Ashton classification pill badge (`Bittersweet`, `Bittersharp`, `Sharp`, `Sweet`).
  - Specific Gravity range pill badge (e.g. `1.050–1.062 SG`).
  - Acidity & Tannin indicators (e.g. `Acid: 0.28% • Tannin: High (0.35%)`).
  - Top 3 flavor descriptors.
  - Standard card clickability to detail without trailing navigation arrow.
- [ ] **Zero-Result Filter Recovery**: Centered empty state with orchard tree icon (`Icons.Default.Nature`), bold headline `"No pomes found"`, subtitle `"Try adjusting your search query or filters."`, and single-tap primary `"Reset Filters"` action button.

### 3. Pome Detail Screen (`PomeDetailScreen` / `pome_detail/{pomeId}`)
Ordered precisely according to resolved design hierarchy using standardized `RoundedCornerShape(16.dp)` cards:
1. **Hero Header**:
   - Variety name, botanical name, origin country flag and name tag, Fruit Type badge, Long Ashton classification pill badge.
   - Narrative overview description.
2. **Long Ashton Matrix Gauge**:
   - Visual 2×2 quadrant gauge showing where the variety plots relative to the 0.45% acidity and 0.20% tannin boundary lines.
   - Exact numerical Acidity (% malic) and Tannin (% polyphenols) badges.
3. **Organoleptic Flavor Profile Section**:
   - 8-axis **Pome Sensory Radar Chart** (Acidity/Sharpness, Tannin/Bitterness, Fresh Fruit, Baked/Ripe Fruit, Citrus & Tropical, Floral & Herbal, Earthy & Farmhouse, Sweet & Confectionary).
   - Flavor & aroma descriptor chip cloud.
4. **Pomological & Chemical Specs Card**:
   - **Harvest Season**: Badge (`Early`, `Mid`, `Late`).
   - **Sugar Potential**: Specific Gravity range badge (e.g. `1.050–1.062 SG`).
   - **Tannin Level**: Badge (`Low`, `Medium`, `High`).
   - **Sorbitol Content** (for Pears): Badge showing sorbitol concentration (e.g. `~32 g/L`) with explanation dialog explaining its unfermentable residual sweetness in perry.
5. **Cidermaking & Pressing Dynamics Section**:
   - Comprehensive narrative on blending role (single-varietal vs. acid/tannin backbone), milling/pressing timing, oxidation tendency, and pectin behavior.
6. **Pome Substitutes Section**:
   - Chips displaying interchangeable alternative varieties with similar classification and profile. Tapping navigates directly to that variety's detail screen.
7. **Associated Cider & Beverage Styles Section**:
   - Clickable cards showing cider (and mead cyser) styles where this variety is traditionally or ideally featured (e.g., Dabinett in C1B English Cider, Kingston Black in C1A/C1B, Thorn in C1E Traditional Perry). Tapping navigates directly to `style_detail/{id}/{guideline}`.

### 4. Navigation Drawer & Icon Updates
- [ ] **Pomes Navigation Item**: Added under ingredients in `AppDrawer` with label `"Pomes"`, route `"pomes"`, using `Icons.Default.Nature`.
- [ ] **Malt Navigation Item**: Update icon from `Icons.Default.Grain` to `Icons.Default.Grass` (representing wheat/grain stalks).
- [ ] **Style Detail Screen**: When viewing a Cider style (or Mead Cyser M2B), display an **Associated Pomes** section listing linked variety cards navigating to their pome details.
