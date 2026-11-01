# Feature Spec: 002 Hop Database & Style Cross-Reference

---

## 🎯 High-Level Goal
Allow beer judges and brewers to look up hop varieties, see their alpha acid ranges, origin, and sensory profile (citrus, floral, spicy, earthy, herbal), and cross-reference which beer styles typically utilize or feature each hop.

---

## 👤 User Story
**As a** Beer Judge evaluating an IPA or Pale Ale  
**I want to** look up a hop variety (e.g. Citra, Saaz, Mosaic)  
**So that** I know what specific hop aromas and flavors to expect in the beer style being judged.

---

## 📋 Requirements & Acceptance Criteria
- [ ] **Hop Search & Lookup**: Search hops by name, origin, or flavor descriptor (e.g., "citrus", "dank", "noble").
- [ ] **Hop Detail View**: View Alpha Acid %, Beta Acid %, origin country, aroma descriptors, and recommended beer styles.
- [ ] **Cross-Link to Style Guidelines**: Clicking a recommended style in a hop profile opens that style's detail card from Feature 001.

---

## 📱 User Interface & Visuals
- **Hop List Tab**: Search bar + list of hop variety cards.
- **Hop Detail Screen**: Sensory descriptor chips, alpha acid range gauge, and clickable list of associated beer styles.

---

## 📦 Assets & Attachments
- `hops_data.json` (to be added)
