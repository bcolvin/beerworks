# 0011. Direct Preferences Seam, Unified Guideline Dispatch, and Projected Editor State

## Status
Accepted

## Context
Following Features 009–013, architectural friction emerged across three seams:
1. **Shallow preference pass-throughs**: `BeerStyleRepository` forwarded `keepScreenOn`, `temperatureUnit`, and `confirmFavoriteRemoval` 1:1 to `PreferencesManager`, and `ScoresheetRepository` forwarded `lastScoresheetDiscipline`, forcing `SettingsViewModel`, `FavoritesViewModel`, and `IngredientDetailViewModel` to depend on unrelated domain repositories for user preferences.
2. **Repeated guideline dispatch & separate style streams**: `BeerStyleRepository` repeated 4-way `when (guideline)` switches five times, exposed three separate discipline-specific getters (`getBjcpBeerStyles`, `getMeadStyles`, `getCiderStyles`), ignored the passed `guidelineType` in `getCategoryGroup()`, and leaked `currentStyleId`'s favorite flag into `getNextStyle()`.
3. **UI-embedded domain projection & duplicated vital-stat formatting**: `ScoresheetEditorScreen` (~1,537 lines) collected four raw catalog flows to filter styles/off-flavors inside Compose `remember` blocks and duplicated `StyleQuickPeekSheet` vital-stat formatting already present in `StyleDetailScreen` and `StyleCard` with inconsistent missing-value fallbacks (`"—"` vs. `"N/A"`).

## Decision
We established four architectural guardrails going forward:
1. **Direct `PreferencesManager` Seam (No Repository Pass-Throughs)**:
   - `BeerStyleRepository` exposes only `selectedGuideline` / `setGuideline` (since style catalog queries react to the active guideline); `ScoresheetRepository` exposes only persisted `Scoresheet` CRUD.
   - ViewModels that read or mutate other preferences (`SettingsViewModel`, `FavoritesViewModel`, `IngredientDetailViewModel`, `ScoresheetEditorViewModel`) inject `PreferencesManager` directly.
2. **Unified `GuidelineStyleSource` Adapter Map in `BeerStyleRepository`**:
   - All four style DAOs (`BjcpStyleDao`, `BaStyleDao`, `MeadStyleDao`, `CiderStyleDao`) sit behind a single internal `Map<GuidelineType, GuidelineStyleSource>` adapter table.
   - Style catalog callers across disciplines use the single parameterized `getStyles(guidelineType: GuidelineType)` stream instead of adding per-guideline `get*Styles()` methods.
   - `getCategoryGroup(categoryNumber, guidelineType)` queries the explicit `guidelineType` source, and `getNextStyle(currentStyleId, guidelineType)` resolves the next style's own favorite state.
3. **ViewModel-Projected Editor State & Modular UI Split**:
   - `ScoresheetEditorViewModel` synchronously projects `filteredStyles`, `linkedStyle`, and `availableOffFlavors` onto `ScoresheetEditorUiState`, so `ScoresheetEditorScreen` observes one state stream and all filtering/linking logic is JVM-testable without Compose UI tests.
   - Editor UI primitives live in sibling modules `ScoresheetSections.kt` and `ScoresheetQuickPeekSheets.kt`, keeping `ScoresheetEditorScreen.kt` focused on screen orchestration.
4. **Shared `StyleVitalStats` Formatting Module**:
   - `OG`, `FG`, `IBU`, `SRM`, and `ABV` range formatting lives in `com.bc.beerworks.util.StyleVitalStats` (`formattedOgRange`, `formattedFgRange`, `formattedIbuRange`, `formattedSrmRange`, `formattedAbvRange`) with `"—"` unified as the missing-value fallback across `StyleCard`, `StyleDetailScreen`, and `StyleQuickPeekSheet`.

## Consequences
- `SettingsViewModel` and its tests have zero dependency on `BeerStyleRepository` or style DAOs.
- Adding or modifying a beverage guideline requires registering one `GuidelineStyleSource` adapter rather than editing five `when` blocks.
- Filtering, linking, and vital-stat formatting changes are localized to one ViewModel/utility seam and verified directly by JVM unit tests.
