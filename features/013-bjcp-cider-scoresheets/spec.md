# Feature Spec: 013 BJCP Cider Scoresheets

---

## 🎯 High-Level Goal
Enable cider judges and cidermakers to create, edit, delete, search, filter, and share **BJCP Cider Scoresheets** under the **Reviews** section of the app—completing the Beer / Mead / Cider judging triad with the exact same streamlined UX and Cider-specific descriptors, entrant metadata, and Cider-relevant fault scoping.

---

## 👤 User Story
**As a** Cider Judge or Cidermaker  
**I want to** switch to BJCP Cider in the Scoresheet editor, link BJCP Cider styles (`C1`–`C2`), record 50-point Cider evaluations with Cider-specific descriptors and Cider-filtered off-flavors, and filter/search Cider reviews on the Reviews screen  
**So that** I can evaluate ciders and perries cleanly at a judging table or cidery using official BJCP Cider criteria.

---

## 📚 Domain & Architecture References
- **Domain Vocabulary**: Strictly adhere to `GLOSSARY.md` (`Scoresheet`, `Beverage Discipline`, `Cider Style`, `Cider Sweetness Classification`, `Cider Carbonation Classification`, `Cider Strength Classification`, `BJCP Scoring Tier`, `Off-Flavor`).
- **Architectural Decisions**:
  - `docs/adr/0009-bjcp-scoresheets-and-form-modes.md`: Unified `scoresheets` persistence, `ScoresheetDiscipline`, and single-back `ModalBottomSheet` inline reference sheets (`skipPartiallyExpanded = true`).
  - `docs/adr/0010-polymorphic-scoresheet-single-table.md`: `@Immutable sealed interface Scoresheet` (`BeerScoresheet`, concrete `FourSectionScoresheet` parameterized by `discipline`) backed by the unified `scoresheets` Room table, with `ScoresheetDiscipline` owning section bounds, labels, and options.
  - `docs/adr/0011-direct-preferences-seam-guideline-dispatch-and-editor-projection.md`: Direct `PreferencesManager` seam, unified `BeerStyleRepository.getStyles(GuidelineType)`, projected `ScoresheetEditorUiState`, and shared `StyleVitalStats` formatting (`"—"` fallback).
- **Performance & UI Standards**: Follow `GEMINI.md`:
  - Compose `@Immutable` domain and UI state models (`Scoresheet`, `FourSectionScoresheet`, `ScoresheetEditorUiState`).
  - Offload repository Flow mapping to `Dispatchers.Default` via `.flowOn(Dispatchers.Default)`.
  - No placeholder/hint text in search fields (`OutlinedTextField`).
  - Toggle `FilterChip`s on `ReviewsScreen` with no `"All"` chip.
  - No trailing navigation arrows/chevrons on list cards; neutral `onSurface` icon tinting on Share and Delete actions.

---

## 📋 Requirements & Acceptance Criteria

### 1. Data Pipeline & Persistence
- [x] **Polymorphic Domain Model (`Scoresheet.kt`)**:
  - Support `BJCP_CIDER` via the unified concrete `FourSectionScoresheet : Scoresheet` parameterized by `discipline`, with `ScoresheetDiscipline.BJCP_CIDER` defining section maximums, `"Cidermaker"` labels, and Cider-specific classification options (`Sweetness`, `Carbonation`, `Strength`).
  - `FourSectionScoresheet` (`BJCP_CIDER`): `id`, `entryTitle`, `brewer` (cidermaker), `styleId` (`BJCP_CIDER`), `styleName`, `styleNumber`, `evaluatedAtMillis`, integer section scores (`aromaScore` `0–10`, `appearanceScore` `0–6`, `flavorScore` `0–24`, `overallScore` `0–10`; all starting at `0`), optional per-section notes (`aromaNotes`, `appearanceNotes`, `flavorNotes`, `overallNotes`), `declaredCarbonation: String?`, `declaredSweetness: String?`, `declaredStrength: String?`, `specialIngredients: String`, `bottleInspectionOk`, `bottleInspectionNotes`, `0–10` impressions (`stylisticAccuracy`, `technicalMerit`, `intangibles`), `descriptorValues: Map<String, String>`, `offFlavorLevels: Map<String, IntensityLevel>`, and `updatedAtMillis`.
  - Computed `totalScore` (`/50`) and `scoringTier` (`BjcpScoringTier`) shared via `Scoresheet`.
  - **Cider `ScoresheetDescriptor`s**:
    - **Aroma** (`0–10`): `Apple / Pear`, `Fruity / Esters`, `Spices / Adjuncts`, `Oak / Wood`, `Alcohol`, `Fermentation` (`Low / Med / High`).
    - **Appearance** (`0–6`): `Color` (`Pale / Yellow / Gold / Amber / Copper / Pink / Red`), `Clarity` (`Opaque / Hazy / Brilliant`), `Legs / Viscosity` (`Light / Med / Heavy`), `Carbonation` (`Still / Pétillant / Sparkling`).
    - **Flavor** (`0–24`): `Apple / Pear`, `Spices / Adjuncts`, `Oak / Wood`, `Sweetness`
      (`Dry / Medium-Dry / Medium / Medium-Sweet / Sweet`), `Acidity` (`Low / Med / High`),
      `Tannin / Astringency` (`Low / Med / High`), `Body` (`Thin / Med / Full`), `Carbonation`
      (`Still / Pétillant / Sparkling`), `Alcohol Warmth` (`Low / Med / High`), `Balance`
      (`Sweet / Balanced / Tart-Tannic`), `Finish` (`Dry / Balanced / Sweet`), `Aftertaste`
      (`Clean / Lingering / Bitter / Sweet / Tart`).
  - `formatForShare()`: Generates `"BJCP Cider Scoresheet"` plain-text summary with `Cidermaker`, populated `Additional Information` lines only when set, the 4-section breakdown (`Aroma / 10`, `Appearance / 6`, `Flavor / 24`, `Overall Impression / 10`), impressions, descriptors, off-flavors, and notes.
- [x] **Room Persistence (`ScoresheetEntity.kt`)**:
  - Round-trip `FourSectionScoresheet` (`BJCP_CIDER`) cleanly through the existing `scoresheets` table (`declaredCarbonation`, `declaredSweetness`, `declaredStrength`, `specialIngredients`) without requiring a schema migration.
- [x] **Repository (`BeerStyleRepository`)**:
  - Expose `getStyles(GuidelineType.BJCP_CIDER): Flow<List<BeerStyle>>` sourced from `CiderStyleDao` via the unified `GuidelineStyleSource` adapter map independent of the active Style Guide setting.

### 2. Reviews List Screen (`ReviewsScreen` / `ReviewsViewModel`)
- [x] **Search & Discipline Filter**:
  - Existing `🍏 Cider (N)` toggle `FilterChip` filters Cider reviews cleanly alongside Beer and Mead.
  - Text search matches `specialIngredients` on `FourSectionScoresheet` in addition to `entryTitle`, `styleName`, `styleNumber`, and `brewer`.
  - `ScoresheetCard` uses the shared compact 4-row layout (`1. Date • 🍏 Cider` + Share/Delete; `2. Cidermaker`; `3. Entry Name` + `Score/50 • Tier`; `4. Style`).

### 3. Scoresheet Editor Screen (`ScoresheetEditorScreen` / `ScoresheetEditorViewModel`)
- [x] **Discipline Switcher, Remembered Default & Dirty-Draft Guard**:
  - `🍏 Cider` is active in `SingleChoiceSegmentedButtonRow` on new sheets, switching silently when clean and prompting for reset confirmation when dirty (preserving `entryTitle`, `brewer`/maker, `evaluatedAtMillis`, and `bottleInspection`).
  - Saving a new Cider `FourSectionScoresheet` updates `PreferencesManager.lastScoresheetDiscipline` to `BJCP_CIDER` so subsequent new sheets start with Cider selected.
  - Editing a saved Cider sheet hides the switcher and opens directly in Cider mode.
- [x] **Cider Entry Details & Collapsible `"Additional Information"`**:
  - Adapted labels: `"Entry # or Cider Name *"`, `"Cidermaker / Cidery (Optional)"`, and `"BJCP Cider Style"` dropdown scoped to `GuidelineType.BJCP_CIDER`.
  - `"Additional Information"` expandable subsection inside `Entry Details`:
    - Collapsed by default on new sheets (chips stay unset even when linking a style); auto-expanded when opening a saved Cider sheet with at least one populated value in this section.
    - Contains 3 multi-tap chips (`Carbonation`: `Still → Pétillant → Sparkling → Unset`; `Sweetness`: `Dry → Medium-Dry → Medium → Medium-Sweet → Sweet → Unset`; `Strength`: `Session → Standard → High Gravity → Unset`) and a `"Special Ingredients / Varietals (Optional)"` text field (max 2 lines).
  - Keeps **Bottle Inspection** below `"Additional Information"`.
- [x] **4 Scored Section Cards (`10 / 6 / 24 / 10`), Impressions & Cider-Scoped Off-Flavors**:
  - Renders `Aroma` (`0–10`), `Appearance` (`0–6`), `Flavor` (`0–24`), and `Overall Impression` (`0–10`) section cards with Cider descriptors (no `Mouthfeel` card).
  - Keeps `0–10` `Stylistic Accuracy`, `Technical Merit`, and `Intangibles` sliders identically to Beer and Mead.
  - Filters `Off-Flavors & Faults` chips to faults tagged `"Cider"` in `OffFlavor.disciplines` (retaining long-press `OffFlavorQuickPeekSheet`).
- [x] **Inline Reference Quick-Peeks**:
  - `StyleQuickPeekSheet` adapts when `style.guidelineType.isCider` to display `OG`, `FG`, `ABV`, `Sweetness`, `Carbonation`, and `Strength` (instead of Beer's `IBU` / `SRM`).
  - Cider `Flavor` `(i)` info sheet combines `linkedStyle.flavor` and `linkedStyle.mouthfeel`.

---

## 🧪 Testing Plan
- `ScoresheetModelTest`: `FourSectionScoresheet` (`BJCP_CIDER`) 50-point calculation (`10/6/24/10`), `ScoresheetEntity` round-trip serialization, Cider descriptor cycling and section filtering, and `formatForShare()` (populated vs. empty `Additional Information`).
- `ScoresheetEditorViewModelTest`: Cider score clamping (`10/6/24/10`), `CiderStyle` linking without auto-filling collapsed `Additional Information` chips, Cider `Sweetness`/`Carbonation`/`Strength` multi-tap cycling, dirty discipline switching between Cider/Mead/Beer, remembered `BJCP_CIDER` creation default, and save/load persistence.
- `ReviewsViewModelTest`: Filtering by `BJCP_CIDER` and searching Cider `specialIngredients`.
- Verified with `./gradlew --no-daemon --offline testDebugUnitTest assembleDebug`.
