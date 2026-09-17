<p align="center">
  <img src="assets/brand/antigolem-logo.svg" width="132" alt="AntiGolem icon" />
</p>

<h1 align="center">AntiGolem</h1>
<p align="center"><b>Semantic Forensics · Strict Wording Audit · Local-First Toolkit</b><br/>Analyze harder. Rewrite better. Reply with evidence.</p>

<p align="center">
  <a href="https://chekento.github.io/antigolem/"><img alt="Open Live App" src="https://img.shields.io/badge/OPEN-LIVE%20APP-21d9c6?style=for-the-badge&logo=githubpages&logoColor=001018"></a>
  <a href="https://github.com/chekento/antigolem/releases/latest/download/AntiGolem.apk"><img alt="Download AntiGolem APK" src="https://img.shields.io/badge/ANDROID-DOWNLOAD%20APK-6f5cff?style=for-the-badge&logo=android&logoColor=white"></a>
</p>

<p align="center">
  <a href="docs/about.md">About</a> ·
  <a href="docs/methodology.md">Methodology</a> ·
  <a href="docs/local-ai.md">Local AI</a> ·
  <a href="docs/privacy.md">Privacy</a>
</p>

<p align="center"><img src="assets/marketing/hero.svg" width="100%" alt="AntiGolem app overview" /></p>

## AntiGolem 1.5.1 — Freehand Circle Select

AntiGolem performs a **strict sentence-by-sentence full-text audit**. Version 1.5.1 changes Android Circle Select from a stretched oval into a **real freehand lasso**.

Draw any contour with your finger — irregular, narrow, diagonal, jagged or rounded. AntiGolem stores the actual finger path as a polygon and automatically closes it when you lift your finger. It does **not** turn the start and end points into an ellipse.

For accessibility-visible text, node bounds are tested against the real polygon. For Android 11+ Circle OCR, AntiGolem captures a one-shot screenshot after the explicit gesture, crops to the polygon bounds and **masks everything outside the hand-drawn shape before ML Kit OCR**.

### Four interactive workspaces

| Workspace | What it does |
|---|---|
| **Forensic Heatmap** | Color-codes every detected sentence and opens a clickable *Why this rating?* stack showing negative normalization, D-factor, absolutism, modal pressure, responsibility diffusion and constructive offsets. |
| **Before / After** | Builds a rewrite proposal, compares grounded issue-rate and clarity before/after, and supports per-sentence **Accept / Keep original**, **Accept all**, local-AI refinement and version history. |
| **Reply Studio** | Generates replies in **Friendly, Very polite, Professional, Short, Detailed, Question-led, Unfriendly, Dissing and Challenging** styles. Sharp modes criticize the wording/argument rather than degrading the author. Every reply can include real counts and percentages from the current audit. |
| **Document Lab** | Multi-file import and comparison for TXT, Markdown, HTML, CSV, JSON, XML, RTF, PDF, DOCX and images. PDF.js, Mammoth and Tesseract.js are loaded only when those formats are needed. |

**Grounded reply evidence** can include values such as `12/30 sentences (40%) flagged`, BAD/AMBIVALENT shares, critical/high percentage, D-1/D-2+ counts, negative-normalization signals, absolutisms, modal-pressure cues and responsibility-diffusion signals. Percentages are calculated from the analyzed text rather than invented by a reply template.

In the Android APK, Reply Studio can hand its final text directly to Android's **Share** sheet. In browsers AntiGolem uses Web Share when available and falls back to copying.

---

## Android floating toolkit

<a href="https://github.com/chekento/antigolem/releases/latest/download/AntiGolem.apk"><img src="https://img.shields.io/badge/%E2%AC%87%20GET%20ANTIGOLEM%20FOR%20ANDROID-LATEST%20APK-00d8b3?style=for-the-badge&labelColor=07111f" alt="Download latest AntiGolem APK" /></a>

After the user explicitly enables **AntiGolem Screen Text Analyzer** in Android Accessibility settings, a small draggable AntiGolem bubble remains available over the launcher and apps.

**Toolkit actions include:**

- **Circle Select · freehand text** — paint a freehand loop with your finger. AntiGolem automatically closes the path and analyzes accessibility-visible text intersecting the actual polygon.
- **Circle OCR · freehand** — Android 11+ only; paints the same arbitrary lasso, takes an explicit one-shot AccessibilityService screenshot and masks all pixels outside the lasso before **bundled on-device ML Kit Latin OCR**. Protected/secure windows remain unavailable.
- **Analyze visible text** — full active accessibility-visible screen text.
- **Open document** — jumps straight into Document Lab and Android's multi-file chooser.
- **Write / paste**, **Open AntiGolem**, **Local AI**, **Settings**.

A tiny tap or too-small gesture cancels the lasso. The drawn path follows motion-history points for smoother and more faithful finger tracking. On finger-up the open path is automatically closed; no perfect hand-drawn closure is required.

After capture/OCR, a compact **Quick Result** card appears over the current app with **Details · Improve · Reply · Copy**. The quick percentage is explicitly preliminary; **Details** opens the complete strict audit and grounded metrics.

---

## Rewrite workflow

AntiGolem can apply improvements directly to the source text. The workbench adds a safer review layer on top:

1. Generate deterministic conservative proposal.
2. Compare **before vs after** issue rate and clarity.
3. Accept/reject changes sentence by sentence or accept all.
4. Optionally refine with the local on-device language-model path.
5. Restore earlier versions from the local session history.

Automatic rewrites are constrained to preserve factual claims, names, numbers, links, chronology, viewpoint and intended meaning as far as possible, but should still be reviewed before publication.

---

## API-free AI & OCR architecture

The deterministic audit requires **no API key, account or inference server**.

- Browser-provided local `LanguageModel` / Prompt API when available.
- Optional compact Qwen2.5-0.5B through WebLLM/WebGPU after runtime/model download.
- Android Circle OCR uses the **bundled** `com.google.mlkit:text-recognition` Latin model, so OCR inference runs on-device and is available after installation.
- Browser Document Lab image OCR uses Tesseract.js after its runtime/language assets are downloaded.

> **Method note:** Golem/Pygmalion, mantra, ghost-context and “psychological programming” are editorial/rhetorical heuristic labels. AntiGolem does not claim that wording literally programs a brain or proves subconscious causation.

---

## Six languages

**Deutsch · English · Français · Español · Italiano · Nederlands**

The web app detects browser language on first use. Unsupported languages fall back to **English**; a manual choice is stored locally.

---

<p align="center"><img src="assets/marketing/make-your-texts-less-harming.svg" width="100%" alt="Make your texts less harming" /></p>
<p align="center"><img src="assets/marketing/avoid-your-own-threat.svg" width="100%" alt="Avoid your own threat" /></p>
<p align="center"><img src="assets/marketing/its-better-to-write-better.svg" width="100%" alt="It's better to write better" /></p>

---

## Privacy

The rule engine, rewrites, local-model inference and Android OCR are designed local-first. Freehand Circle OCR occurs only after the user explicitly selects that tool and draws a lasso. The screenshot is processed locally; everything outside the drawn polygon is masked before OCR, and screenshot bitmaps are released after recognition. AntiGolem does not continuously record the screen.

[About](docs/about.md) · [Methodology](docs/methodology.md) · [Local AI](docs/local-ai.md) · [Privacy](docs/privacy.md) · [Latest APK](https://github.com/chekento/antigolem/releases/latest/download/AntiGolem.apk)

<details>
<summary><strong>Repository / developer / build details</strong></summary>

### Core files

- `app.js` — deterministic full-text analyzer
- `editor-tools.js` — strict-mode rating, direct rewrite/undo and base critique workflow
- `workbench.js` — heatmap, explainable scoring, diff/version workflow, Reply Studio and Document Lab
- `share-tools.js` — Android/browser sharing
- `export-tools.js` — TXT/Markdown/PDF export
- `advanced-stats.js` — extended metrics
- `local-ai.js` — local LLM cross-check/generation layer
- `report-i18n.js` / `locale-bootstrap.js` — language/report localization
- `android/` — WebView wrapper, floating toolkit, true freehand lasso, Circle OCR and quick-result overlay
- `docs/` — GitHub-native documentation pages that work without GitHub Pages

### Android package

`cloud.kosch.antigolem`

### Build locally

```bash
gradle -p android :app:assembleDebug
```

### GitHub Pages

The static web application lives in the repository root. GitHub-native documentation links above do **not** depend on Pages. To publish the live web app, configure Pages once with `Settings → Pages → Deploy from a branch → main → /(root)`.

### Current distribution

The public APK is a debug-signed test build. Production / Play Store distribution should use a persistent private signing key and AAB release pipeline.

</details>

<p align="center"><b>AntiGolem</b> · local · strict · explainable · open</p>