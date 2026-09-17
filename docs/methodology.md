<p align="center"><img src="../assets/brand/antigolem-logo.svg" width="96" alt="AntiGolem icon"></p>

# AntiGolem Methodology

[← Home](../README.md) · [About](about.md) · [Local AI](local-ai.md) · [Privacy](privacy.md)

## Full-text audit

Every detected sentence enters the statistical base. AntiGolem does not intentionally reduce a document to a few illustrative examples.

## Strict rating mode

The current engine is intentionally conservative: a clear negative-normalization signal is not cancelled merely because the same sentence also contains positive wording. D-2+ is treated as critical, D-1 more cautiously than before, and absolutism, modal pressure, responsibility diffusion and over-inclusive “we” can increase severity.

## D-factor

- **D-0:** reference is heuristically local and explicit.
- **D-1:** reference reaches into the previous sentence and deserves review.
- **D-2+:** reference distance or ambiguity is high enough to trigger ghost-context risk.

This is a heuristic co-reference model, not a full linguistic parser.

## Framing and responsibility

AntiGolem looks for negative-normalization framing, constructive framing, absolutism, modal pressure, blame cues, vague responsibility, broad collective framing and agency-supporting alternatives.

## Rewriting

**Apply improvements** preserves the source version for Undo, tries an on-device/local model when available, and otherwise uses deterministic conservative corrections. Rewrites are instructed to preserve facts, names, figures, links, chronology, viewpoint and intended meaning.

## Friendly critique

For analyzed posts, AntiGolem can create an editable reply that asks for clearer and more constructive wording without asserting motives or presenting psychological effects as proven facts.

## Limits

“Golem effect”, “Pygmalion effect”, “mantra”, “ghost-context” and “psychological programming” are editorial/rhetorical labels in AntiGolem. They are not clinical findings, medical diagnoses or proof that wording literally programs a brain.
