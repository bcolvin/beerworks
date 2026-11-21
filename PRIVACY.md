# Privacy Policy for Zymurgist Table

**Effective Date:** November 19, 2026  
**Application ID:** `com.bc.beerworks`  
**Repository:** [https://github.com/bcolvin/beerworks](https://github.com/bcolvin/beerworks)

## Summary

**Zymurgist Table** is a 100% offline-first Android application. It does **not** collect, transmit, store remotely, sell, or share any personal or usage data.

## Permissions & Network Access

- **Zero Android Permissions:** Zymurgist Table declares no `<uses-permission>` entries in its Android manifest. It has no `INTERNET`, location, camera, microphone, contacts, or external storage permissions.
- **No Analytics, Telemetry, or Ads:** The app contains no advertising SDKs, analytics trackers, or crash-reporting services.

## Local On-Device Storage

All user-created content and preferences remain strictly on your device:

- **App Preferences (`DataStore`):** Selected style guideline, temperature unit (`°F` / `°C`), drawer state, last selected scoresheet discipline, and confirmation toggles.
- **Favorites & Digital Scoresheets (`Room` SQLite):** Bookmarked styles, ingredients, off-flavors, and your personal Beer, Mead, and Cider evaluation scoresheets.
- **Backups:** Depending on your Android system settings, Android Auto Backup may back up app data to your personal Google Drive account under Android's standard OS backup rules.

## User-Initiated External Actions

Zymurgist Table only interacts with external apps when you explicitly choose to do so:

- **External Web Links:** Tapping links to BJCP portals (`app.bjcp.org`, `www.bjcp.org`) or the GitHub repository (`github.com/bcolvin/beerworks`) opens your device's default web browser via an Android `ACTION_VIEW` intent. Once opened, those external websites are governed by their own privacy policies.
- **Sharing Scoresheets:** Tapping **Share** on a saved scoresheet opens the standard Android share sheet (`ACTION_SEND`) with plain text generated from that scoresheet so you can send it to an app of your choice.

## Children's Privacy

Zymurgist Table is an educational brewing and beverage judging reference tool intended for adults of legal drinking age in their jurisdiction and does not knowingly collect any information from anyone, including children.

## Changes to This Policy

If future versions introduce optional online features or new permissions, this Privacy Policy will be updated in the repository prior to release.

## Contact

If you have questions about this Privacy Policy, please open an issue at [https://github.com/bcolvin/beerworks/issues](https://github.com/bcolvin/beerworks/issues).
