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
- [x] **Settings Screen (`SettingsScreen`)**: Dedicated settings page for selecting active guideline organization (BJCP 2021 default vs Brewers Association 2026), removing toggle clutter from the main list.
- [x] **Placeholder Screens**: Lightweight placeholder screens for Hops, Malt, and Review History.

### Style Grouping & Expansion
- [x] **Base Style Grouping**: Group style guidelines by base style categories (`1. Standard American Beer`, `2. International Lager`, `21. IPA`...).
- [x] **Press & Hold Inline Expansion**: Pressing and holding (or tapping the arrow) on a base style card expands it inline to reveal its sub-styles (1A, 1B, 1C, 1D...).
- [x] **Clean Category Cards**: Minimalist base style headers displaying style number prefix and name without clutter or unnecessary hints.
- [x] **Top-Level Category Detail Page**: Single-tapping a base style category opens `CategoryDetailScreen` showing category title, sub-style count, spec summary ranges (ABV, IBU, SRM), and sub-style list.
- [x] **Context-Aware Back Navigation State**: Returning via Back from any sub-page leaves only that sub-page's section expanded on the top-level list while collapsing other sections.

### Search & Filtering
- [x] **Borderless Pill Search Bar**: Single-line, borderless pill search container with leading search icon and trailing clear/filter buttons.
- [x] **Instant Multi-Column Search**: Search across style numbers (`1A`, `7B`), style names, commercial examples (`Bud Light`, `Guinness`, `Pliny`), ingredients (`Citra`, `Cascade`, `Spalt`, `Pilsner`, `lager yeast`), aroma, flavor, and tags.
- [x] **Inline Search Auto-Expansion**: Typing a search query automatically expands matching style categories inline so search results are instantly visible.
- [x] **Smart Range Slider Filtering**: Range sliders for SRM (0–40+), ABV (0–20%), and IBU (0–120). When a range slider is actively modified, entries with `N/A` for that numeric specification are excluded from results.

### SRM Visualizers & Interactive Scrollbar
- [x] **Vertical SRM Gradient Scrollbar**: On sub-style detail pages, a vertical right-hand scrollbar renders a gradient from minimum SRM (top) to maximum SRM (bottom), expanding smoothly on scroll or touch drag.
- [x] **Foam / Head Position Thumb Indicator**: Position indicator thumb styled as a frothy beer head whose color automatically adjusts based on the style's `appearance` description (white, creamy off-white, tan, deep tan).
- [x] **Full-Width Horizontal SRM Bottom Banner**: A ~2 cm (56dp) full-width banner at the bottom of the style detail page showing the horizontal SRM color gradient and a SRM range badge.
- [x] **Bookmarking / Favorites**: Pin favorite styles with a heart icon for quick access.

---

## 📱 User Interface & Visuals
- **Navigation Drawer**: Left slide-out modal sheet with icons for Style Guide, Hops, Malt, Review History, and Settings.
- **Search Bar**: Borderless pill search bar with single line input and filter action button.
- **Base Style Card**: Minimalist card showing `1. Standard American Beer` with expand/collapse arrow.
- **Detail Screen**: Structured sensory cards, vertical SRM scrollbar with foam head thumb, and full-width bottom SRM gradient banner.
