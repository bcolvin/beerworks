# Independent Datasets with Unified Domain Schema for Beverage Disciplines

We decided to maintain distinct, isolated raw datasets and database entities for alternative fermented beverage disciplines (e.g., `mead_styles.json` / `MeadStyleEntity` and future `cider_styles.json`) while mapping them to a unified domain model (`BeerStyle` / beverage style) with discipline-specific nullable extensions (e.g., sweetness, carbonation, and strength classifications). 

Rather than polluting the core beer dataset or maintaining completely disconnected UI screens, this approach isolates distinct guideline life-cycles and asset pre-population while enabling the existing catalog search, base style expansion, favorites, and detail screens to render mead and cider seamlessly. Guideline switching is unified through the existing header press-and-hold selector and Settings screen.
