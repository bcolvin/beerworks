# 0010. Polymorphic `Scoresheet` Interface Backed by Single Unified Room Table

## Status
Accepted

## Context
Feature 011 introduced `Scoresheet` as a flat data class tailored to BJCP Beer (`Aroma 0–12`, `Appearance 0–3`, `Flavor 0–20`, `Mouthfeel 0–5`, `Overall Impression 0–10`). Adding BJCP Mead introduces a distinct 4-section 50-point format (`Aroma 0–10`, `Appearance 0–6`, `Flavor 0–24`, `Overall Impression 0–10` with no separate `Mouthfeel` score section) plus Mead-specific entrant declarations (`declaredCarbonation`, `declaredSweetness`, `declaredStrength`, `specialIngredients`).

## Decision
We chose to model `Scoresheet` in the domain layer as a `@Immutable sealed interface Scoresheet` with discipline-specific implementations (`BeerScoresheet`, `MeadScoresheet`, and future `CiderScoresheet`), while persisting all disciplines in the single existing `scoresheets` Room table (`ScoresheetEntity`) extended via `MIGRATION_13_14` with non-null default columns for Mead metadata (`declaredCarbonation`, `declaredSweetness`, `declaredStrength`, `specialIngredients`).

## Considered Options
1. **Separate Room tables per discipline (`beer_scoresheets`, `mead_scoresheets`, ...)**: Rejected because `ReviewsScreen` queries, sorts, filters, and searches all evaluations in one chronological feed; separate tables would require multi-table UNION queries or reactive multi-flow merges for every list and search operation.
2. **Single flat `Scoresheet` data class with nullable/unused fields everywhere**: Rejected because Beer-only fields (`mouthfeelScore`, `mouthfeelNotes`) and Mead-only declarations (`declaredCarbonation`, `declaredSweetness`, `declaredStrength`, `specialIngredients`) would leak across disciplines, forcing callers and `formatForShare()` to branch defensively on invalid combinations.

## Consequences
- Domain and UI callers get compile-time exhaustiveness (`when (sheet) { is BeerScoresheet -> ...; is MeadScoresheet -> ... }`) and discipline-accurate section maximums and `formatForShare()` output.
- `ScoresheetEntity` carries columns for all supported disciplines (with unused discipline columns defaulted to `0` or `""`), and `toDomainModel()` dispatches on `ScoresheetDiscipline.fromStorageValue(formType)` to reconstruct the matching `Scoresheet` subtype.
