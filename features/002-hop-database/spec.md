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
- [ ] **Bundled Asset (`hops.json`)**: Curate and bundle `app/src/main/assets/hops.json` (~80–100 prominent global cultivars) containing IDs, names, country/origin, purpose, alpha/beta acid ranges, sensory categories, aroma descriptors, descriptions, parent hop IDs, substitute IDs, common style IDs, and optional 8-axis radar profiles.
- [ ] **Room Database Version 3**:
  - `HopVarietyEntity`: Primary table storing complete hop metadata.
  - `HopVarietyFtsEntity`: SQLite FTS4 table for sub-millisecond full-text search over names, origins, descriptors, and descriptions.
  - `HopStyleCrossRef`: Many-to-many relational join table keyed on `(hopId, styleId, guidelineType)` for bidirectional querying.
- [ ] **Database Preloading**: `DatabaseInitializer` seeds Room tables from `hops.json` on first launch without main-thread blocking.
- [ ] **Repository Layer (`HopRepository`)**: Expose distinct flows for catalog search/filtering, individual hop details, dynamic child descendants (`getChildHops`), and style cross-references, dispatched on `Dispatchers.Default`.

### 2. Hop Catalog List Screen (`HopsScreen`)
- [ ] **Borderless Pill Search Bar**: Single-line search container with search icon, clear button, and IME search action. Focus auto-clears on list scroll or background tap.
- [ ] **Quick-Filter Strip**: Horizontal scrollable chips for **Purpose** (`All`, `Aroma`, `Bittering`, `Dual-Purpose`) and **Sensory Categories** (`Citrus`, `Tropical Fruit`, `Floral`, `Herbal`, `Spicy`, `Pine/Resinous`, `Earthy/Woody`).
- [ ] **Advanced Filter Sheet**: Bottom modal sheet with:
  - Alpha Acid % range slider (0% to 20%).
  - Origin country multi-select chips.
  - Purpose selection chips.
  - Common Beer Styles multi-select based on **Base Style Categories** (e.g. Czech Lager, IPA, English Bitter).
- [ ] **Glanceable `HopCard` Items**:
  - Hop name with bold typography.
  - Origin country tag/flag (🇺🇸 US, 🇩🇪 DE, 🇨🇿 CZ, 🇳🇿 NZ, 🇬🇧 UK, etc.).
  - Color-coded Purpose pill badge (Aroma = teal, Bittering = amber, Dual-Purpose = violet).
  - Alpha Acid range badge (`AA: 11.0–13.0%`).
  - Top 3 sensory descriptor chips.
  - Chevron indicator indicating clickable navigation to detail.
- [ ] **Zero-Result Filter Recovery**: Friendly empty state with a single-tap "Reset Filters" action button when active queries return 0 results.

### 3. Hop Detail Screen (`HopDetailScreen` / `hop_detail/{hopId}`)
- [ ] **Acid Range Gauges**: Custom horizontal graphical range meters for Alpha Acid (warm amber gradient) and Beta Acid (sage green gradient) highlighting the min-max span against a standard scale.
- [ ] **8-Axis Sensory Radar Chart**: Custom Jetpack Compose `Canvas` radial polygon plotting quantitative intensity (0.0–5.0) across *Citrus, Tropical, Floral, Herbal, Spicy, Piney, Earthy, Grassy*. Semi-transparent fill and labeled vertices.
- [ ] **Graceful Radar Fallback**: When numeric radar profile data is absent for a hop, hide the radar chart and display sensory category chips alongside the full aroma descriptor cloud.
- [ ] **Lineage & Genetics Section**: Displays parent hop chips and dynamically resolved child descendant chips. Tapping any lineage chip navigates directly to that hop's detail screen.
- [ ] **Hop Substitutes Section**: Displays alternative hop chips with comparable brewing profiles. Tapping any substitute chip navigates directly to that hop's profile.
- [ ] **Common Beer Styles Section**: Displays clickable style cards associated with the hop. Tapping a style navigates directly to `style_detail/{id}/{guideline}`.

### 4. Bidirectional Style Guide Integration (`StyleDetailScreen`)
- [ ] **Collapsible "Common Hops" Section**: On `StyleDetailScreen`, add a card titled `"Common Hops (N)"` below the guideline sensory sections.
- [ ] **Collapsed by Default**: Starts collapsed by default to preserve the clean BJCP/BA guideline reading flow.
- [ ] **Deep Linking**: Expanding the card displays interactive hop chips with purpose tags. Tapping any hop chip navigates directly to `hop_detail/{hopId}`.

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
