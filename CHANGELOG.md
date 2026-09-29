# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- Pin guardrails `1.8.0` and github-scaffold `1.7.0`, and sync the scaffold
  release (rules, issue templates, `AGENTS.md`, `SKILLS.md`).
- Cite methodologies `1.7.0` from repo-owned docs.
- Run actionlint `1.7.12` (the commondevops ci-lint pin) and rescan security
  jobs on the 1st and the 15th.
- Record non-numeric guardrail departures (Android static analysis, the
  Godot `MissingSuperCall` suppression, coverage exclusions, Dokka
  `doc_coverage` not applicable) and track the SDK-only proof as MAT-006.
- Init `docs/guardrails` with `GUARDRAILS_READ_TOKEN` and call commondevops
  `5.1.2` reusables with `COMMONDEVOPS_READ_TOKEN`.
