<p align="center"><img src="../assets/brand/antigolem-logo.svg" width="96" alt="AntiGolem icon"></p>

# AntiGolem Local AI

[← Home](../README.md) · [About](about.md) · [Methodology](methodology.md) · [Privacy](privacy.md)

## API-free first

AntiGolem’s deterministic audit always remains available without an API key, account or paid inference service.

## Local model strategy

1. **Browser-provided LanguageModel / Prompt API** when the browser or Android WebView exposes a compatible on-device model.
2. **WebLLM/WebGPU fallback** on compatible devices, using a compact local model such as `Qwen2.5-0.5B-Instruct-q4f16_1-MLC`.
3. **Deterministic fallback** when neither local-model path is available.

## What local AI is used for

- linguistic cross-check of the deterministic audit
- direct full-text rewrite while preserving facts and viewpoint
- refinement of friendly wording-critique replies
- synthesis of findings across larger text chunks

## Privacy boundary

AntiGolem does not require a paid cloud-inference API. Model/runtime files may need to be downloaded initially from external hosting and cached locally. During the intended local inference path, analyzed text is processed on-device rather than sent to an AntiGolem inference backend.

## Compatibility

Local AI availability depends on browser/WebView version, WebGPU support, RAM, storage and the device’s model capabilities. The core audit, strict rating, statistics and conservative rewrite fallback continue to work without local AI.
