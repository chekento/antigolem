<p align="center">
  <img src="assets/brand/antigolem-logo.svg" width="132" alt="AntiGolem icon" />
</p>

<h1 align="center">AntiGolem</h1>
<p align="center"><b>Semantic Forensics · Strict Wording Audit · Local-First Toolkit</b><br/>Analyze harder. Rewrite better. Reply with evidence.</p>

<h2 align="center">🌐 LIVE VERSION</h2>
<p align="center">
  <a href="https://raw.githack.com/chekento/antigolem/main/index.html"><img alt="Open AntiGolem Live Version" src="https://img.shields.io/badge/OPEN%20THE%20LIVE%20APP-IN%20YOUR%20BROWSER-21d9c6?style=for-the-badge&logo=googlechrome&logoColor=001018"></a>
</p>
<p align="center"><b>No GitHub Pages 404:</b> the main live button opens the rendered app directly from the repository.</p>

<p align="center">
  <a href="https://github.com/chekento/antigolem/releases/latest/download/AntiGolem.apk"><img alt="Download AntiGolem APK" src="https://img.shields.io/badge/ANDROID-DOWNLOAD%20LATEST%20APK-6f5cff?style=for-the-badge&logo=android&logoColor=white"></a>
</p>

<p align="center">
  <a href="docs/about.md">About</a> ·
  <a href="docs/methodology.md">Methodology</a> ·
  <a href="docs/local-ai.md">Local AI</a> ·
  <a href="docs/privacy.md">Privacy</a>
</p>

<p align="center">
  <a href="https://github.com/chekento/antigolem/releases/latest/download/AntiGolem.apk"><img src="assets/marketing/hero.svg" width="100%" alt="AntiGolem app overview and APK download" /></a>
</p>
<p align="center"><b>↑ Tap/click the hero image to download the latest AntiGolem APK.</b></p>

## AntiGolem 1.5.1 — Freehand Circle Select

AntiGolem performs a **strict sentence-by-sentence full-text audit**. Android Circle Select is a **real freehand lasso**: draw any contour with your finger and AntiGolem keeps the actual path instead of stretching an oval between start and end points.

For accessibility-visible text, node bounds are tested against the real polygon. On Android 11+, **Circle OCR** captures a one-shot screenshot after the explicit gesture, crops to the lasso bounds and masks everything outside the hand-drawn shape before local ML Kit OCR.

### Main workspaces

| Workspace | What it does |
|---|---|
| **Forensic Heatmap** | Color-codes every detected sentence and explains strict scoring signals. |
| **Before / After** | Rewrite proposal, issue-rate comparison, per-sentence accept/reject and version history. |
| **Reply Studio** | Friendly through challenging/dissing wording critique with grounded counts and percentages. |
| **Document Lab** | TXT, MD, HTML, CSV, JSON, XML, RTF, PDF, DOCX and image import/analysis. |

### Android floating toolkit

- **Freehand Circle Select** for accessibility-visible text.
- **Freehand Circle OCR** with bundled on-device Latin OCR.
- **Analyze visible text** from the active app.
- **Open document** via Android file picker.
- **Write / paste**, **Local AI**, **Settings**.
- **Quick Result overlay** with Details · Improve · Reply · Copy.

### Rewrite & reply workflow

AntiGolem can apply improvements directly to the source text, compare before/after metrics, keep or reject individual sentence changes and restore earlier versions. Reply Studio can generate Friendly, Very polite, Professional, Short, Detailed, Question-led, Unfriendly, Dissing and Challenging variants. Sharp modes target the wording/argument rather than degrading the author.

### API-free AI & OCR

The deterministic audit requires **no API key, account or inference server**. Optional local model paths include browser-provided on-device LanguageModel support and WebLLM/WebGPU. Android Circle OCR uses a bundled on-device ML Kit Latin model.

> **Method note:** Golem/Pygmalion, mantra, ghost-context and “psychological programming” are editorial/rhetorical heuristic labels, not clinical diagnoses or proof of neural causation.

### Six languages

**Deutsch · English · Français · Español · Italiano · Nederlands**

Browser language is detected on first use. Unsupported languages fall back to **English**; a manual choice is stored locally.

---

<p align="center">
  <a href="https://raw.githack.com/chekento/antigolem/main/index.html"><img src="assets/marketing/make-your-texts-less-harming.svg" width="100%" alt="Make your texts less harming — open live AntiGolem" /></a>
</p>
<p align="center">
  <a href="https://raw.githack.com/chekento/antigolem/main/index.html"><img src="assets/marketing/avoid-your-own-threat.svg" width="100%" alt="Avoid your own threat — open live AntiGolem" /></a>
</p>
<p align="center">
  <a href="https://raw.githack.com/chekento/antigolem/main/index.html"><img src="assets/marketing/its-better-to-write-better.svg" width="100%" alt="It's better to write better — open live AntiGolem" /></a>
</p>

---

## Privacy

The rule engine, rewrites, local-model inference and Android OCR are designed local-first. Freehand Circle OCR runs only after the user explicitly invokes it and draws a lasso. AntiGolem does not continuously record the screen.

[Open Live Version](https://raw.githack.com/chekento/antigolem/main/index.html) · [About](docs/about.md) · [Methodology](docs/methodology.md) · [Local AI](docs/local-ai.md) · [Privacy](docs/privacy.md) · [Latest APK](https://github.com/chekento/antigolem/releases/latest/download/AntiGolem.apk)

<details>
<summary><strong>Repository / developer / build details</strong></summary>

### Core files

- `app.js` — deterministic full-text analyzer
- `editor-tools.js` — strict-mode rating and rewrite/undo
- `workbench.js` — heatmap, explainable scoring, diff/version workflow, Reply Studio and Document Lab
- `share-tools.js` — Android/browser sharing
- `export-tools.js` — TXT/Markdown/PDF export
- `advanced-stats.js` — extended metrics
- `local-ai.js` — local LLM layer
- `android/` — WebView wrapper, floating toolkit, freehand lasso, Circle OCR and quick-result overlay
- `docs/` — GitHub-native documentation pages

### Android package

`cloud.kosch.antigolem`

### Build locally

```bash
gradle -p android :app:assembleDebug
```

### Live web delivery

The prominent Live Version link uses a rendered view of `main/index.html` so visitors do not land on an unconfigured GitHub Pages 404. GitHub Pages can still be enabled later as an additional canonical deployment.

### Current distribution

The public APK is a debug-signed test build. Production / Play Store distribution should use a persistent private signing key and AAB release pipeline.

</details>

<p align="center"><b>AntiGolem</b> · local · strict · explainable · open</p>