# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- Replace the fixed ages with bands of 3–5 and 6–8 years. Modules can belong
  to both bands. Existing profiles migrate from `THREE_YEARS` and
  `SEVEN_YEARS` without deleting rows.
- The name field accepts 24 characters, matching `ChildName`.
- Add an emulator job (`workflow_dispatch` and a monthly schedule) and
  device smoke tests for Room and the Godot plugin processes.
- Apply ktlint and detekt to `:app` and `:data`. Compose functions are
  ignored by the complexity and length rules; the canvas hosts keep a
  local suppression until they are split.
- Parent PIN storage uses `MasterKey.Builder` (`security-crypto` `1.1.0-alpha06`).
- Enable R8 on the release variant and keep the Godot JNI types.
- Drop the static-analysis, complexity, and `MissingSuperCall` deviations.
  `KT-TEST-002` and `KT-DOC-001` remain.
- Pin guardrails `1.13.1`, github-scaffold `1.10.2`, and methodologies `1.9.2`.
- Call commondevops `5.6.2` (pins verify, advisory Scorecard, markdownlint)
  on a public Ubuntu image, because `ci-lint` is not visible to this repo.
  pydevops `python-quality` `3.3.1` stays skipped until that image pull works.
- Drop in-repo copies of gitleaks, Semgrep, Grype, actionlint, shellcheck,
  and zizmor. Those run through the reusable workflows. Host-missing linters
  use `scripts/check-ci-docker.sh`.
- Read the SC-LIC-001 deny list from the guardrails supply-chain thresholds.
- Bump `actions/setup-java` to `6.0.1` and Gradle wrapper validation to `6.3.0`.
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
- Add a root `pyproject.toml` (`package = false`) and call pydevops
  `python-quality` `2.1.1` for the CI helper scripts.
- Authenticate the guardrails submodule clone with `git -c`, parse CI XML
  with defusedxml, and call python-quality on Python 3.13.
- Pin the uv dependency cutoff to the lock timestamp. python-quality's
  uv 0.6.9 cannot parse `exclude-newer = "7 days"`.
- Leave the python-quality caller skipped until repository variable
  `PYTHON_QUALITY_ENABLED` is `true`. `uv tool install pytest-cov` exits 1
  on the pinned uv (PDO-TOOL-001).

### Fixed

- Type Godot pointer samples so a Dictionary value cannot drop a prize script.
- Default the boot scene to the 2D runner when the engine plugin is absent.
- Give every Godot prize a Sair control that settles the reward as not won.
- Keep the GLES restart flag on the Activity instead of the process.
- Cap the child-name and mascot extras read by the engine.
