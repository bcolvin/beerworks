# 📋 Beerworks Backlog & Idea Board

This backlog stores feature ideas, user requests, and future capabilities before they are flushed out into formal Spec-Driven Development specs (`features/00X-feature-name/spec.md`).

---

## ✅ Completed Ideas & Enhancements (Implemented in Feature 001)

- [x] **Collapse Beer Styles by Category**: Expandable/collapsible base style headers (`1. Standard American Beer`, `21. IPA`) with press-and-hold inline sub-style expansion and context-aware back navigation state.
- [x] **Multi-Column & Commercial Examples Search**: Search across style numbers (`1A`, `7B`), style names, commercial examples (`Bud Light`, `Guinness`, `Pliny`), and hop/malt/yeast descriptors (`Citra`, `Cascade`, `Pilsner`).
- [x] **SRM Color Gradient Visual Enhancements**: Custom vertical SRM gradient scrollbar with dynamic foam head position thumb indicator, full-width horizontal bottom SRM gradient banner, and smart N/A range filter exclusion.
- [x] **Left Slide-Out Navigation & Settings**: Slide-out navigation drawer (`Style Guide`, `Hops`, `Malt`, `Review History`, `Settings`) and dedicated `SettingsScreen` for BJCP 2021 / BA 2026 guideline selection.

---

## 🎯 Planned Roadmap Items (For Future Breakdown)

- [x] **Hop Profile Database & Style Cross-Reference (Feature 002)**
  - Search hops by name, origin, or flavor descriptor (citrus, dank, noble).
  - Cross-reference hop profiles directly with BJCP / BA style guidelines.

- [x] **Malt Profile Database & Flavor Characteristics (Feature 003)**
  - Explore base & specialty malts, EBC/Lovibond color ratings, extract potential, and diastatic power.
  - Typical usage roles, functional tags, and 8-axis malt flavor radar chart.
  - Notable maltsters & commercial examples (Briess, Weyermann, Crisp, Dingemans, etc.).
  - Cross-reference malt profiles directly with BJCP / BA style guidelines.

- [ ] **Side-by-Side Comparison Matrix (Beer Styles, Hops & Malts)**
  - Compare 2–3 beer styles side-by-side across vital statistics (ABV, IBU, SRM), appearance, and sensory profiles.
  - Compare 2–3 hop varieties side-by-side with overlaid/contrasting 8-axis sensory radar charts, alpha/beta acid ranges, and common beer styles.
  - Future expansion: Compare 2–3 malt profiles side-by-side (color Lovibond/EBC, extraction %, flavor contributions).

- [ ] **Digital BJCP & BA Scoresheet Entry**
  - Fill out shortform & longform scoresheets (Aroma, Appearance, Flavor, Mouthfeel, Overall Impression).
  - Score calculations and automated BJCP category validation.

- [ ] **AI / OCR Photo Digitizer for Handwritten Scoresheets**
  - Capture a photo of a physical scoresheet and parse it into a digital scoresheet automatically.

- [ ] **Searchable Judging Log & Firebase Sync**
  - Search past judging entries by date, competition, style, or score.
  - Persist scoresheets safely in Firebase DataStore.

- [ ] **Social Judge Comparison & Flight Consensus**
  - Compare scoring sheets side-by-side with co-judges during competition flights.

- [x] **BJCP Mead Style Guidelines Support (Feature 004)**
  - Incorporate the official BJCP Mead Guidelines (Categories M1–M4: Traditional Mead, Fruit Mead, Spiced Mead, Specialty Mead).
  - Support mead-specific attributes: sweetness (Dry, Semi-Sweet, Sweet), carbonation (Still, Pétillant, Sparkling), and strength (Hydromel, Standard, Sack).
  - Integrate into guideline switching alongside BJCP 2021 Beer and BA 2026 Beer with dynamic discipline icons.

- [x] **BJCP Cider Style Guidelines Support (Feature 005)**
  - Incorporate the official BJCP Cider Guidelines (Categories C1–C2: Standard Cider & Perry, Specialty Cider & Perry).
  - Support cider-specific attributes (5-tier sweetness levels, carbonation, strength tiers, apple/pear varieties, ABV).
  - Integrate into guideline switching alongside Beer and Mead with dynamic discipline icon (🍏 / Nature).

- [x] **Honey Profile Database & Varietal Sensory Profiles (Feature 006)**
  - Comprehensive reference of canonical varietal honeys (Orange Blossom, Clover, Tupelo, Meadowfoam, Buckwheat, etc.).
  - Pfund scale (mm) and USDA color classifications (Water White to Dark Amber) with visual color swatches.
  - Fructose/Glucose ratios, crystallization tendencies (Slow, Moderate, Rapid), moisture %, and gravity yields.
  - Custom 8-axis honey sensory radar chart (Floral, Fruity, Herbal, Spicy, Sweet, Nutty, Earthy, Animal/Pungent).
  - Nutrient demand and volatile aroma preservation in meadmaking.
  - Bidirectional relational cross-referencing between honey varieties and BJCP Mead styles (M1–M4), Braggots, and Cysers.

- [x] **Pome Varieties Database & Sensory Profiles (Feature 007)**
  - Comprehensive reference of canonical cider apple and perry pear varieties (Dabinett, Kingston Black, Yarlington Mill, Harrison, Thorn, etc.).
  - Long Ashton 4-tier classification (Bittersweet, Bittersharp, Sharp, Sweet) with visual 2×2 matrix gauge.
  - Pomological & chemical metrics: Acidity (% malic acid), Tannins (% polyphenols and Low/Med/High tiers), Specific Gravity (SG) range, and ripening season.
  - Perry pear sorbitol content, pressing dynamics, and milling behavior.
  - Custom 8-axis pome sensory radar chart (Acidity/Sharpness, Tannin/Bitterness, Fresh Fruit, Baked/Ripe Fruit, Citrus & Tropical, Floral & Herbal, Earthy & Farmhouse, Sweet & Confectionary).
  - Navigation drawer integration with `Nature` icon for Pomes and `Grass` (wheat/stalks) icon for Malt.
  - Bidirectional relational cross-referencing between pome varieties and BJCP Cider styles (C1A–C2F) and Mead Cysers (M2B).

- [x] **Yeast Strains Database & Fermentation Profiles (Feature 008)**
  - Comprehensive catalog of ~50 canonical brewing, cider, and mead yeast strains (American Ale, English, Belgian, German/Lager, Kveik, Wild/Bacteria, Mead/Wine, Cider).
  - Commercial lab cross-referencing (White Labs, Wyeast, Fermentis, Lallemand, Imperial, Omega, etc.) with sub-millisecond FTS search on vendor codes (e.g. "US-05", "WLP001", "1056").
  - Physiological metrics: Attenuation % range, Flocculation tier, Alcohol tolerance (% ABV), and optimum fermentation temperature range.
  - User-configurable temperature unit preference (Fahrenheit / Celsius) in Settings.
  - Clean genetic status tags (`STA1+/-`, `POF+/-`) without alarmist clutter.
  - Custom 8-axis yeast sensory radar chart (Clean, Fruity, Spicy, Tart, Funky, Sulfury, Alcohol, Mouthfeel).
  - Navigation drawer integration with `BubbleChart` icon.
  - Bidirectional relational cross-referencing between yeast strains and Beer, Mead, and Cider styles.

---

## 🏗️ Planned Architectural Deepening (Upcoming Enhancements)

- [ ] **Consolidate Shallow Ingredient Detail ViewModels (Architecture Candidate 1)**
  - Replace five separate pass-through view models (`HopDetailViewModel`, `MaltDetailViewModel`, `HoneyDetailViewModel`, `PomeDetailViewModel`, `YeastDetailViewModel`) with a single deep generic `IngredientDetailViewModel<T : Ingredient>`.
  - Maximize leverage from the unified `IngredientRepository` seam (ADR 0006) across all ingredient disciplines.
  - Unify detail lifecycle state emission, substitute resolution, and style favorite toggling into one thoroughly tested interface.

- [ ] **Deepen Style-to-Ingredient Association in StyleDetailViewModel (Architecture Candidate 2)**
  - Consolidate five separate ingredient repositories and five separate StateFlow streams in `StyleDetailViewModel` into a unified `StyleIngredients` stream aggregated at the data seam.
  - Shrink the `StyleDetailViewModel` constructor interface from six dependencies down to two (`BeerStyleRepository`, `styleId`).
  - Isolate ingredient cross-referencing entirely within the data layer so future ingredients require zero ViewModel or navigation wiring changes.

- [ ] **Decompose Monolithic DatabaseInitializer into Focused Domain Seeders (Architecture Candidate 4)**
  - Decompose the 700-line procedural seeder into isolated, self-contained `DomainSeeder` modules (`StyleSeeder`, `HopSeeder`, `MaltSeeder`, `HoneySeeder`, `PomeSeeder`, `YeastSeeder`).
  - Encapsulate raw JSON DTOs and Gson mapping logic privately within each seeder.
  - Coordinate seeding through a lightweight asynchronous runner executing independent datasets in parallel on the background dispatcher pool.
