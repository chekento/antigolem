# AntiGolem — Semantic Forensics & Language Audit

> **Local-first forensic language analysis for Web + Android.** AntiGolem parses the complete supplied text sentence-by-sentence and audits structural ambiguity, anaphoric distance (D-0 / D-1 / D-2+), destructive/constructive framing, responsibility diffusion and target-group resonance.

[![Live Web App](https://img.shields.io/badge/LIVE-GitHub%20Pages-00d4aa?style=for-the-badge)](https://chekento.github.io/antigolem/)
[![Download Android APK](https://img.shields.io/badge/ANDROID-Download%20APK-6f5cff?style=for-the-badge&logo=android)](https://github.com/chekento/antigolem/releases/latest/download/AntiGolem.apk)
[![Android CI](https://github.com/chekento/antigolem/actions/workflows/android.yml/badge.svg)](https://github.com/chekento/antigolem/actions/workflows/android.yml)

## What it does

- Parses the **entire supplied text** instead of sampling excerpts.
- Produces complete sentence counts and percentages for **BAD / AMBIVALENT / GOOD / NEUTRAL**.
- Estimates **D-0 / D-1 / D-2+** anaphoric binding risk.
- Detects heuristic **Golem-like** destructive framing and **Pygmalion-like** constructive framing.
- Flags generic **“man” / “we”** responsibility diffusion or over-inclusive wording.
- Generates target-group resonance, mechanism notes and constructive rewrites.
- Works in **German, English, French, Spanish, Italian and Dutch**.
- Exports the complete audit as Markdown.
- Runs locally in the browser with no account and no mandatory API.

## Android overlay mode

The Android app contains an `AccessibilityService` used only after explicit user activation. A visible floating AntiGolem button can collect text exposed by the **currently active Android accessibility tree**, open AntiGolem and analyze it locally.

**Important limitation:** Android apps do not always expose underlying text through accessibility. Text drawn into images, video, canvas surfaces, some PDF viewers and protected apps may therefore not be readable without a separate OCR / screen-capture path.

## Methodology note

The labels **Golem effect**, **Pygmalion effect**, **mantra** and **psychological programming** are implemented as a *heuristic rhetorical/framing model*. The software does **not** claim that a sentence literally programs a neural network, diagnoses a person, or proves subconscious causation. Results are prompts for close reading and editorial review, not clinical findings.

## Privacy

Text analysis is local-first. The web app does not transmit pasted text to an AntiGolem backend. The Android overlay processes accessibility text on-device. See [`privacy.html`](./privacy.html) for details.

<details>
<summary><strong>Repository / developer details</strong></summary>

### Structure

- `index.html`, `styles.css`, `app.js` — GitHub Pages web app
- `android/` — native Android wrapper + accessibility overlay
- `.github/workflows/pages.yml` — Pages deployment
- `.github/workflows/android.yml` — APK build and `latest` release asset

### Android build

```bash
gradle -p android :app:assembleDebug
```

The public CI build is a **test/debug-signed APK**. For production distribution, configure a private stable signing key in GitHub Secrets before publishing to an app store.

### Package

`cloud.kosch.antigolem`

</details>

---

Built as **AntiGolem** — complete-text semantic forensics with transparent heuristics.