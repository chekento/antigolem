<p align="center"><img src="../assets/brand/antigolem-logo.svg" width="96" alt="AntiGolem icon"></p>

# AntiGolem Methodology

[← Home](../README.md) · [About](about.md) · [Local AI](local-ai.md) · [Privacy](privacy.md)

## Full-text audit

Every detected sentence enters the statistical base. AntiGolem does not intentionally reduce a document to a few illustrative examples.

## Strict rating mode

The engine is intentionally conservative: a clear negative-normalization signal is not cancelled merely because the same sentence also contains positive wording. D-2+ is treated as critical, D-1 more cautiously, and absolutism, modal pressure, responsibility diffusion and over-inclusive “we” can independently increase severity.

## Explainable score stack

The 1.5 workbench exposes a transparent per-sentence score stack. The current UI assigns visible penalty/offset points to detected signals so users can see *why* a sentence was flagged instead of receiving an unexplained label. The stack currently surfaces:

- negative-normalization cues,
- D-2+ ghost-context risk,
- D-1 reference distance,
- absolutism,
- modal pressure,
- responsibility diffusion / over-inclusive collective framing,
- constructive framing offsets.

These point values are **editorial prioritization weights**, not probabilities and not psychological measurements. They exist to make the strict rule system inspectable.

## D-factor

- **D-0:** reference is heuristically local and explicit.
- **D-1:** reference reaches into the previous sentence and deserves review.
- **D-2+:** reference distance or ambiguity is high enough to trigger ghost-context risk.

This is a heuristic co-reference model, not a full linguistic parser.

## Grounded counts and percentages

Reply Studio and Document Lab calculate their numbers from the current sentence audit. For example, an “issue rate” is the number of sentences rated **SCHLECHT/BAD or AMBIVALENT** divided by the number of detected sentences. BAD share, critical/high share, D-1/D-2+ counts and signal counts are reported separately so a user can inspect what the percentage actually represents.

A generated reply must not invent an error percentage. It can only use values produced by the current analysis.

## Rewriting and diff review

The direct rewrite workflow preserves source versions and supports:

1. deterministic conservative proposal,
2. before/after strict metrics,
3. sentence-level Accept / Keep original,
4. Accept all,
5. optional local-model refinement,
6. local version restoration.

Automatic rewrites are instructed to preserve facts, names, figures, links, chronology, viewpoint and intended meaning, but still require human review.

## Reply Studio tones

Reply Studio includes Friendly, Very polite, Professional, Short/social, Detailed constructive, Question-led, Unfriendly, Dissing and Challenging modes.

The sharper modes are deliberately constrained to criticize **the wording, reasoning or presentation**, not to degrade the author as a person. They do not justify slurs, threats, protected-class attacks or claims about the author’s mental state. A “diss” can be pointed or sarcastic about the text while remaining grounded in the audit evidence.

## OCR and document extraction

- Android Circle OCR uses an explicit user-drawn region and bundled on-device ML Kit Latin-script OCR on Android 11+.
- Browser/WebView Document Lab can use PDF.js for PDF extraction, Mammoth for DOCX and Tesseract.js for image OCR after those public runtimes are loaded.
- Protected Android windows remain unavailable to screenshot OCR.

## Limits

“Golem effect”, “Pygmalion effect”, “mantra”, “ghost-context” and “psychological programming” are editorial/rhetorical labels in AntiGolem. They are not clinical findings, medical diagnoses or proof that wording literally programs a brain.
