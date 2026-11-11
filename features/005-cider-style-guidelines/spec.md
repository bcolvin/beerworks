# Feature Spec: 005 - BJCP Cider Style Guidelines

Provide an offline-first reference viewer for the official BJCP Cider Style Guidelines (Categories C1–C2), with discipline-specific sensory indicators (5-tier Sweetness, Carbonation, Strength), dynamic beverage discipline iconography (Apple glyph 🍏 / `Icons.Default.Eco`), multi-select filter toggles, and streamlined guideline switching.

---

## 🎯 High-Level Goal
Allow cider judges, cidermakers, and brewers to reference the complete BJCP Cider Guidelines with dedicated cider metrics (5-tier Sweetness, Carbonation, Strength, Gravity ranges), seamless header-based guideline switching, and an uncluttered interface without beer-specific distractions (such as IBU or SRM).

---

## 👤 User Story
**As a** BJCP Cider Judge or Cidermaker  
**I want to** switch into the BJCP Cider Guidelines from the top app bar header selector  
**So that** I can explore standard and specialty cider styles, review complete sensory descriptions, filter by sweetness and strength tiers, and inspect vital gravity statistics without irrelevant beer metrics.

---

## 📋 Requirements & Acceptance Criteria

### Data & Persistence
- [ ] **Curated Asset (`cider_styles.json`)**: Bundle all 11 canonical BJCP cider styles across 2 categories:
  - **C1. Standard Cider and Perry**: *C1A New World Cider*, *C1B English Cider*, *C1C French Cider*, *C1D New World Perry*, *C1E Traditional Perry*
  - **C2. Specialty Cider and Perry**: *C2A New England Cider*, *C2B Cider with Other Fruit*, *C2C Applewine*, *C2D Ice Cider*, *C2E Cider with Herbs/Spices*, *C2F Specialty Cider/Perry*
- [ ] **Room Database Version 6**: Introduce `CiderStyleEntity` and FTS4 virtual table `cider_styles_fts`, managed by `CiderStyleDao`.
- [ ] **Automated Room Migration (v5 -> v6)**: Create `MIGRATION_5_6` for seamless upgrade without data loss.
- [ ] **Background Preloading**: `DatabaseInitializer` seeds `cider_styles` from `cider_styles.json` on initial launch.
- [ ] **Relational Independence**: Do not link ciders to hops or malts (dedicated apple/fruit/honey databases deferred to future phases).

### Domain & Guideline Switching
- [ ] **Guideline Type Enum**: Add `BJCP_CIDER("BJCP Cider")` to `GuidelineType` with `discipline = "Cider"` and helper `isCider`.
- [ ] **Header Quick Switcher**: Long-pressing or tapping the top-level style guide header reveals `BJCP Cider` alongside `BJCP 2021`, `Brewers Association 2026`, and `BJCP Mead`.
- [ ] **Settings Screen Streamlining**: Remove the "Active Style Guideline" radio card completely from `SettingsScreen`, making the header selector the single unified guideline picker.
- [ ] **Contextual Dynamic Discipline Icon**:
  - Beer (BJCP 2021, BA 2026): Beer mug (`Icons.Default.SportsBar` / 🍺)
  - Mead (BJCP Mead): Bee glyph (`Icons.Default.EmojiNature` / 🐝)
  - Cider (BJCP Cider): Eco / Fruit leaf glyph (`Icons.Default.Eco` / 🍏)
  - Reflected dynamically on the **Style Guide drawer item**, the **top app bar subtitle badge**, and the **zero-results empty state**.

### Style List & Category Presentation
- [ ] **Base Style Grouping**: Categories displayed as `C1. Standard Cider and Perry` and `C2. Specialty Cider and Perry` with inline accordion expansion.
- [ ] **Category Summary Specs**: Show **ABV Range** and **Final Gravity Range** (e.g., `0.995–1.020 FG`), suppressing IBU and SRM bars.
- [ ] **StyleCard Cider Indicators**: On cider cards, replace SRM swatch and IBU text with indicator chips for:
  - **Sweetness**: *Dry*, *Medium-Dry*, *Medium*, *Medium-Sweet*, *Sweet*
  - **Carbonation**: *Still*, *Pétillant*, *Sparkling*
  - **Strength**: *Session*, *Standard*, *High Gravity*
- [ ] **Auto-Scroll on Expansion**: Maintain the rule ensuring at least 1.5 sub-cards are visible when expanding cider categories.

### Filtering & Search
- [ ] **Multi-Select Toggle Filter Chips**: On `FilterSheet`, when cider is active, replace SRM/IBU sliders with multi-select toggle chips for Cider Sweetness (5 tiers), Carbonation (3 tiers), and Strength (3 tiers).
- [ ] **Weighted Relevance Search**: Leverage search ranking weighting Style Name/Number (10), Overall Impression and Commercial Examples (5), and other sensory fields (1).

### Style Detail Screen
- [ ] **Cider Detail View**: Display full sensory sections (Aroma, Appearance, Flavor, Mouthfeel, Overall Impression, Ingredients, Commercial Examples).
- [ ] **Suppress Beer Visualizers**: Hide vertical SRM scrollbar and bottom SRM banner for cider styles.
- [ ] **Ingredient Cross-References Suppressed**: Omit hop and malt cross-reference cards for cider styles.

---

## 📱 User Interface & Visuals
- **Navigation Drawer**: "Style Guide" navigation item displays an `Eco` leaf icon when BJCP Cider is active.
- **Top App Bar**: Subtitle reads `BJCP Cider 🍏`.
- **Category Card**: Minimalist card showing `C1. Standard Cider and Perry` with ABV and FG range metrics.
- **Style Card**: Displays style number badge (e.g. `C1A`), name, full overall impression up to 10 lines, Sweetness/Carbonation/Strength chips, and ABV range.
- **Filter Sheet**: Dynamic filter chip rows for Cider Sweetness, Carbonation, and Strength.
- **Settings Screen**: Clean screen displaying Keep Screen Awake toggle without redundant style guideline selection.
- **Detail Screen**: Structured sensory cards without SRM color bars; sequential forward navigation on hidden horizontal swipe.

---

## 📦 Assets & Attachments
- `app/src/main/assets/cider_styles.json`: Complete dataset containing 11 canonical BJCP cider styles.
