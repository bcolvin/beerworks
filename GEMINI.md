# Beerworks Workspace Guidelines

## ⚡ Performance Engineering Standards
- **Compose Stability**: Always mark domain models and data classes used as Composable parameters (e.g. `BeerStyle`, `BaseStyleGroup`, `FilterCriteria`) with `@Immutable` (`androidx.compose.runtime.Immutable`) or `@Stable` to allow the Compose compiler to skip unchanged list items and prevent unnecessary recompositions.
- **Repository Threading**: Always offload heavy Flow data transformations, mapping, and string parsing in Repositories to `Dispatchers.Default` using `.flowOn(Dispatchers.Default)`.
- **Database Search**: Use SQLite FTS subquery indexing (`rowid IN (SELECT rowid FROM fts WHERE ... MATCH :query)`) for instant keyword search.
- **Asset Preloading**: Pre-populate Room DB from raw JSON assets on initial startup; never parse large JSON files on the main thread or during UI rendering.

## 🎨 UI & Design Conventions
- **Search Field Hint Text**: Do NOT include placeholder / hint text in search fields (`OutlinedTextField`) across the app unless explicitly requested by the user. Keep search bars clean and minimal with only the leading search icon and trailing clear button.
- **Filter Chips & "All" Option**: Do NOT add an "All" chip to filter sheets or filter strips unless explicitly asked. Filter chips are treated as toggle on/off (selected = active filter, unselected = no filter for that value). When no chips in a group are selected, all items are included by default.
- **List Item Navigation Arrows / Chevrons**: Do NOT include navigation indicator arrows / chevrons (e.g. `KeyboardArrowRight`) on card items across list pages (Style Guide, Hops, Malt, Honey) unless explicitly asked for the navigation arrow. List items are clickable cards with subtle elevation and container tint without trailing arrow glyphs.
