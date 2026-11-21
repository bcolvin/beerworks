# 🍺 Zymurgist Table

**Zymurgist Table** is a modern, offline-first Android application designed for homebrewers, meadmakers, cidermakers, and BJCP/Brewers Association judges. It provides sub-millisecond reference guidelines, sensory databases, digital scoresheet evaluation, and judge comparison tools.

---

## 💡 Have an Idea or Feedback?

Zymurgist Table is built for the brewing and judging community! If you have a feature idea, style/ingredient suggestion, or bug report, please **[open an issue on GitHub](https://github.com/bcolvin/beerworks/issues)** to share it (feel free to glance at [Features & Roadmap](#-features--roadmap) and [`BACKLOG.md`](./BACKLOG.md) below to see what's already implemented or planned).

---

## 🎯 Features & Roadmap

Zymurgist Table is built in modular phases:

### ✅ Implemented (v1.0 Beta)

1. **📖 Style Guidelines Reference Viewer (Features 001, 004, 005)**
   - Offline-first reference for **BJCP 2021** (116 beer styles), **Brewers Association 2026** (158 beer styles), **BJCP Mead** (13 styles), and **BJCP Cider** (11 styles).
   - Left slide-out navigation drawer (`AppDrawer`) with access to *Style Guide*, collapsible *Ingredients* (*Hops*, *Malts*, *Honey*, *Pomes*, *Yeast*), *Off-Flavors*, *Favorites*, *Reviews*, *Settings*, and a **Keep Screen Awake** toggle.
   - Base style category grouping (`1. Standard American Beer`, `21. IPA`) with press-and-hold inline sub-style expansion and context-aware back navigation state.
   - Dedicated `SettingsScreen` for selecting active guidelines, temperature units (`°F` / `°C`), and favorite removal confirmation.
   - Instant FTS5 multi-column search across style numbers (`1A`, `7B`), style names, commercial examples (`Bud Light`, `Guinness`, `Pliny`), and ingredients (`Citra`, `Cascade`, `Pilsner`).
   - Dynamic numeric parameter range filters for **SRM Color** (0–40+), **ABV** (0–20%), and **IBU** (0–120), with smart exclusion of `N/A` values when range sliders are active.
   - Interactive vertical SRM color gradient scrollbar on detail pages with dynamic frothy "foam head" position indicator thumb and full-width horizontal SRM gradient bottom banner.

2. **🌿 Ingredient & Sensory Databases (Features 002, 003, 006, 007, 008, 010)**
   - **Hops (77 varieties)**, **Malts (56 varieties)**, **Honey (36 varieties)**, **Pomes (37 apple/pear varieties)**, and **Yeast Strains (44 strains)** with 8-axis sensory radar charts, Pfund/Long Ashton/chemistry gauges, commercial lab cross-references, and bidirectional style links.
   - **Off-Flavors (31 sensory fault profiles)** with perception thresholds, chemical compounds, causes, and style contrasts.

3. **⭐ Unified Favorites Catalog (Feature 009)**
   - Bookmark styles, ingredients, and off-flavors for quick access during brewing or judging sessions, with cross-discipline search and filter chips.

4. **📝 Digital BJCP Scoresheets (Features 011, 012, 013)**
   - Offline-first digital **BJCP Beer**, **BJCP Mead**, and **BJCP Cider** scoresheet editor with descriptor chips, live scoring breakdowns, quick-peek style/scoring references, searchable/sortable **Reviews** history, and plain-text sharing.

5. **⚖️ Become a BJCP Judge Guide (Feature 014)**
   - Built-in certification pathway, Beer/Mead/Cider exam overview, tasting rubric, in-app study links, and direct links to BJCP competition and exam portals.

### 🔭 Upcoming Roadmap (see [`BACKLOG.md`](./BACKLOG.md))

- **Side-by-Side Comparison Matrix**: Compare styles, hops, or malts side-by-side.
- **AI / OCR Photo Digitizer**: Snap a photo of a handwritten scoresheet to auto-generate a digital scoresheet.
- **Optional Cloud Sync & Judge Consensus**: Sync judging logs and compare scores and flight notes with co-judges.

---

## 🔒 Privacy, Permissions & Licensing

- **100% Offline & Zero Permissions:** Zymurgist Table declares no Android permissions, contains no ads or analytics, and keeps all preferences, favorites, and scoresheets on-device. See [`PRIVACY.md`](./PRIVACY.md) for the full Privacy Policy (also linked inside Settings → *Licenses & Attributions*).
- **Licensing & Attributions:** Source code is licensed under **GPLv3** ([`LICENSE`](./LICENSE)). See [`ATTRIBUTIONS.md`](./ATTRIBUTIONS.md) for BJCP, Brewers Association 2026, and *Complete Beer Fault Guide* attributions.

---

## 🚀 How to Build & Load the App

### Prerequisites
- **Android Studio**: Android Studio Ladybug (2024.2.1+) or newer.
- **Java Development Kit (JDK)**: JDK 17 or higher.
- **Android SDK**: API Level 36 (compileSdk 36, targetSdk 36, minSdk 29).

### Command Line Build Instructions

Run these commands from the project root (`~/code/beerworks`):

1. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest -Djava.version=17.0.2
   ```

2. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug -Djava.version=17.0.2
   ```
   *The generated APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.*

3. **Install & Run on Connected Device / Emulator**:
   ```bash
   ./gradlew installDebug -Djava.version=17.0.2
   ```

---

## 📐 Spec-Driven Development (SDD) Workflow

This repository is configured for **Spec-Driven Development** so technical and non-technical team members can define and launch features in plain English:

```text
features/
├── _template/                       <- Reference template for new features
│   └── spec.md
├── 001-style-guidelines/            <- Implemented: BJCP 2021 & BA 2026
├── 002-hop-database/                <- Implemented: Hop varieties
├── 003-malt-database/               <- Implemented: Malt varieties
├── ...                              <- Implemented specs 004–013
└── 014-bjcp-judge-guide/            <- Implemented: Become a BJCP Judge hub
```

### 💡 Backlog & Quick Idea Capture
Future feature ideas and roadmap items are stored in [`BACKLOG.md`](./BACKLOG.md).

To quickly save a new idea without writing a full spec:
> *"Add [idea] to my backlog"* or *"Use the skill backlog-add"*

### 🛠️ How to Add a New Feature Spec

When you're ready to break an idea down into an executable feature spec:

#### Option 1: Interactive AI Interview (Recommended)
Simply ask the Gemini CLI:
> *"Add a new feature"* or *"Use the skill add-feature"*

The agent will interview you step-by-step to gather goals, user stories, UI preferences, and testable acceptance criteria, then automatically create `features/00X-<feature-slug>/spec.md`.

#### Option 2: Manual Spec Creation
1. Create a new directory under `features/` (e.g., `features/015-comparison-matrix/`).
2. Copy `features/_template/spec.md` into your new folder.
3. Fill out `spec.md` in plain English.
4. Invoke the AI agent:
   > *"Implement the spec in features/015-comparison-matrix/spec.md"*

---

## 🏛️ Architecture & Engineering Constitution

- **Architecture**: MVVM/MVI with Unidirectional Data Flow (UDF).
- **UI Toolkit**: 100% Jetpack Compose with Material 3 (XML layouts are strictly banned).
- **Asynchrony**: Kotlin Coroutines & `StateFlow` exclusively.
- **Local Persistence**: Room SQLite DB with FTS5 search tables & DataStore Preferences.
- **Testing Standard**: AssertJ `assertThat(x).isEqualTo(y)` assertions and Mockito `doReturn(x).when(y).z()` syntax. Every screen includes isolated `@Preview` functions.
