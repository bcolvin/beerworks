# Feature Spec: 011 BJCP Beer Scoresheets & QoL Enhancements

---

## 🎯 High-Level Goal
Enable beer judges and brewers to record, edit, delete, search, and share **Scoresheets** under the **Reviews** section of the app—paired with navigation, settings, and Brewers Association list QoL improvements.

---

## 👤 User Story
**As a** Beer Judge or Homebrewer  
**I want to** create, edit, delete, and share BJCP Beer scoresheets directly on my phone with integer sliders, quick-tap sensory descriptor chips, canonical off-flavor chips, and inline style/scoring reference sheets  
**So that** I can evaluate beers cleanly at a judging table or taproom without losing unsaved work or needing paper sheets.

---

## 📚 Domain & Architecture References
- **Domain Vocabulary**: Strictly adhere to `GLOSSARY.md` (`Scoresheet`, `BJCP Scoring Tier`, `Off-Flavor`, `Beer Style`).
- **Architectural Decisions**:
  - `docs/adr/0009-bjcp-scoresheets-and-form-modes.md`: Unified `ScoresheetEntity` (`scoresheets`), `BJCP_2021`-scoped style linking, and single-back `ModalBottomSheet` inline reference sheets (`skipPartiallyExpanded = true`).
- **Performance & UI Standards**: Follow `GEMINI.md`:
  - Compose `@Immutable` domain and UI state models (`Scoresheet`, `ScoresheetEditorUiState`, `SectionGuidelineInfo`).
  - Offload repository Flow mapping to `Dispatchers.Default` via `.flowOn(Dispatchers.Default)`.
  - No placeholder/hint text in search fields (`OutlinedTextField`).
  - No trailing navigation arrows/chevrons on list cards.
  - Neutral `onSurface` icon tinting on Share and Delete actions (no red trashcan tint).

---

## 📋 Requirements & Acceptance Criteria

### 1. Data Pipeline & Persistence
- [x] **Domain Model (`Scoresheet.kt`)**:
  - `Scoresheet`: `id`, `formType`, `entryTitle` (Entry # or Beer Name), `brewer` (optional), `styleId` (optional `BJCP_2021` link), `styleName`, `styleNumber`, `evaluatedAtMillis`, integer section scores (`aromaScore` 0–12, `appearanceScore` 0–3, `flavorScore` 0–20, `mouthfeelScore` 0–5, `overallScore` 0–10; all starting at `0`), optional per-section notes (`aromaNotes`, `appearanceNotes`, `flavorNotes`, `mouthfeelNotes`, `overallNotes`), `bottleInspectionOk` (Boolean), `bottleInspectionNotes`, 0–10 impressions (`stylisticAccuracy`, `technicalMerit`, `intangibles`; all starting at `0`), `descriptorValues: Map<String, String>`, `offFlavorLevels: Map<String, IntensityLevel>`, and `updatedAtMillis`.
  - Computed `totalScore` (`/50`) and `scoringTier` (`BjcpScoringTier`: Outstanding 45–50, Excellent 38–44, Very Good 30–37, Good 21–29, Fair 14–20, Problematic 0–13 with official descriptions).
  - `ScoresheetDescriptor`: Section-grouped tap-cycle descriptors for `Aroma` (`Malt`, `Hops`, `Fermentation Character`), `Appearance` (`Color`, `Clarity`, `Head Size`, `Head Retention`, `Head Color`), `Flavor` (`Malt`, `Hops`, `Bitterness`, `Fermentation Character`, `Balance`, `Finish / Aftertaste`), and `Mouthfeel` (`Body`, `Carbonation`, `Warmth`, `Creaminess`, `Astringency`).
  - `IntensityLevel`: 4-state cycle (`Unset → Low → Medium → High → Unset`) for Off-Flavors.
  - `formatForShare()`: Generates a formatted plain-text summary for the Android Sharesheet.
- [x] **Room Database (`BeerworksDatabase` v12 → v13)**:
  - `ScoresheetEntity` (`scoresheets` table) and `ScoresheetDao` (`getAllScoresheets` ordered newest-first, `getScoresheetById`, `upsert`, `deleteById`).
  - `MIGRATION_11_12` and `MIGRATION_12_13`.
- [x] **Repository & Preferences**:
  - `ScoresheetRepository` wired into `AppContainer`.
  - `BeerStyleRepository.getBjcpBeerStyles()` exposing `BJCP_2021` styles independent of the currently selected guideline.
  - `PreferencesManager.ingredientsExpanded` persisting sidebar `"Ingredients"` group expand/collapse state.

### 2. Reviews List Screen (`ReviewsScreen` / route: `"review_history"`)
- [x] **TopAppBar & Search**:
  - Title `"Reviews"`, drawer menu button, and `+` (`Icons.Default.Add`) action to create a new Scoresheet.
  - Clean search field without hint/placeholder text filtering by entry title, style name/number, or brewer.
- [x] **Scoresheet Cards (`ScoresheetCard`)**:
  - Displays Entry/Beer Name, linked/custom Style, optional Brewer, Date Evaluated, and `totalScore/50 • Tier` badge (no chevrons, no form-type badge).
  - Per-card **Share** (Android `Intent.ACTION_SEND`) and **Delete** icons tinted `MaterialTheme.colorScheme.onSurface` (white in dark mode, no red trashcan tint).
  - Delete always prompts with a confirmation `AlertDialog`.

### 3. Scoresheet Editor Screen (`ScoresheetEditorScreen` / routes: `"scoresheet_editor"`, `"scoresheet_editor/{scoresheetId}"`)
- [x] **TopAppBar & Scroll-Locked Total Score Bubble**:
  - TopAppBar with Back, Delete (when editing existing, neutral `onSurface` tint, confirmation dialog), and Save (enabled when `entryTitle.isNotBlank()`); no Share action on the editor page.
  - Scroll-locked Total Score bubble anchored directly beneath the TopAppBar showing `totalScore / 50`, active `BjcpScoringTier (range)`, and an `(i)` button opening `BjcpScoringGuideSheet` (`skipPartiallyExpanded = true`).
- [x] **Entry Details Bubble**:
  - Header row with white `18.sp` bold `"Entry Details"` title on the left and a compact date picker button (`CalendarToday` icon + date, no `"Date Evaluated"` text) on the right.
  - Required `Entry # or Beer Name *`, optional `Brewer / Brewery`, typable searchable `BJCP Beer Style` dropdown (no up/down trailing arrow; clear button when non-empty) paired on the same row with an always-visible `MenuBook` `OutlinedIconButton` (greyed out/disabled until a BJCP style is linked; opens `StyleQuickPeekSheet` when enabled).
  - **Bottle Inspection** directly below the style row using a `Checkbox` (clickable row) + optional notes field.
- [x] **Individual Section Bubbles (`Aroma`, `Appearance`, `Flavor`, `Mouthfeel`, `Overall Impression`)**:
  - Each subsection rendered in its own bubble card with white `18.sp` bold title, `(i)` info button (`SectionInfoSheet` without redundant section title or max-points chip), integer `Slider` starting at `0`, per-section tap-cycle descriptor chips above optional notes, and `Overall Comments (Optional)` under `Overall Impression`.
- [x] **0–10 Impressions & Off-Flavors**:
  - Headerless bubble above Off-Flavors containing `Stylistic Accuracy`, `Technical Merit`, and `Intangibles` sliders (`0..10`, starting at `0`).
  - `Off-Flavors & Faults` bubble with 4-state canonical `OffFlavor` chips (`Unset → Low → Med → High → Unset`) and long-press `OffFlavorQuickPeekSheet`.
- [x] **Keyboard & Bottom Sheet UX**:
  - Software keyboard starts closed on entry, opens only when tapping a text field, and collapses immediately on scroll, background tap, or interacting with sliders/chips/buttons.
  - All `ModalBottomSheet`s use `rememberModalBottomSheetState(skipPartiallyExpanded = true)` so they always dismiss on a single back press.

### 4. Accompanying Navigation, Settings & Catalog QoL Tweaks
- [x] **Sidebar Drawer (`AppDrawer`)**:
  - Renamed `"Review History"` → `"Reviews"`.
  - Added persisted expandable/collapsible `"Ingredients"` group containing `Hops`, `Malts`, `Honey`, `Pomes`, and `Yeast`.
  - Replaced dynamic discipline icon on `"Style Guide"` with `Icons.AutoMirrored.Filled.MenuBook`.
- [x] **Settings (`SettingsScreen`)**:
  - Removed `"Beerworks Reference"` card and restored `"Active Style Guideline"` card with a uniform-font dropdown selector and leading discipline emoji.
- [x] **BA Style Chips (`BaStyleEntity`)**:
  - Derived meaningful strength, color, fermentation, origin, and character descriptor tags in place of redundant `"ba"` / parent category tags.

---

## 🧪 Testing Plan
- `ScoresheetModelTest`: 4-state `IntensityLevel` cycle, all 6 `BjcpScoringTier` score boundaries, `ScoresheetEntity` round-trip serialization, and `formatForShare()`.
- `ScoresheetEditorViewModelTest`: Zero-initialized sliders, descriptor & off-flavor cycling, style linking, and save/delete persistence.
- `AppDrawerTest`: `StyleGuide` icon, `"Reviews"` label, and `ingredientItems` membership.
- Verified with `./gradlew testDebugUnitTest`.
