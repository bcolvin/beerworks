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

- [ ] **BJCP Cider Style Guidelines Support (Feature 005)**
  - Incorporate the official BJCP Cider Guidelines (Categories C1–C2: Standard Cider & Perry, Specialty Cider & Perry).
  - Support cider-specific attributes (5-tier sweetness levels, carbonation, strength tiers, apple/pear varieties, ABV).
  - Integrate into guideline switching alongside Beer and Mead with dynamic discipline icon (🍏 / Eco).
