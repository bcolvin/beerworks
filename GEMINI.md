# Beerworks Workspace Guidelines

## ⚡ Performance Engineering Standards
- **Compose Stability**: Always mark domain models and data classes used as Composable parameters (e.g. `BeerStyle`, `BaseStyleGroup`, `FilterCriteria`) with `@Immutable` (`androidx.compose.runtime.Immutable`) or `@Stable` to allow the Compose compiler to skip unchanged list items and prevent unnecessary recompositions.
- **Repository Threading**: Always offload heavy Flow data transformations, mapping, and string parsing in Repositories to `Dispatchers.Default` using `.flowOn(Dispatchers.Default)`.
- **Database Search**: Use SQLite FTS subquery indexing (`rowid IN (SELECT rowid FROM fts WHERE ... MATCH :query)`) for instant keyword search.
- **Asset Preloading**: Pre-populate Room DB from raw JSON assets on initial startup; never parse large JSON files on the main thread or during UI rendering.

## 🎨 UI & Design Conventions
- **Search Field Hint Text**: Do NOT include placeholder / hint text in search fields (`OutlinedTextField`) across the app unless explicitly requested by the user. Keep search bars clean and minimal with only the leading search icon and trailing clear button.
- **Filter Chips & "All" Option**: Do NOT add an "All" chip to filter sheets or filter strips unless explicitly asked. Filter chips are treated as toggle on/off (selected = active filter, unselected = no filter for that value). When no chips in a group are selected, all items are included by default.
- **List Item Navigation Arrows / Chevrons**: Do NOT include navigation indicator arrows / chevrons (e.g. `KeyboardArrowRight`) on card items across list pages (Style Guide, Hops, Malt, Honey, Pomes, Yeast, Off-Flavors) unless explicitly asked for the navigation arrow. List items are clickable cards with subtle elevation and container tint without trailing arrow glyphs.
- **Ingredient Card Layout Consistency**: All catalog item cards across all ingredient and sensory reference disciplines (`HopCard`, `MaltCard`, `HoneyCard`, `PomeCard`, `YeastCard`, `OffFlavorCard`, and any future additions) must adhere to a strict, uniform vertical structure:
  1. **Header Row**: Flag or sensory cue emoji + bold title on the left, with classification/category badge(s) on the top right.
  2. **Sub-Header**: Italicized botanical/scientific name and geographic origin (`"${botanicalName} • ${origin}"`) or chemical compound and perception threshold (`"${chemicalCompound} • Threshold: ${perceptionThreshold}"`).
  3. **Narrative Description**: Directly below the sub-header with no intermediate metric, spec, or attribute chips in between (max 3 lines with ellipsis). Keep detailed chemistry/genetic/physiological specs reserved for the detail screen.
  4. **Bottom Strip**: Single-row horizontal descriptor chip strip (`SingleRowChipLayout`).
