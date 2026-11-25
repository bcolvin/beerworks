# Feature Spec: 012 BJCP Mead Scoresheets

---

## 🎯 High-Level Goal
Enable mead judges and meadmakers to create, edit, delete, search, filter, and share **BJCP Mead Scoresheets** under the **Reviews** section of the app—sharing the streamlined Beer scoresheet UX while matching official BJCP Mead scoring sections, descriptors, entrant metadata, and Mead-relevant fault scoping.

---

## 👤 User Story
**As a** Mead Judge or Meadmaker  
**I want to** switch to BJCP Mead in the Scoresheet editor, link BJCP Mead styles (`M1`–`M4`), record 50-point Mead evaluations with Mead-specific descriptors and Mead-filtered off-flavors, and filter/search Mead reviews on the Reviews screen  
**So that** I can evaluate meads cleanly at a judging table or tasting room using official BJCP Mead criteria.

---

## 📚 Domain & Architecture References
- **Domain Vocabulary**: Strictly adhere to `GLOSSARY.md` (`Scoresheet`, `Beverage Discipline`, `Mead Style`, `Mead Sweetness Classification`, `Mead Carbonation Classification`, `Mead Strength Classification`, `BJCP Scoring Tier`, `Off-Flavor`).
- **Architectural Decisions**:
  - `docs/adr/0009-bjcp-scoresheets-and-form-modes.md`: Unified `scoresheets` persistence, `ScoresheetDiscipline`, and single-back `ModalBottomSheet` inline reference sheets (`skipPartiallyExpanded = true`).
  - `docs/adr/0010-polymorphic-scoresheet-single-table.md`: `@Immutable sealed interface Scoresheet` (`BeerScoresheet`, concrete `FourSectionScoresheet` parameterized by `discipline`) backed by the unified `scoresheets` Room table (`MIGRATION_13_14`), with `ScoresheetDiscipline` as the single source of truth for section bounds, labels, and options.
  - `docs/adr/0011-direct-preferences-seam-guideline-dispatch-and-editor-projection.md`: Direct `PreferencesManager` seam, unified `BeerStyleRepository.getStyles(GuidelineType)`, projected `ScoresheetEditorUiState`, and shared `StyleVitalStats` formatting (`"—"` fallback).
- **Performance & UI Standards**: Follow `GEMINI.md`:
  - Compose `@Immutable` domain and UI state models (`Scoresheet`, `BeerScoresheet`, `FourSectionScoresheet`, `ScoresheetEditorUiState`).
  - Offload repository Flow mapping to `Dispatchers.Default` via `.flowOn(Dispatchers.Default)`.
  - No placeholder/hint text in search fields (`OutlinedTextField`).
  - Toggle `FilterChip`s on `ReviewsScreen` with no `"All"` chip.
  - No trailing navigation arrows/chevrons on list cards; neutral `onSurface` icon tinting on Share and Delete actions.

---

## 📋 Requirements & Acceptance Criteria

### 1. Data Pipeline & Persistence
- [x] **Polymorphic Domain Model (`Scoresheet.kt`)**:
  - Model `Scoresheet` as `@Immutable sealed interface Scoresheet` with `BeerScoresheet` and unified `FourSectionScoresheet(discipline = BJCP_MEAD | BJCP_CIDER)` implementations, with `ScoresheetDiscipline` owning section maximums, maker/field labels, and classification options.
  - `FourSectionScoresheet` (`BJCP_MEAD`): `id`, `entryTitle`, `brewer` (meadmaker), `styleId` (`BJCP_MEAD`), `styleName`, `styleNumber`, `evaluatedAtMillis`, integer section scores (`aromaScore` `0–10`, `appearanceScore` `0–6`, `flavorScore` `0–24`, `overallScore` `0–10`; all starting at `0`), optional per-section notes (`aromaNotes`, `appearanceNotes`, `flavorNotes`, `overallNotes`), `declaredCarbonation: String?`, `declaredSweetness: String?`, `declaredStrength: String?`, `specialIngredients: String`, `bottleInspectionOk`, `bottleInspectionNotes`, `0–10` impressions (`stylisticAccuracy`, `technicalMerit`, `intangibles`), `descriptorValues: Map<String, String>`, `offFlavorLevels: Map<String, IntensityLevel>`, and `updatedAtMillis`.
  - Computed `totalScore` (`/50`) and `scoringTier` (`BjcpScoringTier`) shared via `Scoresheet`.
  - **Mead `ScoresheetDescriptor`s**:
    - **Aroma** (`0–10`): `Honey`, `Floral / Fruity`, `Spices / Adjuncts`, `Alcohol`, `Fermentation` (`Low / Med / High`).
    - **Appearance** (`0–6`): `Color` (`Straw / Gold / Amber / Dark / Pink / Red / Purple`), `Clarity` (`Opaque / Hazy / Brilliant`), `Legs / Viscosity` (`Light / Med / Heavy`), `Carbonation` (`Still / Pétillant / Sparkling`).
    - **Flavor** (`0–24`): `Honey`, `Spices / Adjuncts`, `Sweetness` (`Dry / Semi-Sweet / Sweet`),
      `Acidity` (`Low / Med / High`), `Tannin / Astringency` (`Low / Med / High`), `Body`
      (`Light / Med / Full`), `Carbonation` (`Still / Pétillant / Sparkling`), `Alcohol Warmth`
      (`Low / Med / High`), `Balance` (`Sweet / Balanced / Acidic`), `Finish`
      (`Dry / Balanced / Sweet`), `Aftertaste` (`Clean / Lingering / Bitter / Sweet / Tart`).
  - `formatForShare()`: Generates `"BJCP Mead Scoresheet"` plain-text summary including populated `Additional Information` lines only when set, the 4-section breakdown (`Aroma / 10`, `Appearance / 6`, `Flavor / 24`, `Overall Impression / 10`), impressions, descriptors, off-flavors, and notes.
- [x] **Room Database (`BeerworksDatabase` v13 → v14)**:
  - Extend `ScoresheetEntity` with `declaredCarbonation`, `declaredSweetness`, `declaredStrength`, and `specialIngredients` columns (`TEXT NOT NULL DEFAULT ''`), migrated non-destructively via `MIGRATION_13_14`.
  - Update `ScoresheetEntity.toDomainModel()` and `Scoresheet.toEntity()` to round-trip both `BeerScoresheet` and `FourSectionScoresheet`.
- [x] **Repository (`BeerStyleRepository`)**:
  - Expose `getStyles(GuidelineType.BJCP_MEAD): Flow<List<BeerStyle>>` sourced from `MeadStyleDao` via the unified `GuidelineStyleSource` adapter map independent of the active Style Guide setting.

### 2. Reviews List Screen (`ReviewsScreen` / `ReviewsViewModel`)
- [x] **Search & Discipline Filter**:
  - Existing `🐝 Mead (N)` toggle `FilterChip` filters Mead reviews cleanly alongside `🍺 Beer (N)`.
  - Text search matches `specialIngredients` on `FourSectionScoresheet` in addition to `entryTitle`, `styleName`, `styleNumber`, and `brewer`.
  - `ScoresheetCard` uses the shared compact 4-row layout (`1. Date • 🐝 Mead` + Share/Delete; `2. Meadmaker`; `3. Entry Name` + `Score/50 • Tier`; `4. Style`).

### 3. Scoresheet Editor Screen (`ScoresheetEditorScreen` / `ScoresheetEditorViewModel`)
- [x] **Discipline Switcher, Remembered Default & Dirty-Draft Guard (New Sheets)**:
  - Creating a new scoresheet initializes with the last created & saved discipline (`PreferencesManager.lastScoresheetDiscipline`, defaulting to `BJCP_BEER`; editing an existing sheet does not overwrite this preference).
  - Selecting `🐝 Mead` in `SingleChoiceSegmentedButtonRow` on a new sheet switches silently when no style, scores, descriptors, or section notes have been entered (`isDirty == false`).
  - Once any judging data is entered, switching between Beer and Mead prompts with a confirmation `AlertDialog` that resets style, scores, descriptors, and section notes while preserving `entryTitle`, `brewer`/meadmaker, `evaluatedAtMillis`, and `bottleInspection`.
  - Editing a saved Mead sheet hides the switcher and opens directly in Mead mode.
- [x] **Mead Entry Details & Collapsible `"Additional Information"`**:
  - Adapted labels: `"Entry # or Mead Name *"`, `"Meadmaker / Meadery (Optional)"`, and `"BJCP Mead Style"` dropdown scoped to `GuidelineType.BJCP_MEAD`.
  - `"Additional Information"` expandable subsection inside `Entry Details`:
    - Collapsed by default on new sheets (chips stay unset even when linking `M1A`–`M1C`); auto-expanded when opening a saved Mead sheet that has at least one populated value in this section.
    - Contains 3 multi-tap chips (`Carbonation`: `Still → Pétillant → Sparkling → Unset`; `Sweetness`: `Dry → Semi-Sweet → Sweet → Unset`; `Strength`: `Hydromel → Standard → Sack → Unset`) and a `Special Ingredients` text field (max 2 lines).
  - Keeps **Bottle Inspection** below `"Additional Information"`.
- [x] **4 Scored Section Cards (`10 / 6 / 24 / 10`), Impressions & Mead-Scoped Off-Flavors**:
  - Renders `Aroma` (`0–10`), `Appearance` (`0–6`), `Flavor` (`0–24`), and `Overall Impression` (`0–10`) section cards with Mead descriptors (no `Mouthfeel` card).
  - Keeps `0–10` `Stylistic Accuracy`, `Technical Merit`, and `Intangibles` sliders identically to Beer.
  - Filters `Off-Flavors & Faults` chips to faults tagged `"Mead"` in `OffFlavor.disciplines` (retaining long-press `OffFlavorQuickPeekSheet`).
- [x] **Inline Reference Quick-Peeks**:
  - `StyleQuickPeekSheet` adapts when `style.guidelineType.isMead` to display `OG`, `FG`, `ABV`, `Sweetness`, `Carbonation`, and `Strength` (instead of Beer's `IBU` / `SRM`).
  - Mead `Flavor` `(i)` info sheet combines `linkedStyle.flavor` and `linkedStyle.mouthfeel`.

---

## 🧪 Testing Plan
- `ScoresheetModelTest`: `FourSectionScoresheet` (`BJCP_MEAD`) 50-point calculation (`10/6/24/10`), `ScoresheetEntity` round-trip serialization for both `BeerScoresheet` and `FourSectionScoresheet`, Mead descriptor cycling, and `formatForShare()` (populated vs. empty `Additional Information`).
- `ScoresheetEditorViewModelTest`: Mead score clamping (`10/6/24/10`), `MeadStyle` linking without auto-filling collapsed `Additional Information` chips, clean vs. dirty discipline switching with shared metadata preservation, Mead off-flavor filtering, and save/load persistence.
- `ReviewsViewModelTest`: Filtering by `BJCP_MEAD` and searching by `specialIngredients`.
- Verified with `./gradlew --no-daemon --offline testDebugUnitTest assembleDebug`.
