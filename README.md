# 🍺 Zymurgist Table

**Zymurgist Table** is a modern, offline-first Android application designed for homebrewers, meadmakers, cidermakers, and BJCP/Brewers Association judges. It provides sub-millisecond reference guidelines, sensory databases, digital scoresheet evaluation, and judge comparison tools.

---

## 💡 Have an Idea or Feedback?

Zymurgist Table is built for the brewing and judging community! If you have a feature idea, style/ingredient suggestion, or bug report, please **[open an issue on GitHub](https://github.com/bcolvin/beerworks/issues)** to share it (feel free to glance at the [Project Goals & Vision](#-project-goals--vision) and [`BACKLOG.md`](./BACKLOG.md) below to see what's already on the roadmap).

---

## 🎯 Project Goals & Vision

Zymurgist Table is structured into modular phases:

1. **📖 Style Guidelines Reference Viewer (Implemented - Feature 001)**
   - Offline-first reference for **BJCP 2021** (34 categories) and **Brewers Association 2026** (158 styles).
   - Left slide-out navigation drawer (`AppDrawer`) with access to *Style Guide*, *Hops*, *Malt*, *Review History*, and *Settings*.
   - Base style category grouping (`1. Standard American Beer`, `21. IPA`) with press-and-hold inline sub-style expansion and context-aware back navigation state.
   - Dedicated `SettingsScreen` for selecting active guidelines, keeping the list interface clean and uncluttered.
   - Instant multi-column search across style numbers (`1A`, `7B`), style names, commercial examples (`Bud Light`, `Guinness`, `Pliny`), and hop/malt/yeast ingredients (`Citra`, `Cascade`, `Pilsner`).
   - Borderless pill search bar with inline search auto-expansion.
   - Dynamic numeric parameter range filters for **SRM Color** (0–40+), **ABV** (0–20%), and **IBU** (0–120), with smart exclusion of `N/A` values when range sliders are active.
   - Interactive vertical SRM color gradient scrollbar on detail pages with dynamic frothy "foam head" position indicator thumb (color-matched to style appearance descriptions).
   - Full-width ~2 cm (56dp) horizontal SRM gradient bottom banner with SRM color range badge.
   - Local bookmarking/favorites for quick access during judging sessions.

2. **🌿 Hop Database & Style Cross-Reference (Upcoming - Feature 002)**
   - Look up hop varieties by name, origin, or flavor profile (citrus, floral, dank, noble).
   - Cross-reference hops directly with recommended beer styles.

3. **🌾 Malt Database & Flavor Profiles (Upcoming)**
   - Explore specialty malts, grain profiles, and expected sensory notes.

4. **📝 Digital Scoresheets & AI Photo Digitizer (Upcoming)**
   - Fill out digital shortform and longform BJCP / BA scoresheets directly in the app.
   - **AI/OCR Photo Digitizer**: Snap a photo of a handwritten scoresheet to auto-generate a structured digital scoresheet.
   - Searchable judging log (by date, beer name, or style) backed by Firebase sync.

5. **🌐 Social Judge Comparison & Consensus (Upcoming)**
   - Compare judging scores and flight notes with co-judges.

---

## 🚀 How to Build & Load the App

### Prerequisites
- **Android Studio**: Android Studio Ladybug (2024.2.1+) or newer.
- **Java Development Kit (JDK)**: JDK 17 or higher.
- **Android SDK**: API Level 35 (compileSdk 35, minSdk 29).

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
├── _template/                  <- Reference template for new features
│   └── spec.md
├── 001-style-guidelines/       <- Implemented Phase 1
│   └── spec.md
└── 002-hop-database/           <- Planned Phase 2
    └── spec.md
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
1. Create a new directory under `features/` (e.g., `features/003-digital-scoresheets/`).
2. Copy `features/_template/spec.md` into your new folder.
3. Fill out `spec.md` in plain English.
4. Invoke the AI agent:
   > *"Implement the spec in features/003-digital-scoresheets/spec.md"*

---

## 🏛️ Architecture & Engineering Constitution

- **Architecture**: MVVM/MVI with Unidirectional Data Flow (UDF).
- **UI Toolkit**: 100% Jetpack Compose with Material 3 (XML layouts are strictly banned).
- **Asynchrony**: Kotlin Coroutines & `StateFlow` exclusively.
- **Local Persistence**: Room SQLite DB with FTS5 search tables & DataStore Preferences.
- **Testing Standard**: AssertJ `assertThat(x).isEqualTo(y)` assertions and Mockito `doReturn(x).when(y).z()` syntax. Every screen includes isolated `@Preview` functions.
