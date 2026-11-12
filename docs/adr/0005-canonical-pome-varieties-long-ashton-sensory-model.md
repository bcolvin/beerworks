# 0005. Canonical Pome Varieties with Long Ashton Classification and 8-Axis Sensory Model

## Context
Following the implementation of hops, malts, and honeys, as well as the BJCP Cider Style Guidelines (Feature 005), cidermakers and BJCP cider judges need an offline reference for cider apples and perry pears:
- Pome fruit in cidermaking is characterized by titratable acidity (predominantly malic acid) and polyphenol tannins. The internationally recognized **Long Ashton (Barker) classification** categorizes fruit into four classes: **Bittersweet**, **Bittersharp**, **Sharp**, and **Sweet** using the 0.45% acidity and 0.20% tannin thresholds.
- Perry pears (*Pyrus communis*) differ from cider apples (*Malus domestica*) by containing significant concentrations of non-fermentable **sorbitol** (contributing body and unfermentable residual sweetness) and volatile pressing dynamics (narrow ripening windows, high pectin, rapid browning).
- While modern ciders frequently utilize dessert/culinary fruit, traditional styles (English C1B, French C1C, Traditional Perry C1E) require vintage bittersweet and bittersharp specimens for tannin structure and acidity balance.
- Cidermakers evaluate organoleptic properties across crisp acidity, drying astringency, fresh and baked fruit notes, floral/herbal aromatics, and rustic earthy notes.

## Decision
We decided to:
1. Model cider apples and perry pears in a unified domain entity (`PomeVarietyEntity`) under the collective domain term **Pomes**, distinguished by a `FruitType` enum (`Apple` vs. `Pear`).
2. Classify varieties using the **Long Ashton 4-tier system** (`Bittersweet`, `Bittersharp`, `Sharp`, `Sweet`) based on acidity and tannin thresholds, supplemented by qualitative tannin levels (`Low`, `Medium`, `High`), Specific Gravity ranges (`sgMin`, `sgMax`), harvest ripening season (`Early`, `Mid`, `Late`), and optional sorbitol metrics for perry pears.
3. Quantify organoleptic profiles using a standardized **8-axis Pome Sensory Radar** (*Acidity / Sharpness*, *Tannin / Bitterness*, *Fresh Fruit*, *Baked / Ripe Fruit*, *Citrus & Tropical*, *Floral & Herbal*, *Earthy & Farmhouse*, and *Sweet & Confectionary* rated 0.0–5.0).
4. Provide a visual **Long Ashton Matrix Gauge** on pome detail screens illustrating where the fruit lands in the 2×2 acid/tannin matrix across the 0.45% acid and 0.20% tannin thresholds.
5. Create a bidirectional relational join table (`PomeStyleCrossRef`) connecting varieties to BJCP Cider styles (Categories C1–C2) and cross-discipline styles (e.g. Mead Cyser M2B).
6. Index variety names, botanical species, origins, descriptions, descriptors, and cidermaking guidance in SQLite FTS4 for sub-millisecond search.
7. Integrate **"Pomes"** into the navigation drawer using `Icons.Default.Nature`, while switching the Malt icon to `Icons.Default.Grass` (wheat/stalks).

## Consequences
- Maintains consistency with established ingredient architectures (Hops, Malt, Honey) while accurately modeling the science of cider pomology.
- Provides immediate visual and educational value to BJCP judges and home cidermakers through the Long Ashton 2×2 matrix gauge and 8-axis sensory radar.
- Requires Room database migration from version 7 to 8 and asset preloading of bundled `pomes.json`.
