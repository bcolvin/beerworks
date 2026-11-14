# Feature Spec: 009 Favorites Catalog & Cross-Discipline Reference

---

## 🎯 High-Level Goal
Provide a unified, offline-first **Favorites** screen accessible from the navigation drawer (directly above Review History) that aggregates all user-favorited **Beverage Styles** (across BJCP 2021 Beer, BA 2026 Beer, BJCP Mead, and BJCP Cider guidelines) and **Ingredients** (Hops, Malts, Honey, Pomes, and Yeast) into grouped discipline sections with instant keyword search, dynamic discipline filter chips, and right-to-left swipe-to-remove with an optional confirmation dialog.

---

## 👤 User Story
**As a** Brewer, Meadmaker, Cidermaker, or Beverage Judge  
**I want to** view all styles and ingredients I have favorited across the entire app in one place, filter them by discipline or keyword, and quickly remove items via swipe  
**So that** I can rapidly reference my go-to beverage styles and recipe ingredients without switching back and forth between separate catalog screens and guidelines.

---

## 📚 Domain & Architecture References
- **Domain Vocabulary**: Strictly adhere to `GLOSSARY.md` (`Favorite`, `Beverage Discipline`, `Beer Style`, `Mead Style`, `Cider Style`, `Ingredient`, `Hop Variety`, `Malt Variety`, `Honey Variety`, `Pome Variety`, `Yeast Strain`).
- **Performance & UI Standards**: Follow `GEMINI.md`:
  - Compose `@Immutable` state models.
  - Offload Flow combining, filtering, and sorting to `Dispatchers.Default` via `.flowOn(Dispatchers.Default)`.
  - No placeholder/hint text in search fields (`OutlinedTextField`).
  - No `"All"` chip in filter strips (chips toggle on/off; when none are selected, all non-empty disciplines are shown).
  - No trailing navigation arrows/chevrons on cards; reuse standard catalog card components (`StyleCard`, `HopCard`, `MaltCard`, `HoneyCard`, `PomeCard`, `YeastCard`).

---

## 📋 Requirements & Acceptance Criteria

### 1. Data & Persistence Layer
- [x] **Cross-Guideline Favorite Styles Resolution**:
  - Add `getAllFavorites(): Flow<List<FavoriteStyleEntity>>` to `FavoriteStyleDao` (`SELECT * FROM favorite_styles`).
  - Expose `getAllFavoriteStyles(): Flow<List<BeerStyle>>` in `BeerStyleRepository` that resolves favorited styles across all four guideline tables (`bjcp_styles`, `ba_styles`, `mead_styles`, `cider_styles`) regardless of the currently active Style Guide guideline.
- [x] **Favorite Ingredients Resolution**:
  - Expose flows to resolve favorited `HopVariety`, `MaltVariety`, `HoneyVariety`, `PomeVariety`, and `YeastStrain` items from `favorite_ingredients` (`FavoriteIngredientDao`).
- [x] **Canonical Ordering (No DB Migration)**:
  - Sections are ordered in canonical drawer order: **Styles → Hops → Malts → Honey → Pomes → Yeast**.
  - Within **Styles**, sort by `GuidelineType` ordinal (`BJCP_2021`, `BA_2026`, `BJCP_MEAD`, `BJCP_CIDER`), then category number, then style number.
  - Within each **Ingredient** section (`Hops`, `Malts`, `Honey`, `Pomes`, `Yeast`), sort alphabetically by `name`.
- [x] **Swipe-to-Remove Confirmation Preference**:
  - Add `confirmFavoriteRemoval: Flow<Boolean>` (defaulting to `true`) and `setConfirmFavoriteRemoval(enabled: Boolean)` to `PreferencesManager` and `DefaultPreferencesManager` (persisted in DataStore under `"confirm_favorite_removal"`).

### 2. Navigation Drawer & Settings Integration
- [x] **Navigation Drawer Entry (`AppDrawer.kt`)**:
  - Add `data object Favorites : DrawerDestination("favorites", "Favorites", Icons.Default.Favorite)` to `DrawerDestination`.
  - Position `Favorites` in `DrawerDestination.items` directly above `ReviewHistory` (`StyleGuide, Hops, Malt, Honey, Pomes, Yeast, Favorites, ReviewHistory, Settings`).
- [x] **Settings Toggle (`SettingsScreen.kt` & `SettingsViewModel.kt`)**:
  - Add a **"Confirm Favorite Removal"** card with a `Switch` bound to `confirmFavoriteRemoval` so users who previously checked `"Always remove without asking"` can re-enable the confirmation prompt at any time.

### 3. Favorites Screen (`FavoritesScreen` / route: `"favorites"`)
- [x] **Top App Bar**:
  - Title `"Favorites"` with bold typography and navigation drawer menu icon (`Icons.Default.Menu`).
- [x] **Search Bar**:
  - Clean `OutlinedTextField` with leading search icon (`Icons.Default.Search`) and trailing clear button (`Icons.Default.Clear` when query is non-empty), with **no placeholder/hint text**.
  - Filters favorited items across all disciplines by matching name, style number/category, origin, botanical/scientific name, description, or descriptors.
- [x] **Dynamic Discipline Filter Chip Strip**:
  - Horizontal scrollable row of toggleable `FilterChip`s (without an `"All"` chip).
  - Only displays chips for disciplines that currently have at least 1 favorited item (before search/chip filtering), labeled with their total count (e.g., `Styles (3)`, `Hops (2)`, `Malts (1)`).
  - Toggling one or more chips restricts the displayed sections to the selected discipline(s); when no chips are selected, all non-empty discipline sections are shown.
- [x] **Grouped Discipline Sections & Standard Cards**:
  - Each visible discipline with matching items renders a section header with its discipline icon (`Icons.Default.SportsBar`, `Icons.Default.Grass`, `Icons.Default.CandlestickChart`, `Icons.Default.Hive`, `Icons.Default.Nature`, `Icons.Default.BubbleChart`) and title, followed by its cards:
    - **Styles**: `StyleCard` -> tap navigates to `style_detail/{styleId}/{guidelineType}`
    - **Hops**: `HopCard` -> tap navigates to `hop_detail/{hopId}`
    - **Malts**: `MaltCard` -> tap navigates to `malt_detail/{maltId}`
    - **Honey**: `HoneyCard` -> tap navigates to `honey_detail/{honeyId}`
    - **Pomes**: `PomeCard` -> tap navigates to `pome_detail/{pomeId}`
    - **Yeast**: `YeastCard` -> tap navigates to `yeast_detail/{yeastId}`
- [x] **Right-to-Left Swipe-to-Remove & Confirmation Modal**:
  - Each card on the Favorites page is wrapped in a `SwipeToDismissBox` configured for right-to-left (`enableDismissFromStartToEnd = false`, `enableDismissFromEndToStart = true`) swipe with an empty `backgroundContent` (no trashcan icon showing behind cards).
  - When `confirmFavoriteRemoval == true`:
    - Swiping a card triggers an `AlertDialog`:
      - Title: `"Remove from Favorites?"`
      - Text: `"Remove <Item Name> from your favorites?"` plus a clickable row with a `Checkbox` labeled `"Always remove without asking"`.
      - Buttons: `"Cancel"` (dismisses dialog and snaps the card back into place) and `"Remove"` (removes the item from favorites, and if the checkbox is checked, persists `confirmFavoriteRemoval = false`).
  - When `confirmFavoriteRemoval == false`:
    - Swiping immediately removes the item from favorites without showing the dialog.
- [x] **Empty States**:
  - **No Favorites Saved**: When the user has zero favorited items across the app, hide the search bar and chip strip and display a centered empty state with `Icons.Default.FavoriteBorder`, bold title `"No Favorites Yet"`, and subtitle `"Tap the heart icon on any style or ingredient detail page to save it here for quick reference."`
  - **No Matching Filter Results**: When the user has favorites saved, but the current search query or selected discipline chips match zero items, display a centered empty state with bold title `"No matching favorites"`, subtitle `"Try adjusting your search query or discipline filters."`, and a `"Reset Filters"` button.

---

## 📱 User Interface & Visuals
- **Drawer Item**: `"Favorites"` with `Icons.Default.Favorite` between `"Yeast"` and `"Review History"`.
- **Section Headers**: Row with primary-tinted discipline icon, bold title (e.g. `"Styles"`, `"Hops"`), and subtle item count badge.
- **Swipe Behavior**: Right-to-left swipe gesture (`SwipeToDismissBox`) with no background tint or icon behind cards so translucent catalog cards retain their normal surface appearance.

---

## 🧪 Verification & Testing Strategy
- **Unit Tests**:
  - `BeerStyleRepositoryTest`: Verify `getAllFavoriteStyles()` returns favorited styles across BJCP 2021, BA 2026, BJCP Mead, and BJCP Cider guidelines in canonical order.
  - `FavoritesViewModelTest`: Verify reactive aggregation across styles and all 5 ingredient repositories, keyword search filtering, discipline chip toggling, and swipe-remove confirmation / preference updates.
  - `SettingsViewModelTest`: Verify `confirmFavoriteRemoval` state and toggle updates.
- **UI / Compose Tests**:
  - Verify `AppDrawer` renders `"Favorites"` above `"Review History"`.
  - Verify `FavoritesContent` empty states, discipline chip counts, card rendering, and confirmation dialog flow.
