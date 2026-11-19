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
  - `docs/adr/0009-bjcp-scoresheets-and-form-modes.md`: Unified `ScoresheetEntity` (`scoresheets`), `ScoresheetDiscipline` (`BJCP_BEER`, `BJCP_MEAD`, `BJCP_CIDER`) with legacy `SHORT_FORM`/`LONG_FORM` normalization, `BJCP_2021`-scoped Beer style linking, and single-back `ModalBottomSheet` inline reference sheets (`skipPartiallyExpanded = true`).
- **Performance & UI Standards**: Follow `GEMINI.md`:
  - Compose `@Immutable` domain and UI state models (`Scoresheet`, `ScoresheetDiscipline`, `ScoresheetEditorUiState`, `SectionGuidelineInfo`).
  - Offload repository Flow mapping to `Dispatchers.Default` via `.flowOn(Dispatchers.Default)`.
  - No placeholder/hint text in search fields (`OutlinedTextField`).
  - Toggle `FilterChip`s on `ReviewsScreen` with no `"All"` chip (unselected = all items included).
  - No trailing navigation arrows/chevrons on list cards.
  - Neutral `onSurface` icon tinting on Share and Delete actions (no red trashcan tint).

---

## 📋 Requirements & Acceptance Criteria

### 1. Data Pipeline & Persistence
- [x] **Domain Model (`Scoresheet.kt`)**:
  - `ScoresheetDiscipline`: `BJCP_BEER` (`"BJCP Beer"`, `"Beer"`, `"🍺"`, `isSupported = true`), `BJCP_MEAD` (`"BJCP Mead"`, `"Mead"`, `"🐝"`, `isSupported = false`), and `BJCP_CIDER` (`"BJCP Cider"`, `"Cider"`, `"🍏"`, `isSupported = false`). Includes `fromStorageValue()` normalizing `"BJCP_BEER"` and legacy `"SHORT_FORM"` / `"LONG_FORM"` values to `BJCP_BEER` (removing the short-form vs. long-form split).
  - `Scoresheet`: `id`, `discipline: ScoresheetDiscipline`, `entryTitle` (Entry # or Beer Name), `brewer` (optional), `styleId` (optional `BJCP_2021` link), `styleName`, `styleNumber`, `evaluatedAtMillis`, integer section scores (`aromaScore` 0–12, `appearanceScore` 0–3, `flavorScore` 0–20, `mouthfeelScore` 0–5, `overallScore` 0–10; all starting at `0`), optional per-section notes (`aromaNotes`, `appearanceNotes`, `flavorNotes`, `mouthfeelNotes`, `overallNotes`), `bottleInspectionOk` (Boolean), `bottleInspectionNotes`, 0–10 impressions (`stylisticAccuracy`, `technicalMerit`, `intangibles`; all starting at `0`), `descriptorValues: Map<String, String>`, `offFlavorLevels: Map<String, IntensityLevel>`, and `updatedAtMillis`.
  - Computed `totalScore` (`/50`) and `scoringTier` (`BjcpScoringTier`: Outstanding 45–50, Excellent 38–44, Very Good 30–37, Good 21–29, Fair 14–20, Problematic 0–13 with official descriptions).
  - `ScoresheetDescriptor`: Section-grouped tap-cycle descriptors for `Aroma` (`Malt`, `Hops`, `Fermentation Character`), `Appearance` (`Color`, `Clarity`, `Head Size`, `Head Retention`, `Head Color`), `Flavor` (`Malt`, `Hops`, `Bitterness`, `Fermentation Character`, `Balance`, `Finish / Aftertaste`), and `Mouthfeel` (`Body`, `Carbonation`, `Warmth`, `Creaminess`, `Astringency`).
  - `IntensityLevel`: 4-state cycle (`Unset → Low → Medium → High → Unset`) for Off-Flavors.
  - `formatForShare()`: Generates a formatted plain-text summary for the Android Sharesheet prefixed with `"${discipline.displayName} Scoresheet"`.
- [x] **Room Database (`BeerworksDatabase` v12 → v13)**:
  - `ScoresheetEntity` (`scoresheets` table, retaining the `formType` column to persist `discipline.name` and decode legacy `"SHORT_FORM"` / `"LONG_FORM"` rows seamlessly) and `ScoresheetDao` (`getAllScoresheets` ordered newest-first, `getScoresheetById`, `upsert`, `deleteById`).
  - `MIGRATION_11_12` and `MIGRATION_12_13`.
- [x] **Repository & Preferences**:
  - `ScoresheetRepository` wired into `AppContainer` (exposing `getAllScoresheets()`, `getScoresheetById()`, `saveScoresheet()`, `deleteScoresheet()`).
  - `BeerStyleRepository.getBjcpBeerStyles()` exposing `BJCP_2021` styles independent of the currently selected guideline.
  - `PreferencesManager.ingredientsExpanded` persisting sidebar `"Ingredients"` group expand/collapse state.

### 2. Reviews List Screen (`ReviewsScreen` / route: `"review_history"`)
- [x] **TopAppBar, Search, Sort & Discipline Filter**:
  - Title `"Reviews"`, drawer menu button, and `+` (`Icons.Default.Add`) action to navigate to `"scoresheet_editor"`.
  - Clean search field without hint/placeholder text filtering by entry title, style name/number, brewer, or special ingredients, paired with a Sort icon (`Icons.AutoMirrored.Filled.Sort`) opening a compact `DropdownMenu` to order by `Date (Newest)`, `Date (Oldest)`, `Score (High → Low)`, or `Score (Low → High)`.
  - Horizontal toggle `FilterChip` strip (`🍺 Beer (N)`, `🐝 Mead (N)`, `🍏 Cider (N)`) displayed below the search bar whenever `totalCount > 0` (no `"All"` chip per `GEMINI.md`; unselected = all disciplines included, multi-select = union of selected disciplines, combined with text search).
  - Empty states show `"No Reviews Yet"` (`totalCount == 0`) or `"No Matching Scoresheets"` (when filters/search match nothing) and always provide the `"New Scoresheet"` button.
- [x] **Scoresheet Cards (`ScoresheetCard`)**:
  - Four-row card layout (no trailing navigation chevrons):
    1. Date Evaluated + Beverage Discipline (`date • emoji + shortLabel`, left) + Share and Delete icons (right)
    2. Optional Brewer / Meadmaker / Cidery
    3. Entry # or Name (left) + `totalScore/50 • Tier` badge (right)
    4. Optional linked/custom Style (`bodySmall`)
  - Per-card **Share** (Android `Intent.ACTION_SEND`) and **Delete** icons tinted `MaterialTheme.colorScheme.onSurface` (white in dark mode, no red trashcan tint).
  - Delete always prompts with a confirmation `AlertDialog`.

### 3. Scoresheet Editor Screen (`ScoresheetEditorScreen` / routes: `"scoresheet_editor"`, `"scoresheet_editor/{scoresheetId}"`)
- [x] **TopAppBar, Unsaved-Changes Guard, Discipline Switcher & Scroll-Locked Total Score Bubble**:
  - TopAppBar with Back, Delete (when editing existing, neutral `onSurface` tint, confirmation dialog), and Save (enabled only when `discipline.isSupported && entryTitle.isNotBlank()`); no Share action on the editor page. Title reflects mode and discipline (`"New <Discipline> Scoresheet"` / `"Edit <Discipline> Scoresheet"`).
  - Attempting to leave via the TopAppBar Back button or system Back gesture when `hasUnsavedChanges` is `true` prompts a `"Discard Unsaved Changes?"` confirmation `AlertDialog`.
  - On new scoresheets (`isNew == true`), a `SingleChoiceSegmentedButtonRow` at the top of the editor lets the user switch between `🍺 Beer`, `🐝 Mead`, and `🍏 Cider` (all three supported; dirty-draft confirmation resets judging data when switching).
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
  - Software keyboard starts closed on entry, opens when tapping a text field (ignoring Compose's programmatic `BringIntoView` scroll from `.imePadding()`), and collapses on genuine user vertical drags (`NestedScrollSource.UserInput`), background tap, or interacting with sliders/chips/buttons.
  - All `ModalBottomSheet`s use `rememberModalBottomSheetState(skipPartiallyExpanded = true)` so they always dismiss on a single back press.

### 4. Accompanying Navigation, Settings & Catalog QoL Tweaks
- [x] **Sidebar Drawer & Navigation (`AppDrawer` / `MainActivity`)**:
  - Renamed `"Review History"` → `"Reviews"` (preserving route `"review_history"` with `Icons.Default.History`, navigating to `"scoresheet_editor"` for new sheets and `"scoresheet_editor/{scoresheetId}"` when tapping an existing card).
  - Added persisted expandable/collapsible `"Ingredients"` group (`PreferencesManager.ingredientsExpanded`) containing `Hops`, `Malts`, `Honey`, `Pomes`, and `Yeast`, highlighting the parent `"Ingredients"` item when collapsed and an ingredient route is active.
  - Replaced dynamic discipline icon on `"Style Guide"` with `Icons.AutoMirrored.Filled.MenuBook`.
- [x] **Settings (`SettingsScreen`)**:
  - Removed `"Beerworks Reference"` card and restored `"Active Style Guideline"` card with a uniform-font dropdown selector and leading discipline emoji.
- [x] **BA Style Chips (`BaStyleEntity`)**:
  - Derived meaningful strength, color, fermentation, origin, and character descriptor tags in place of redundant `"ba"` / parent category tags.

---

## 🧪 Testing Plan
- `ScoresheetModelTest`: 4-state `IntensityLevel` cycle, all 6 `BjcpScoringTier` score boundaries, `ScoresheetEntity` round-trip serialization, `formatForShare()`, and `ScoresheetDiscipline.fromStorageValue()` legacy `"SHORT_FORM"` / `"LONG_FORM"` normalization.
- `ScoresheetEditorViewModelTest`: Zero-initialized sliders, descriptor & off-flavor cycling, style linking, save/delete persistence, discipline switching, `hasUnsavedChanges` tracking/reverts, and ignoring discipline changes when editing existing saved scoresheets.
- `ReviewsViewModelTest`: Multi-select discipline toggle filtering, per-discipline counts, combined discipline + search query filtering, and `ReviewsSortOption` (`DATE_DESC`, `DATE_ASC`, `SCORE_DESC`, `SCORE_ASC`).
- `AppDrawerTest`: `StyleGuide` icon, `"Reviews"` label, and `ingredientItems` membership.
- Verified with `./gradlew --no-daemon --offline testDebugUnitTest assembleDebug`.
