# 0007. Canonical Yeast Strains with Multi-Discipline Fermentation Profiles and Commercial Equivalents

## Context
Brewers, meadmakers, and cidermakers select yeast based on fermentation kinetics (apparent attenuation %, flocculation tier, alcohol tolerance, optimal temperature range), genetic markers (STA1, POF), and sensory characteristics. The same biological strains and isolates are produced and packaged under dozens of commercial product codes across various laboratories (White Labs, Wyeast, Fermentis, Lallemand, Imperial, Omega, Mangrove Jack's, Escarpment, Cellar Science). Furthermore, Beerworks supports three distinct beverage disciplines: Beer, Mead, and Cider.

Cataloging commercial laboratory SKUs as independent top-level entities would duplicate identical strains across the database, fracture cross-references to style guidelines, and clutter search results.

## Decision
We decided to:
1. Model **Canonical Yeast Strains** (e.g., Chico / American Ale, Weihenstephan Weizen, Montpellier / EC-1118) as the primary domain entities (`YeastStrain`), implementing `Ingredient` to inherit the unified repository pattern established in ADR 0006.
2. Embed commercial lab offerings and product codes (`CommercialYeastEquivalent`) within each canonical strain, indexing laboratory names, product codes, and brand trade names into SQLite FTS4 so queries for commercial codes (e.g., "US-05", "WLP001", "1056", "EC-1118") immediately resolve to their canonical strain.
3. Span all three beverage disciplines (Beer, Mead, Cider) with bidirectional style cross-referencing via `YeastStyleCrossRef`.
4. Represent genetic traits compactly as `STA1+` / `STA1-` (*Diastaticus*) and `POF+` / `POF-` (*Phenolic Off-Flavor*) without alarmist warning callouts or UI clutter.
5. Store fermentation temperature bounds with a user-configurable unit preference (defaulting to Fahrenheit with a Celsius toggle in Settings).
6. Adopt an 8-axis fermentation flavor radar model plotting: Clean/Neutral, Fruity Esters, Spicy Phenols, Tart/Acidity, Funky/Earthy, Sulfury/Crisp, Alcohol/Warmth, and Mouthfeel/Sweetness Retention.

## Consequences
- Eliminates database bloat while maintaining instant search discoverability for any commercial manufacturer code.
- Fully integrates with `BaseIngredientRepository` for seamless multi-guideline style linking and FTS query sanitization.
- Completes the fermentation ingredient suite across Beer, Cider, and Mead disciplines.
