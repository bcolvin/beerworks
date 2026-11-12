# Canonical Honey Varieties with Pfund Scale and 8-Axis Sensory Profiles

## Context
Following the addition of hops and malts, as well as the BJCP Mead Guidelines (Feature 004), meadmakers and brewers require a comprehensive reference for varietal honeys. Honey characteristics differ significantly from malted grains:
- Color in brewing malts is quantified in Degrees Lovibond (°L) or EBC, whereas honey is globally and historically standardized in the beekeeping and meadmaking communities using the **Pfund scale** (optical millimeters 1–140 mm) categorized into seven USDA visual color grades (*Water White* through *Dark Amber*).
- Rather than starch conversion and diastatic power, honey's fermentation dynamics depend on sugar ratios (Fructose-to-Glucose ratio dictating crystallization tendency and perceived sweetness), moisture content, and delicate volatile aromatic retention through primary fermentation.
- Sensory characteristics in honey encompass floral, fruit, herbal, and phenolic nuances derived directly from botanical blossoms or aphid honeydew secretions.

## Decision
We decided to:
1. Model honey as **canonical varietals** (`HoneyVarietyEntity`, e.g. *Orange Blossom*, *Meadowfoam*, *Tupelo*, *Buckwheat*, *Heather*) based on botanical source and floral classification (`Monofloral`, `Polyfloral`, `Honeydew`) rather than commercial apiary brand SKUs.
2. Store color ranges using optical **Pfund millimeters (min/max mm)** mapped to official **USDA color classifications**, providing both descriptive badges and visual color swatches.
3. Quantify flavor and aroma using a standardized **8-axis Honey Sensory Radar** (*Floral*, *Fruity*, *Herbal & Fresh*, *Spicy & Warm*, *Sweet & Confectionary*, *Nutty & Woody*, *Earthy & Mineral*, and *Animal & Pungent*) based on the UC Davis Honey Flavor Wheel and BJCP Mead Exam criteria.
4. Establish bidirectional relational links (`HoneyStyleCrossRef`) connecting honey varieties directly to BJCP Mead styles (Categories M1–M4), Braggots, and Cysers.
5. Index names, botanical species, origins, descriptors, and tasting notes in SQLite FTS4 for sub-millisecond offline search.

## Consequences
- Preserves domain clarity and scientific accuracy for BJCP mead judges and home meadmakers/brewers.
- Reuses the existing polished UI patterns from `Hops` and `Malt` (borderless pill search, quick filters, detail cards, radar charts) while honoring honey-specific metrics.
- Requires Room database migration from version 6 to 7 and asynchronous asset preloading of `honeys.json`.
