# Feature Spec: 001 Style Guidelines Reference Viewer

---

## 🎯 High-Level Goal
Provide an offline-first, sub-millisecond reference viewer for the official BJCP 2021 and Brewers Association 2026 beer style guidelines with multi-level expandable style grouping, left slide-out navigation, user settings, instant hybrid full-text search, SRM color spectrum visualizers, and interactive foam head scrollbars.

---

## 👤 User Story
**As a** BJCP Beer Judge or Homebrewer  
**I want to** browse, search, and filter beer style guidelines offline using clean base style grouping, slide-out navigation, and visual SRM indicators  
**So that** I can accurately evaluate beers during competitions or recipe development.

---

## 📋 Requirements & Acceptance Criteria

### Navigation & Settings
- [x] **Left Slide-Out Navigation Drawer (`AppDrawer`)**: Slide-out drawer with quick navigation to *Style Guide*, *Hops*, *Malt*, *Review History*, and *Settings*.
- [x] **Keep Screen Awake (Stay Unlocked)**: Toggle switch in the navigation drawer and in `SettingsScreen` allowing users to keep the phone screen awake / unlocked while studying beer styles, hops, and malts, managed via `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON`.
- [x] **Settings Screen (`SettingsScreen`)**: Dedicated settings page for selecting active guideline organization (BJCP 2021 default vs Brewers Association 2026), removing toggle clutter from the main list.
- [x] **Placeholder Screens**: Lightweight placeholder screens for Hops, Malt, and Review History.

### Style Grouping & Expansion
- [x] **Base Style Grouping**: Group style guidelines by base style categories (`1. Standard American Beer`, `2. International Lager`, `21. IPA`...).
- [x] **Press & Hold Inline Expansion**: Pressing and holding (or tapping the arrow) on a base style card expands it inline to reveal its sub-styles (1A, 1B, 1C, 1D...).
- [x] **Clean Category Cards**: Minimalist base style headers displaying style number prefix and name without clutter or unnecessary hints.
- [x] **Top-Level Category Detail Page**: Single-tapping a base style category opens `CategoryDetailScreen` showing category title, sub-style count, spec summary ranges (ABV, IBU, SRM), and sub-style list.
- [x] **Context-Aware Back Navigation State**: Returning via Back from any sub-page leaves only that sub-page's section expanded on the top-level list while collapsing other sections.
- [x] **Auto-Scroll on Category Expansion**: Expanding a parent category automatically scrolls it up if necessary so that at least 1.5 sub-cards (or 1 if only 1 exists) are immediately visible in the viewport without requiring manual scrolling.

### Search & Filtering
- [x] **Borderless Pill Search Bar**: Single-line, borderless pill search container with leading search icon and trailing clear/filter buttons.
- [x] **Instant Multi-Column Search**: Search across style numbers (`1A`, `7B`), style names, commercial examples (`Bud Light`, `Guinness`, `Pliny`), ingredients (`Citra`, `Cascade`, `Spalt`, `Pilsner`, `lager yeast`), aroma, flavor, and tags.
- [x] **Weighted Relevance Search Scoring**: Search results are prioritized and ranked by weighted relevance: Style Name & Number (weight 10), Overall Impression and Commercial Examples (weight 5), and all other fields (weight 1). Categories containing higher-scoring styles are surfaced first.
- [x] **Inline Search Auto-Expansion**: Typing a search query automatically expands matching style categories inline so search results are instantly visible.
- [x] **Expanded Overall Impression on Style Cards**: Show the full overall impression text in expanded style cards on the styles list/search page so judges and brewers can read the complete impression without clicking into the style detail page, truncating with ellipsis only if it exceeds 10 lines (regardless of whether an active search query is entered).
- [x] **Smart Range Slider Filtering**: Range sliders for SRM (0–40+), ABV (0–20%), and IBU (0–120). When a range slider is actively modified, entries with `N/A` for that numeric specification are excluded from results.

### SRM Visualizers & Interactive Scrollbar
- [x] **Vertical SRM Gradient Scrollbar**: On sub-style detail pages, a vertical right-hand scrollbar renders a gradient from minimum SRM (top) to maximum SRM (bottom), expanding smoothly on scroll or touch drag.
- [x] **Foam / Head Position Thumb Indicator**: Position indicator thumb styled as a frothy beer head whose color automatically adjusts based on the style's `appearance` description (white, creamy off-white, tan, deep tan).
- [x] **Full-Width Horizontal SRM Bottom Banner**: A ~2 cm (56dp) full-width banner at the bottom of the style detail page showing the horizontal SRM color gradient and a SRM range badge.
- [x] **Bookmarking / Favorites**: Pin favorite styles with a heart icon for quick access.

### Style Detail & Sequential Navigation
- [x] **Sequential Forward Navigation via Hidden Swipe**: On the style guidelines detail page (`StyleDetailScreen`), swiping forward (horizontal swipe to the left) advances the user to the next style detail in sequential order within the active guideline (e.g., from `1A` to `1B`, `1B` to `1C`) as an intuitive, clutter-free hidden gesture without requiring a visible button or prompt card.
- [x] **Standard Backstack Preservation**: Swiping back (or triggering system back) follows the normal Android navigation backstack, returning to the previously visited screen or style rather than forcing an inverted sequential traversal.

---

## 📱 User Interface & Visuals
- **Navigation Drawer**: Left slide-out modal sheet with icons for Style Guide, Hops, Malt, Review History, and Settings.
- **Search Bar**: Borderless pill search bar with single line input and filter action button.
- **Base Style Card**: Minimalist card showing `1. Standard American Beer` with expand/collapse arrow.
- **Expanded Style Card**: Style cards display the full overall impression text inline (up to 10 lines before ellipsis truncation) when expanded on the styles/search page, regardless of whether a search is performed.
- **Detail Screen**: Structured sensory cards, vertical SRM scrollbar with foam head thumb, full-width bottom SRM gradient banner, and hidden horizontal swipe-forward gesture to navigate sequentially to the next beer style without visual button clutter.
