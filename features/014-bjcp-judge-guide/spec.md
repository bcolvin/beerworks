# Feature Spec: 014 Become a BJCP Judge Guide & Community Hub

---

## 🎯 High-Level Goal
Encourage brewers, meadmakers, cidermakers, and sensory enthusiasts using Beerworks to explore the **BJCP (Beer Judge Certification Program)**, volunteer as a **Competition Steward** or judge at local sanctioned competitions, and prepare for Beer, Mead, or Cider judging exams—all from an unobtrusive, 100%-offline educational subpage linked from **Settings** (`"Interested in becoming a judge?"`).

---

## 👤 User Story
**As a** Brewer, Meadmaker, Cidermaker, or Prospective Judge  
**I want to** open an informational guide from Settings that explains the BJCP certification process, stewarding, exam tracks (Beer, Mead, Cider), and scoresheet grading rubric, links directly to Beerworks' in-app study tools, and provides one-tap links to the official BJCP competition and exam calendars  
**So that** I can get involved in local homebrew competitions and prepare for BJCP judging exams without requiring the app to request internet or location permissions.

---

## 📚 Domain & Architecture References
- **Domain Vocabulary**: Strictly adhere to `GLOSSARY.md` (`BJCP (Beer Judge Certification Program)`, `Competition Steward`, `BJCP Entrance Exam`, `BJCP Tasting Exam`, `BJCP Judge Rank`, `Beverage Discipline`, `Scoresheet`, `Off-Flavor`).
- **Zero-Permission Offline Architecture**:
  - `AndroidManifest.xml` remains completely free of `<uses-permission>` declarations (no `INTERNET` or `ACCESS_COARSE_LOCATION`).
  - All educational content, exam track comparisons, and rubric strategies are bundled natively in-app.
  - Live calendars and official portals launch externally in the user's browser via `Intent(Intent.ACTION_VIEW)` with an explicit external-link indicator (`Icons.AutoMirrored.Filled.OpenInNew`).
- **Performance & UI Standards**: Follow `GEMINI.md`:
  - Clickable cards on `SettingsScreen` must NOT include trailing chevron/navigation indicator arrows (`KeyboardArrowRight`).
  - Compose `@Immutable` data models for structured guide sections and study links.

---

## 📋 Requirements & Acceptance Criteria

### 1. Settings Screen Entry Point (`SettingsScreen.kt`)
- [x] **"Interested in becoming a judge?" Card**:
  - Rendered at the bottom of `SettingsScreen` below the **Confirm Favorite Removal** card.
  - Styled consistently with existing Settings cards (`RoundedCornerShape(16.dp)`, `surfaceVariant.copy(alpha = 0.5f)` container, `Icons.Default.Gavel` or `EmojiEvents` primary-tinted icon, bold title **"Interested in becoming a judge?"**, and descriptive body copy).
  - No trailing navigation arrow/chevron on the card, adhering to `GEMINI.md`.
  - Clicking the card invokes `onBecomeJudgeClick` and navigates to the `"bjcp_judge_guide"` route.

### 2. "Become a BJCP Judge" Screen (`BjcpJudgeGuideScreen.kt`)
- [x] **TopAppBar & Navigation**:
  - Title: **"Become a BJCP Judge"** with back navigation button (`Icons.AutoMirrored.Filled.ArrowBack`) popping back to `SettingsScreen`.
- [x] **Section 1 (Always Visible) — Welcome & Competition Stewarding**:
  - Introduces the **BJCP (Beer Judge Certification Program)** (founded in 1985) and its mission.
  - Highlights **Volunteering as a Competition Steward**: explains that anyone can help out at a local competition immediately with zero prior exams required, sitting alongside certified judges, pouring flights, sampling entries, and earning Experience Points.
- [x] **Section 2 (Collapsible Accordion Sections — Pathway Expanded by Default)**:
  - **1. The Certification Pathway (Step-by-Step)** *(Expanded by default)*:
    - **Step 1: BJCP Entrance Exam**: Timed, open-book online prerequisite exam on `app.bjcp.org` testing brewing processes, style guidelines, off-flavors, and judging procedures.
    - **Step 2: BJCP Tasting Exam**: In-person 90-minute blind sensory evaluation of 6 beverages (15 minutes per scoresheet) graded against proctor consensus.
    - **Step 3: BJCP Judge Ranks & Endorsements**: How exam scores combine with competition **Experience Points** (earned from judging, stewarding, and organizing) to progress from *Rank Pending / Apprentice* → *Recognized* → *Certified* → *National* → *Master* → *Grand Master*, plus *Mead Judge* and *Cider Judge* endorsements.
  - **2. Exam Tracks: Beer, Mead & Cider** *(Collapsed by default)*:
    - Breaks down the three distinct **Beverage Discipline** exam pathways:
      - **🍺 Beer Judge Track**: Online Entrance Exam (180 questions in 60 minutes) → Beer Tasting Exam (6 beers in 90 minutes) → Written Proficiency Exam (required for National/Master ranks).
      - **🐝 Mead Judge Track**: Online Mead Entrance Exam (100 questions in 30 minutes) → Mead Tasting Exam (6 meads in 90 minutes). Earns standalone Mead Judge rank or Mead Endorsement for existing Beer judges.
      - **🍏 Cider Judge Track**: Online Cider Entrance Exam (100 questions in 30 minutes) → Cider Tasting Exam (6 ciders/perries in 90 minutes). Earns standalone Cider Judge rank or Cider Endorsement for existing Beer judges.
  - **3. Scoresheet Rubric & Exam Strategy** *(Collapsed by default)*:
    - Details the 5 official BJCP Tasting Exam grading criteria (each worth 20% of the exam score):
      1. **Perception**: Detecting the actual aromas, flavors, appearance, mouthfeel, and faults present in the glass.
      2. **Descriptive Ability**: Using specific sensory descriptors (e.g., *"toasted bread crust, floral noble hops, diacetyl butterscotch"*) rather than vague terms (*"malty, hoppy, good"*).
      3. **Feedback**: Offering constructive, actionable brewing/fermentation advice to troubleshoot detected faults or improve stylistic balance.
      4. **Completeness**: Filling every section completely, quantifying intensity levels (low/medium/high), and avoiding blank space.
      5. **Scoring Accuracy**: Assigning a numerical score out of 50 that aligns with the proctor consensus score and matches the written tone.
- [x] **Section 3 (Always Visible) — In-App Study Toolkit**:
  - Direct quick-action buttons linking to Beerworks' built-in offline study tools:
    - **🍺 Study BJCP Beer Styles (2021)** → Sets active guideline to `GuidelineType.BJCP_2021` via `SettingsViewModel` / `PreferencesManager` and navigates to `"style_list"`.
    - **🐝 Study BJCP Mead Styles** → Sets active guideline to `GuidelineType.BJCP_MEAD` and navigates to `"style_list"`.
    - **🍏 Study BJCP Cider Styles** → Sets active guideline to `GuidelineType.BJCP_CIDER` and navigates to `"style_list"`.
    - **🧪 Study Off-Flavors & Exam Faults** → Navigates to `"off_flavors"`.
    - **📝 Practice a Blind Scoresheet** → Navigates to `"scoresheet_editor"`.
- [x] **Section 4 (Always Visible) — Official BJCP Calendars, Portal & Resources**:
  - Clearly labeled external browser links (`Icons.AutoMirrored.Filled.OpenInNew`, *"Opens in browser"*) launching via `Intent(Intent.ACTION_VIEW)`:
    1. **Upcoming Competitions Calendar**: `https://app.bjcp.org/competition-calendar?start_date=<YYYY-MM-DD>` (dynamically populated with current local date in `YYYY-MM-DD` format).
    2. **Upcoming Tasting Exams**: `https://app.bjcp.org/upcoming-tasting-exams`
    3. **BJCP Online Entrance Exam & Judge Portal**: `https://app.bjcp.org/`
    4. **BJCP Official Website (Study Guides, Manuals & International Resources)**: `https://www.bjcp.org/`
- [x] **Section 5 (Always Visible) — Community & BJCP Attribution Card**:
  - Titled **"Built on the Foundation of the BJCP Community"**, acknowledging that Beerworks' style references, sensory evaluation frameworks, off-flavor guides, and scoresheet structures draw deeply from the decades of volunteer work by the BJCP community, and expressing the hope that this app puts those resources to their fullest use to support judges and brewers and inspire new volunteers.

---

## 🧪 Testing Plan
- Unit tests in `BjcpJudgeGuideTest` / `SettingsViewModelTest`:
  - Verify `buildCompetitionCalendarUrl(LocalDate)` generates `https://app.bjcp.org/competition-calendar?start_date=YYYY-MM-DD` accurately.
  - Verify external BJCP resource URLs (`upcoming-tasting-exams`, `app.bjcp.org`, `www.bjcp.org`).
  - Verify in-app study toolkit guideline selection updates `PreferencesManager.selectedGuideline` (`BJCP_2021`, `BJCP_MEAD`, `BJCP_CIDER`).
- Verify full suite and build with `./gradlew --no-daemon --offline testDebugUnitTest assembleDebug`.
