# AI Agent Handoff Log

Living log for agents picking up work on this repository.

**Last updated:** 2026-08-20
**Last agent focus:** App 95% coverage; GLES kart session; off-track snap; plugin contract

---

## What this repo is

Native Kotlin/Compose educational math game for ages 3 and 7.
**UI/TTS/dialogue:** Portuguese from Portugal.
**Code, comments, KDoc, documentation:** English.
Privacy: on-device only. Decision log: GitHub Epics via github-issue-adr;
authored backlog is `docs/issues.yml`.

## Pins

| Companion | How | Value |
| --- | --- | --- |
| methodologies | annotated tag in docs | `1.2.0` |
| guardrails | submodule SHA `docs/guardrails/` | `0354a747` (tag `1.3.0`) |
| github-scaffold | submodule SHA `.github/scaffold/` | `aac408cc` (tag `1.2.0`) |

## Delivery status

| Epic | Status | Notes |
| --- | --- | --- |
| MAT-001 | open in GitHub until human sync; tasks done in tree | Compose host, local Room, isolated engines |
| MAT-002 | open | Emulator instrumented tests remain; Robolectric 95% and richer packs are in tree |
| MAT-003 | open | Native oval kart + plugin Intent contract in tree; Godot/Unity AAR is later |
| MAT-004 | open in GitHub until human sync; tasks done in tree | Split CI, SAST, gitleaks binary, KDoc/MI/license scripts |

`docs/guardrail-deviations.yml` is empty. Do not re-add KT-TEST-002.
KT-DELIV-001 (500-line PR soft limit) is not a gap for this first PR.

GitHub labels/milestones and issue sync need a write token:

```bash
bash .github/scaffold/scripts/setup-issue-scaffold.sh
python3 .github/scaffold/scripts/issues-sync.py --repo pirlruc/mat-aventuras --yaml docs/issues.yml --dry-run
```

This agent cannot `gh issue create` (read-only `gh`, and methodology forbids
publishing issues without approval).

## Commands

```bash
python3 .github/scaffold/scripts/issues-sync.py --yaml docs/issues.yml --validate-only
python3 .github/scaffold/scripts/lint-doc-links.py --root .
./gradlew :domain:ktlintCheck :domain:detekt :domain:test :domain:koverVerify
python3 scripts/verify-coverage.py
bash scripts/ci-local.sh
```

## Known pitfalls

- Private companion repos: submodule clone needs a PAT with Contents: Read
  on `pirlruc/guardrails` and `pirlruc/github-scaffold`. `.gitmodules` URLs
  are token-free HTTPS.
- `:app` / `:data` are skipped when `ANDROID_HOME` is unset so JDK-only CI
  can still gate `:domain`. With the SDK, coverage is required for all three.
- 3D Activity runs in `:engine3d` and must not open Room.
- Do not add `docs/adr/`. Epic MAT-001 is the decision record.
- Scaffold branch convention is `feature-*`; this cloud run used
  `cursor/mat-aventuras-core-ade3` per the agent environment.
- VM JDK may be 21; target JVM 17 bytecode without `jvmToolchain(17)`.
- Run `:domain:ktlintFormat` before `:domain:ktlintCheck` (parallel format+check races).

## Suggested next work

1. Human: bootstrap labels/milestones and sync `docs/issues.yml`.
2. MAT-002-T1: emulator instrumented tests in CI.
3. Optional Godot/Unity drop-in still isolated in `:engine3d` (contract is documented).

*Last updated: 2026-08-20*
