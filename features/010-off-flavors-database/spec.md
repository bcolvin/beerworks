# Feature Spec: 010 Off-Flavors & Sensory Faults Database

---

## 🎯 High-Level Goal
Provide an offline-first, judging-focused reference catalog of **Off-Flavors** (accessible in the navigation drawer directly above **Favorites**) distilled from Thomas Barnes' *Complete Beer Fault Guide v1.4* and BJCP sensory standards into concise, scannable cards and detail pages with chemical thresholds, sensory modality breakdowns, diagnostic "How to Tell Apart" contrast notes, root causes, actionable scoresheet remedies, and one-way style cross-references.

---

## 👤 User Story
**As a** Beer, Mead, or Cider Judge or Homebrewer  
**I want to** quickly look up an off-flavor or sensory fault by aroma/flavor/mouthfeel cue, chemical compound, or root-cause category, see how to tell it apart from normal beverage flavors or lookalike faults, and review its origins and remedies  
**So that** I can accurately identify sensory faults at the judging table and write clear, constructive troubleshooting feedback on competition scoresheets without wading through verbose academic text.

---

## 📚 Domain & Architecture References
- **Domain Vocabulary**: Strictly adhere to `GLOSSARY.md` (`Off-Flavor`, `Off-Flavor Category`, `Sensory Modality`, `Style Permissibility`, `Perception Threshold`, `Meilgaard Flavor Wheel`, `Favorite`, `Beverage Discipline`, `Beer Style`).
- **Architectural Decisions**:
  - `docs/adr/0001-relational-hop-style-cross-reference.md`: Relational join table (`OffFlavorStyleCrossRef`) for style cross-referencing.
  - `docs/adr/0006-unified-generic-ingredient-repository.md`: Shared catalog architecture (`Ingredient`, `BaseIngredientRepository`, `IngredientListViewModel`, `IngredientDetailViewModel`, `IngredientCatalogScaffold`, `IngredientCardScaffold`, `IngredientDetailScaffold`, `FavoriteIngredientDao`).
  - `docs/adr/0008-off-flavors-sensory-fault-catalog-seam.md`: Semantic mapping of `OffFlavor` onto the generic catalog seam (sensory cue emoji via `countryFlag`, `substituteIds` mapped to *"Commonly Confused With"*, one-way *"Styles Where Permissible"* links without cluttering `StyleDetailScreen`, and a structured **Sensory Detection & Thresholds Card** in place of an 8-axis radar polygon).
- **Performance & UI Standards**: Follow `GEMINI.md`:
  - Compose `@Immutable` domain and filter models.
  - Offload Flow mapping and filtering to `Dispatchers.Default` via `.flowOn(Dispatchers.Default)`.
  - SQLite FTS4 subquery indexing (`rowid IN (SELECT rowid FROM off_flavors_fts WHERE off_flavors_fts MATCH :query)`).
  - Pre-populate Room DB from bundled JSON assets via `OffFlavorSeeder` (`DomainSeeder`) during `DatabaseInitializer`.
  - No placeholder/hint text in search fields (`OutlinedTextField`).
  - No `"All"` chip in filter strips or filter sheets.
  - No trailing navigation arrows/chevrons on cards; strictly follow the 4-row `IngredientCardScaffold` layout.

---

## 📋 Requirements & Acceptance Criteria

### 1. Data Pipeline & Persistence
- [ ] **Bundled Asset (`off_flavors.json`)**: Curate and bundle `app/src/main/assets/off_flavors.json` with the **28 canonical judging off-flavors** grouped across 5 **Off-Flavor Categories**:
  1. **Fermentation & Yeast** (`Warm Amber` badge):
     - 🍏 **Acetaldehyde** (*Ethanal*)
     - 🔥 **Alcoholic / Fusel** (*Higher Alcohols: Propanol, Isobutanol, Isoamyl Alcohol*)
     - 🥩 **Autolysis** (*Yeast Cell Breakdown / Amino & Fatty Acids*)
     - 🐐 **Caprylic / Goaty** (*Octanoic & Decanoic Acids*)
     - 🧈 **Diacetyl** (*2,3-Butanedione*)
     - 🍌 **Estery** (*Isoamyl Acetate, Ethyl Acetate, Ethyl Hexanoate*)
     - 🧪 **Solvent** (*Ethyl Acetate & Fusel Esters*)
     - 🥚 **Sulfur (H₂S)** (*Hydrogen Sulfide & Sulfur Dioxide*)
     - 🍞 **Yeasty** (*Suspended Yeast Cells*)
  2. **Mashing & Boil** (`Golden Wheat` badge):
     - 🧼 **Alkaline** (*High pH / Residual Alkalinity / Caustic*)
     - 🍵 **Astringent** (*Polyphenols & Tannins*)
     - 🌽 **DMS (Dimethyl Sulfide)** (*Dimethyl Sulfide from SMM*)
     - 🌾 **Husky / Grainy** (*Grain Husk Polyphenols & Isobutyraldehyde*)
  3. **Sanitation & Microbial** (`Crimson Tart` badge):
     - 🍶 **Acetic** (*Acetic Acid via Acetobacter*)
     - 🤢 **Butyric** (*Butyric Acid via Clostridium*)
     - 🚽 **Indole** (*Coliform / Enteric Bacteria*)
     - 🥛 **Lactic** (*Lactic Acid via Lactobacillus / Pediococcus*)
     - 🧄 **Mercaptan** (*Ethanethiol / Methanethiol*)
     - 🌶️ **Phenolic** (*4-Vinyl Guaiacol & Volatile Phenols*)
     - 🍋 **Sour / Acidic** (*Mixed Organic Acids*)
     - 🐭 **THP (Mousiness)** (*Tetrahydropyridines via Brett / Lactobacillus*)
  4. **Oxidation & Aging** (`Mystic Purple` badge):
     - 🐈 **Catty** (*p-Menthane-8-thiol-3-one / Ribes*)
     - 🌱 **Grassy** (*Hexanal & Leaf Aldehydes*)
     - 🧀 **Isovaleric** (*Isovaleric Acid from Oxidized Hops*)
     - 📦 **Oxidized / Cardboard** (*Trans-2-nonenal*)
     - 🍷 **Sherry / Vinous** (*Melanoidin & Aldehyde Oxidation*)
     - 🥦 **Vegetal** (*Methional & Dimethyl Trisulfide*)
  5. **Water & Packaging** (`Crisp Sky Blue` badge):
     - 🩹 **Chlorophenol** (*2,6-Dichlorophenol*)
     - 🦨 **Lightstruck (Skunky)** (*3-Methyl-2-butene-1-thiol / MBT*)
     - 🪙 **Metallic** (*Ferrous Iron & Copper Ions*)
     - 🍄 **Musty / Corked** (*2,4,6-Trichloroanisole / Geosmin*)
- [ ] **JSON Schema & Domain Model (`OffFlavor : Ingredient`)**:
  - `id`: Unique kebab-case identifier (e.g. `diacetyl`, `acetaldehyde`, `dms`, `lightstruck`).
  - `name`: Canonical Off-Flavor name (e.g. `"Diacetyl"`).
  - `emoji`: Sensory cue emoji mapped to `Ingredient.countryFlag` (e.g. `"🧈"`).
  - `category`: One of the 5 **Off-Flavor Categories** (`Fermentation & Yeast`, `Mashing & Boil`, `Sanitation & Microbial`, `Oxidation & Aging`, `Water & Packaging`).
  - `chemicalCompound`: Primary chemical compound(s) (e.g. `"2,3-Butanedione"`).
  - `perceptionThreshold`: Concise human sensory threshold (e.g. `"0.10 – 0.15 ppm"`).
  - `typicalConcentration`: Typical concentration range in beer (e.g. `"0.02 – 0.08 ppm (normal); > 0.15 ppm (fault)"`).
  - `meilgaardCode`: Standardized ASBC/Meilgaard Flavor Wheel number(s) (e.g. `"0620"`).
  - `detectedIn`: List of **Sensory Modalities** (`Aroma`, `Flavor`, `Mouthfeel`).
  - `disciplines`: Applicable **Beverage Disciplines** (`Beer`, and where applicable `Mead`, `Cider`).
  - `stylePermissibility`: `Always a Fault` or `Style-Permissible`.
  - `description`: Succinct 1–2 sentence sensory summary for the catalog card and hero header.
  - `aromaPerception`: Concise 1-line description of how it presents in aroma (or `"None / Not volatile"` when palate-only).
  - `flavorPerception`: Concise 1-line description of how it presents on the palate.
  - `mouthfeelPerception`: Concise 1-line description of tactile/trigeminal sensation (or `"No direct mouthfeel impact"`).
  - `descriptors`: List of signature sensory cue tags (`"Butter"`, `"Butterscotch"`, `"Movie Popcorn"`, `"Slick Palate"`).
  - `diagnosticContrasts`: Scannable bullet points explaining **how to tell this off-flavor apart** from normal beverage ingredients (e.g. *Diacetyl* vs. *Caramel Malt*, *Astringent* vs. *Hop Bitterness*) as well as lookalike faults.
  - `typicalOrigins`: Concise bullet points of brewing, fermentation, microbial, or packaging root causes (*Why It Happens*).
  - `preventionTips`: Actionable bullet points for preventing or fixing the fault (*How to Fix / Prevent It*), suitable for competition scoresheets.
  - `whenAppropriate`: Concise guidance on when the compound is style-appropriate vs. a fault, including any Mead/Cider judging context.
  - `confusedWithIds`: Mapped to `Ingredient.substituteIds` to resolve clickable lookalike `OffFlavorCard` links.
  - `permissibleStyleIds`: Associated style identifiers across BJCP 2021, BA 2026, BJCP Mead, and BJCP Cider where low/moderate levels are permissible.
- [ ] **Room Database Version 10**:
  - `OffFlavorEntity`: Primary table storing complete off-flavor attributes.
  - `OffFlavorFtsEntity`: SQLite FTS4 virtual table (`off_flavors_fts`) indexing `name`, `chemicalCompound`, `category`, `meilgaardCode`, `description`, `descriptors`, `diagnosticContrasts`, `typicalOrigins`, `preventionTips`, and `whenAppropriate`.
  - `OffFlavorStyleCrossRef`: Relational join table keyed on `(offFlavorId, styleId, guidelineType)`.
- [ ] **Room Database Migration (v9 -> v10)**: Automated migration creating `off_flavors`, `off_flavors_fts`, and `off_flavor_style_cross_ref` without losing existing data or user favorites.
- [ ] **Database Preloading (`OffFlavorSeeder`)**: `DomainSeeder` implementation registered in `DatabaseInitializer` with fast-path `getOffFlavorCount() > 0` guard and concurrent background execution.
- [ ] **Repository Layer (`OffFlavorRepository`)**: Concrete subclass of `BaseIngredientRepository<OffFlavorEntity, OffFlavor, OffFlavorFilterCriteria>` with `ingredientType = "OFF_FLAVOR"`.

### 2. Off-Flavors Catalog List Screen (`OffFlavorsScreen` / route: `"off_flavors"`)
- [ ] **Shared Catalog Scaffold (`IngredientCatalogScaffold<OffFlavor>`) & ViewModel (`OffFlavorsViewModel : IngredientListViewModel<OffFlavor, OffFlavorFilterCriteria>`)**:
  - TopAppBar with title `"Off-Flavors"`, drawer menu navigation icon, and top-right `FilterActionIcon`.
  - Pill search input with leading search icon, trailing clear icon, and no placeholder hint text.
- [ ] **Quick-Filter Strip (`quickFilterStrip` slot)**:
  - Horizontal scrollable `FilterChip`s for the 5 **Off-Flavor Categories** (`Fermentation & Yeast`, `Mashing & Boil`, `Sanitation & Microbial`, `Oxidation & Aging`, `Water & Packaging`) without an `"All"` chip.
- [ ] **Filter Bottom Sheet (`OffFlavorFilterSheet`)**:
  1. **Category**: `Fermentation & Yeast`, `Mashing & Boil`, `Sanitation & Microbial`, `Oxidation & Aging`, `Water & Packaging`
  2. **Detected In (Sensory Modality)**: `Aroma`, `Flavor`, `Mouthfeel`
  3. **Beverage Discipline**: `Beer`, `Mead`, `Cider`
  4. **Style Permissibility**: `Always a Fault`, `Style-Permissible`
- [ ] **Glanceable `OffFlavorCard` (`IngredientCardScaffold`)**:
  1. **Header Row**: Sensory cue emoji (`offFlavor.countryFlag`) + bold Off-Flavor name on the left; color-coded `OffFlavorCategoryBadge` on the top right.
  2. **Sub-Header**: Italicized `"${offFlavor.chemicalCompound} • Threshold: ${offFlavor.perceptionThreshold}"`.
  3. **Narrative Description**: 1–2 sentence sensory summary (max 3 lines with ellipsis).
  4. **Bottom Strip**: Single-row sensory cue chip strip (`SingleRowChipLayout`).
- [ ] **Empty State**: Centered empty state with `Icons.Default.Science`, headline `"No off-flavors found"`, subtitle `"Try adjusting your search query or filters."`, and conditional `"Reset Filters"` button when `criteria.isFilterActive` is true.

### 3. Off-Flavor Detail Screen (`OffFlavorDetailScreen` / route: `"off_flavor_detail/{offFlavorId}"`)
Built with `IngredientDetailScaffold` and `IngredientDetailViewModel<OffFlavor>`:
1. **Hero Header Card**:
   - Sensory cue emoji, Off-Flavor name, italicized `"${chemicalCompound} • Threshold: ${perceptionThreshold}"`, and `OffFlavorCategoryBadge`.
   - Applicable `Beverage Discipline` chips (`Beer`, `Mead`, `Cider`) and `Style Permissibility` badge (`Always a Fault` vs. `Style-Permissible`).
   - Concise sensory overview paragraph.
2. **Sensory Detection & Thresholds Card**:
   - **Detected In Badges**: Visual pills indicating `Aroma`, `Flavor`, and/or `Mouthfeel`.
   - **Modality Breakdown**: Concise 1-line sensory descriptions for **Aroma**, **Flavor**, and **Mouthfeel**.
   - **Sensory Cue Descriptors**: FlowRow of descriptor chips.
   - **2×2 Technical Spec Metric Boxes**: `Perception Threshold`, `Typical Beer Range`, `Chemical Compound`, and `Meilgaard Wheel #`.
3. **Commonly Confused With & How to Tell Apart Card**:
   - Bulleted **How to Tell Apart** diagnostic contrast notes distinguishing the off-flavor from normal ingredients (e.g. caramel malt, roasted grain, hop bitterness) and similar faults.
   - Clickable `OffFlavorCard` items for related/lookalike off-flavors (`substitutes` flow from `IngredientDetailViewModel<OffFlavor>`).
4. **Why It Happens (Typical Origins) Card**:
   - Scannable bullet points of brewing, fermentation, sanitation, or packaging root causes.
5. **How to Fix / Prevent It Card**:
   - Actionable bullet points for brewer troubleshooting and competition scoresheet feedback.
6. **When Style-Appropriate & Permissible Styles**:
   - Narrative card explaining when the attribute is acceptable or expected (including Mead/Cider notes where relevant).
   - Clickable `AssociatedStylesSection` titled `"Styles Where Permissible"` when `commonStyles` is non-empty.

### 4. Cross-Cutting Integrations
- [ ] **Navigation Drawer (`AppDrawer.kt`)**:
  - Add `data object OffFlavors : DrawerDestination("off_flavors", "Off-Flavors", Icons.Default.Science)` positioned directly above `Favorites` (`StyleGuide, Hops, Malt, Honey, Pomes, Yeast, OffFlavors, Favorites, ReviewHistory, Settings`).
- [ ] **Favorites Integration (`FavoritesScreen.kt`, `FavoritesViewModel.kt`, `IngredientCatalogRepository.kt`)**:
  - Include favorited Off-Flavors (`type = "OFF_FLAVOR"`) in `FavoritesViewModel` and `FavoritesScreen` under `FavoriteDiscipline.OFF_FLAVORS` (`"Off-Flavors"`, `Icons.Default.Science`), supporting keyword search, discipline chip filtering, swipe-to-remove, and tap navigation to `off_flavor_detail/{offFlavorId}`.
  - Keep `StyleDetailScreen` free of Off-Flavors (one-way style cross-referencing per ADR 0008).

---

## 📱 User Interface & Visuals
- **Drawer Item**: `"Off-Flavors"` with `Icons.Default.Science` between `"Yeast"` and `"Favorites"`.
- **Category Badge Palette**:
  - `Fermentation & Yeast`: Warm Amber (`#FEF3C7` background / `#92400E` text)
  - `Mashing & Boil`: Golden Wheat (`#FEF9C3` background / `#854D0E` text)
  - `Sanitation & Microbial`: Crimson Tart (`#FEE2E2` background / `#991B1B` text)
  - `Oxidation & Aging`: Mystic Purple (`#F3E8FF` background / `#6B21A8` text)
  - `Water & Packaging`: Crisp Sky Blue (`#E0F2FE` background / `#0369A1` text)

---

## 📦 Assets & Attachments
- `app/src/main/assets/off_flavors.json`

---

## 🧪 Verification & Testing Strategy
- **Unit Tests**:
  - `OffFlavorRepositoryTest`: Verify FTS query sanitization, descriptor/compound/Meilgaard search matching, filtering by category/modality/discipline/permissibility, lookalike (`confusedWithIds`) resolution, and permissible style mapping.
  - `OffFlavorsViewModelTest`: Verify reactive filter criteria updates, category quick-filter toggling, and `resetFilters()` preserving active search query.
  - `FavoritesViewModelTest`: Verify favorited Off-Flavors appear in `FavoritesUiState`, filter under `FavoriteDiscipline.OFF_FLAVORS`, and support swipe-to-remove.
- **Database Migration & Seeder Tests**:
  - Verify migration `MIGRATION_9_10` and `OffFlavorSeeder` idempotency.
- **UI / Compose Tests**:
  - Verify `AppDrawer` renders `"Off-Flavors"` directly above `"Favorites"`.
  - Verify `OffFlavorsScreen` card layout, filter sheet interactions, and `OffFlavorDetailScreen` diagnostic and threshold cards.
