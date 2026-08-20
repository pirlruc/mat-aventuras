# AI Agent Handoff Log

Living log for agents picking up work on this repository.

**Last updated:** 2026-08-20
**Last agent focus:** MAT-001 foundation (Compose host, local Room, isolated motors)

---

## What this repo is

Native Kotlin/Compose educational math game (pt-PT) for ages 3 and 7.
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
| MAT-001 | open (implemented in tree; issues not yet synced) | Architecture + playable core |
| MAT-002 | open | Instrumented coverage; retire KT-TEST-002 deviation |

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
./gradlew :dominio:ktlintCheck :dominio:detekt :dominio:test :dominio:koverVerify
python3 scripts/verificar-cobertura.py
```

With Android SDK: `./gradlew :app:assembleDebug`

## Known pitfalls

- Private companion repos: submodule clone needs a PAT with Contents: Read
  on `pirlruc/guardrails` and `pirlruc/github-scaffold`. `.gitmodules` URLs
  are token-free HTTPS.
- `:app` / `:dados` are skipped when `ANDROID_HOME` is unset so JDK-only CI
  can still gate `:dominio`.
- 3D Activity runs in `:motor3d` and must not open Room.
- Do not add `docs/adr/`. Epic MAT-001 is the decision record.
- Scaffold branch convention is `feature-*`; this cloud run used
  `cursor/mat-aventuras-core-ade3` per the agent environment.

## Suggested next work

1. Human: bootstrap labels/milestones and sync `docs/issues.yml`.
2. MAT-002: emulator CI, tighten KT-TEST-002.
3. Optional MAT-003: Godot/Unity as drop-in `LancadorMotor` process plugin.

*Last updated: 2026-08-20*
