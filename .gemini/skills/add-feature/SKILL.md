---
name: add-feature
description: Interview the user to specify a new feature, gather testable acceptance criteria, and scaffold the feature spec directory under features/00X-feature-name/spec.md for Spec-Driven Development (SDD). Use when the user asks to "add a new feature", "create a feature spec", "define a new feature", or plan a new phase.
---

# Add Feature (Spec-Driven Development Assistant)

Assist users (technical or non-technical) in defining a new feature for the application using Spec-Driven Development (SDD).

## Process

### Step 1: Scan Existing Features
List the `features/` directory in the repository root to determine the next numeric index:
- Example: `001-style-guidelines`, `002-hop-database` -> next feature is `003`.

### Step 2: Grill the User for Feature Specifications
Interview the user relentlessly using the **grilling** design-tree method until all requirements, user stories, UI components, and testable acceptance criteria are clear.

Work in **rounds**:
- Formulate each round with clear, numbered questions (`❓ Q1`, `❓ Q2`, etc.).
- Lead each question with a recommended answer (`➡️ Recommended Answer`).
- Word questions so "yes" accepts the recommended answer.

#### Key Areas to Cover:
1. **High-Level Goal & User Story**: Who is the user, what action are they performing, and what value do they derive?
2. **Acceptance Criteria & Testable Behavior**: What specific, verifiable conditions must be true for this feature to be complete?
3. **User Interface & Visuals**: What screens, components, inputs, color indicators, or layout structures are required?
4. **Data Assets & Storage**: What data formats, schemas, or external APIs/JSON assets are needed?

### Step 3: Scaffold the Feature Directory
Once the user confirms the specifications:
1. Create directory `features/00X-<feature-slug>/`.
2. Write `features/00X-<feature-slug>/spec.md` matching the standard SDD format:

```markdown
# Feature Spec: 00X [Feature Title]

---

## 🎯 High-Level Goal
[Summary of what this feature does]

---

## 👤 User Story
**As a** [User Type]  
**I want to** [Action]  
**So that** [Benefit]

---

## 📋 Requirements & Acceptance Criteria
- [ ] **Requirement 1**: [Testable condition]
- [ ] **Requirement 2**: [Testable condition]

---

## 📱 User Interface & Visuals
- **Screen / Section**: [UI description]

---

## 📦 Assets & Attachments
- [List any required JSON, image, or document assets]
```

### Step 4: Next Steps
Notify the user that `features/00X-<feature-slug>/spec.md` is ready and offer to begin implementation immediately.
