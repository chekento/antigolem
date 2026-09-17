<p align="center"><img src="../assets/brand/antigolem-logo.svg" width="96" alt="AntiGolem icon"></p>

# AntiGolem Privacy

[← Home](../README.md) · [About](about.md) · [Methodology](methodology.md) · [Local AI](local-ai.md)

## Local-first processing

Text pasted or imported into AntiGolem is processed locally by the deterministic analyzer. The project currently has no AntiGolem text-analysis backend.

## Optional local AI

When explicitly started, AntiGolem may use a browser-provided local language model or a WebLLM/WebGPU model. Runtime and model files can require an initial download from third-party hosting, but AntiGolem does not require a paid remote inference API for analyzed text.

## Android AccessibilityService

The floating Android toolkit requires the user to explicitly enable AntiGolem in Android Accessibility settings. When enabled, the service can inspect text and content descriptions that the active application exposes through Android's accessibility tree.

Capture occurs only after a visible user action such as **Analyze visible text** or **Circle Select & analyze**. The implementation does not continuously harvest screen text in the background.

## Circle Select

Circle Select uses accessibility-node screen bounds to decide which exposed text belongs to the marked region. It does not take a screenshot. Image-only text, video, canvas surfaces, protected content and some PDF viewers may therefore not be readable without a future OCR path.

## Local storage

Captured text can be stored temporarily in local app preferences so it can be transferred from the floating toolkit into the analysis screen. Language choice and some UI state can also be stored locally.

## Methodology disclaimer

Golem/Pygmalion, mantra, ghost-context and psychological-programming labels are rhetorical/editorial heuristics, not diagnoses or proof of neural or subconscious causation.
