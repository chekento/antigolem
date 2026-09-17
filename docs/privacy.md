<p align="center"><img src="../assets/brand/antigolem-logo.svg" width="96" alt="AntiGolem icon"></p>

# AntiGolem Privacy

[← Home](../README.md) · [About](about.md) · [Methodology](methodology.md) · [Local AI](local-ai.md)

## Local-first processing

Text pasted or imported into AntiGolem is processed locally by the deterministic analyzer. The project has no AntiGolem text-analysis backend.

## Optional local AI

When explicitly started, AntiGolem may use a browser-provided local language model or a WebLLM/WebGPU model. Runtime and model files can require an initial download from third-party hosting, but AntiGolem does not require a paid remote inference API for analyzed text.

## Android AccessibilityService

The floating Android toolkit requires the user to explicitly enable AntiGolem in Android Accessibility settings. The service can inspect text/content descriptions exposed by the active application only for explicit user-requested actions.

Capture does not run continuously in the background.

## Freehand Circle Select

Circle Select is a true freehand lasso. AntiGolem records the finger path as a polygon and automatically closes the path when the finger is lifted. Accessibility-visible text is selected by intersecting node screen bounds with that actual polygon. The standard freehand text mode does **not** take a screenshot.

## Freehand Circle OCR

On Android 11+, **Circle OCR** is a separate explicit action. AntiGolem requests a one-shot screenshot through Android's AccessibilityService screenshot capability, crops to the lasso bounds, masks everything outside the hand-drawn polygon locally, and performs Latin-script recognition with the **bundled on-device ML Kit OCR model**. The screenshot is not uploaded by AntiGolem and is released after OCR. Protected/secure Android windows cannot be screenshot-scanned.

## Quick result overlay

Captured text can be stored temporarily in local app preferences so the floating quick-result card and the full analyzer can use the same explicit capture. The small overlay score is a preliminary estimate; the main app computes the full strict sentence audit and grounded percentages.

## Document Lab

The user can explicitly select multiple TXT, Markdown, HTML, CSV, JSON, XML, RTF, PDF, DOCX and image files. Plain-text formats are processed directly. In the browser/WebView, PDF.js, Mammoth and Tesseract.js can be downloaded from public CDN hosting the first time PDF, DOCX or image OCR is used. Those runtime downloads may expose ordinary network metadata to their hosts; extracted document text remains in the local AntiGolem page.

## Local storage

Captured text, language preference, temporary versions and some UI state can be stored locally on the device/browser.

## Methodology disclaimer

Golem/Pygmalion, mantra, ghost-context and psychological-programming labels are rhetorical/editorial heuristics, not diagnoses or proof of neural or subconscious causation. Counts and percentages describe detected signals under AntiGolem's published rules.
