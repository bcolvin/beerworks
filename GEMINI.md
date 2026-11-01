# Beerworks Workspace Guidelines

## ⚡ Performance Engineering Standards
- **Compose Stability**: Always mark domain models and data classes used as Composable parameters (e.g. `BeerStyle`, `BaseStyleGroup`, `FilterCriteria`) with `@Immutable` (`androidx.compose.runtime.Immutable`) or `@Stable` to allow the Compose compiler to skip unchanged list items and prevent unnecessary recompositions.
- **Repository Threading**: Always offload heavy Flow data transformations, mapping, and string parsing in Repositories to `Dispatchers.Default` using `.flowOn(Dispatchers.Default)`.
- **Database Search**: Use SQLite FTS subquery indexing (`rowid IN (SELECT rowid FROM fts WHERE ... MATCH :query)`) for instant keyword search.
- **Asset Preloading**: Pre-populate Room DB from raw JSON assets on initial startup; never parse large JSON files on the main thread or during UI rendering.
