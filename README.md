<p align="center">
  <img src="assets/brand/antigolem-logo.svg" width="132" alt="AntiGolem icon" />
</p>

<h1 align="center">AntiGolem</h1>
<p align="center"><b>Semantic Forensics & Language Audit</b><br/>Clearer texts. Stronger agency. Local-first analysis.</p>

<p align="center">
  <a href="https://chekento.github.io/antigolem/"><img alt="Open Live App" src="https://img.shields.io/badge/OPEN-LIVE%20APP-21d9c6?style=for-the-badge&logo=githubpages&logoColor=001018"></a>
  <a href="https://github.com/chekento/antigolem/releases/latest/download/AntiGolem.apk"><img alt="Download AntiGolem APK" src="https://img.shields.io/badge/ANDROID-DOWNLOAD%20APK-6f5cff?style=for-the-badge&logo=android&logoColor=white"></a>
</p>

<p align="center">
  <img src="assets/marketing/hero.svg" width="100%" alt="AntiGolem app overview" />
</p>

## Analyze the whole text — not a few examples

AntiGolem performs a **sentence-by-sentence full-text audit**. The deterministic engine always works without an API key, account, or inference server. It combines structural language analysis with an optional local LLM cross-check.

| Core audit | Extended statistics | Local AI |
|---|---|---|
| BAD / AMBIVALENT / GOOD / NEUTRAL | word count & sentence length | Browser `LanguageModel` / Prompt API when available |
| D-0 / D-1 / D-2+ binding | lexical diversity | optional Qwen2.5-0.5B via WebLLM/WebGPU |
| ghost-context heuristics | absolutism & modal pressure | model runs locally after model/runtime download |
| Golem/Pygmalion framing tags | blame cues & question/exclamation rate | no paid inference API |
| responsibility diffusion / over-inclusive “we” | critical/high risk count | deterministic engine remains the fallback |
| target-group resonance + rewrites | constructive share + heuristic clarity index | local AI is a second opinion, not a diagnosis |

> **Method note:** terms such as “Golem effect”, “Pygmalion effect”, “mantra”, “ghost-context”, and “psychological programming” are used as **editorial/rhetorical heuristics**. AntiGolem does not claim that wording literally programs a brain or proves subconscious causation.

---

<a href="https://github.com/chekento/antigolem/releases/latest/download/AntiGolem.apk">
  <img src="https://img.shields.io/badge/%E2%AC%87%20GET%20ANTIGOLEM%20FOR%20ANDROID-LATEST%20APK-00d8b3?style=for-the-badge&labelColor=07111f" alt="Download latest AntiGolem APK" />
</a>

**Android mode:** activate the AntiGolem Accessibility Service explicitly, then use the floating AntiGolem control to capture text exposed by the currently active Android accessibility tree and analyze it locally. Images, canvas-rendered text, video, protected surfaces, and some PDF viewers may require a future OCR/screen-capture path.

The APK now uses the same **shield + speech bubble + tangled-to-clear text** AntiGolem icon as the repository branding.

---

## Six languages automatically

**Deutsch · English · Français · Español · Italiano · Nederlands**

The GitHub Pages app detects the visitor’s browser languages on first use. If one of the six supported languages is found, it becomes the default. Otherwise AntiGolem starts in **English**. A manually selected language is remembered locally.

---

<p align="center"><img src="assets/marketing/make-your-texts-less-harming.svg" width="100%" alt="Make your texts less harming" /></p>

<p align="center"><img src="assets/marketing/avoid-your-own-threat.svg" width="100%" alt="Avoid your own threat" /></p>

<p align="center"><img src="assets/marketing/its-better-to-write-better.svg" width="100%" alt="It's better to write better" /></p>

---

## Privacy architecture

The rule-based audit is local-first and does not require an AntiGolem backend. When a browser-provided local language model is available, AntiGolem can use it directly. On compatible WebGPU devices, users may optionally load a small open local model; this requires downloading runtime/model files, but the analyzed text is not sent to a paid inference API by AntiGolem.

[Privacy page](./privacy.html) · [Live app](https://chekento.github.io/antigolem/) · [Latest APK](https://github.com/chekento/antigolem/releases/latest/download/AntiGolem.apk)

<details>
<summary><strong>Repository / developer / build details</strong></summary>

### Project structure

- `index.html`, `styles.css`, `app.js` — core GitHub Pages app
- `locale-bootstrap.js` — browser-language detection and English fallback
- `advanced-stats.js` — extended local heuristic metrics
- `local-ai.js` — API-free local LLM cross-check layer
- `report-i18n.js` — localized audit protocol text
- `assets/brand/` — AntiGolem visual identity
- `assets/marketing/` — repository/app marketing artwork
- `android/` — Android wrapper + Accessibility overlay
- `.github/workflows/android.yml` — automated APK build and latest release

### Android package

`cloud.kosch.antigolem`

### Build locally

```bash
gradle -p android :app:assembleDebug
```

### CI / release behavior

Every relevant Android change triggers the GitHub Actions APK workflow. The current public APK is debug-signed for testing. A production/Play Store build should use a private persistent signing key and an AAB release workflow.

### GitHub Pages

The site is static and lives in the repository root. Configure GitHub Pages once with:

`Settings → Pages → Deploy from a branch → main → /(root)`

### Local LLM strategy

1. Prefer the browser-provided `LanguageModel` Prompt API when available.
2. Otherwise, on WebGPU-capable clients, AntiGolem can optionally initialize `Qwen2.5-0.5B-Instruct-q4f16_1-MLC` through WebLLM.
3. If neither local model path is available, the deterministic full-text audit and extended statistics continue to work normally.

</details>

---

<p align="center"><b>AntiGolem</b> · local · private · open · human-centric</p>
