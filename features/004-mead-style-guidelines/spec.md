# Feature Spec: 004 - BJCP Mead Style Guidelines

Provide an offline-first reference viewer for the official BJCP Mead Style Guidelines (Categories M1–M4), with discipline-specific sensory indicators (Sweetness, Carbonation, Strength), dynamic beverage discipline iconography (Bee glyph), and multi-select filter toggles.

---

## 🎯 High-Level Goal
Allow mead judges, meadmakers, and brewers to reference the complete BJCP Mead Guidelines with dedicated mead metrics (Sweetness, Carbonation, Strength, Gravity ranges), seamless guideline switching, and cross-referencing for Braggots.

---

## 👤 User Story
**As a** BJCP Mead Judge or Meadmaker  
**I want to** switch into the BJCP Mead Guidelines from the header or settings  
**So that** I can explore mead styles, read complete sensory descriptions, filter by sweetness and strength, and navigate styles without beer-specific distractions (such as IBU or SRM).

---

## 📋 Requirements & Acceptance Criteria

### Data & Persistence
- [x] **Curated Asset (`mead_styles.json`)**: Bundle all 14 canonical BJCP mead styles across 4 categories:
  - **M1. Traditional Mead**: *M1A Dry Mead*, *M1B Semi-Sweet Mead*, *M1C Sweet Mead*
  - **M2. Fruit Mead**: *M2A Cyser*, *M2B Pyment*, *M2C Berry Mead*, *M2D Stone Fruit Mead*, *M2E Melomel*
  - **M3. Spiced Mead**: *M3A Fruit and Spice Mead*, *M3B Spice, Herb or Vegetable Mead*
  - **M4. Specialty Mead**: *M4A Braggot*, *M4B Historical Mead*, *M4C Experimental Mead*
- [x] **Room Database Version 5**: Introduce `MeadStyleEntity` and FTS4 virtual table `mead_styles_fts`, managed by `MeadStyleDao`.
- [x] **Automated Room Migration (v4 -> v5)**: Create `MIGRATION_4_5` for seamless upgrade without data loss.
- [x] **Background Preloading**: `DatabaseInitializer` seeds `mead_styles` from `mead_styles.json` on initial launch.
- [x] **Braggot Relational Cross-Referencing**: Link *M4A Braggot* to brewing hops and malts in `hop_style_cross_ref`.

### Domain & Guideline Switching
- [x] **Guideline Type Enum**: Add `BJCP_MEAD("BJCP Mead")` to `GuidelineType`.
- [x] **Header Quick Switcher**: Long-pressing the top-level style guide header reveals `BJCP Mead` alongside `BJCP 2021` and `Brewers Association 2026`.
- [x] **Settings Screen Selection**: Allow selecting `BJCP Mead` in `SettingsScreen`.
- [x] **Contextual Dynamic Discipline Icon**:
  - Beer (BJCP 2021, BA 2026): Beer mug (`Icons.Default.SportsBar`)
  - Mead (BJCP Mead): Bee glyph (`Icons.Default.EmojiNature` / bee icon)
  - Reflected dynamically on the **Style Guide drawer item**, the **top app bar subtitle badge**, and the **zero-results empty state**.

### Style List & Category Presentation
- [x] **Base Style Grouping**: Categories displayed as `M1. Traditional Mead` through `M4. Specialty Mead` with inline accordion expansion.
- [x] **Category Summary Specs**: Show **ABV Range** and **Final Gravity Range** (e.g., `1.000–1.050 FG`), suppressing IBU and SRM bars.
- [x] **StyleCard Mead Indicators**: On mead cards, replace SRM swatch and IBU text with indicator chips for **Sweetness** (*Dry*, *Semi-Sweet*, *Sweet*), **Carbonation** (*Still*, *Pétillant*, *Sparkling*), and **Strength** (*Hydromel*, *Standard*, *Sack*).
- [x] **Auto-Scroll on Expansion**: Maintain the rule ensuring at least 1.5 sub-cards are visible when expanding mead categories.

### Filtering & Search
- [x] **Multi-Select Toggle Filter Chips**: On `FilterSheet`, when mead is active, replace SRM/IBU sliders with multi-select toggle chips for Sweetness, Carbonation, and Strength.
- [x] **Clickable Descriptor Chip Filtering**: Tapping style descriptor chips on `StyleCard` within `StyleListScreen` toggles normalized tag filters using multi-select **AND** matching, synced with the removable active-chip bar below the search box.
- [x] **Weighted Relevance Search**: Leverage search ranking weighting Style Name/Number (10), Overall Impression and Commercial Examples (5), and other sensory fields (1).

### Style Detail Screen
- [x] **Mead Detail View**: Display full sensory sections (Aroma, Appearance, Flavor, Mouthfeel, Overall Impression, Ingredients, Commercial Examples).
- [x] **Suppress Beer Visualizers**: Hide vertical SRM scrollbar and bottom SRM banner for mead styles.
- [x] **Selective Ingredient Cross-References**: Show "Common Hops" and "Common Malts & Grains" exclusively on *M4A Braggot*.

---

## 📱 User Interface & Visuals
- **Navigation Drawer**: "Style Guide" item displays a bee icon when BJCP Mead is active.
- **Top App Bar**: Subtitle reads `BJCP Mead 🐝`.
- **Category Card**: Minimalist card showing `M1. Traditional Mead` with ABV and FG range metrics.
- **Style Card**: Displays style number badge (e.g. `M1A`), name, full overall impression up to 10 lines, Sweetness/Carbonation/Strength chips, and ABV range.
- **Detail Screen**: Structured sensory cards without SRM color bars; sequential forward navigation on hidden horizontal swipe.
