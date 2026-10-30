#!/usr/bin/env bash
# Local CI parity (CI-008). Host PATH first; no extra OS packages.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

python3 scripts/validate-yaml.py \
  docs/issues.yml \
  docs/guardrail-deviations.yml \
  docs/issues-sync-targets.yml \
  docs/companion-pins.yml \
  .github/dependabot.yml \
  .github/workflows/ci.yml \
  .github/workflows/hardening.yml \
  .github/workflows/codeql.yml \
  .github/workflows/shared-ci.yml \
  .github/workflows/python-quality.yml
python3 scripts/verify-companion-pins.py
sh scripts/check-submodule-pins.sh
python3 scripts/lint-doc-links.py --root "$ROOT"

# Host PATH first. Missing linters run in the ci-lint image.
queued=""
if command -v actionlint >/dev/null 2>&1 || command -v curl >/dev/null 2>&1; then
  bash scripts/run-actionlint.sh
else
  queued="${queued} actionlint"
fi
if command -v shellcheck >/dev/null 2>&1; then
  shellcheck --severity=error scripts/*.sh
else
  queued="${queued} shellcheck"
fi
if command -v yamllint >/dev/null 2>&1; then
  yamllint -c .yamllint.yml .github docs config
else
  queued="${queued} yamllint"
fi
if command -v zizmor >/dev/null 2>&1; then
  zizmor --min-severity=low .github/workflows .github/dependabot.yml
else
  queued="${queued} zizmor"
fi
if command -v gitleaks >/dev/null 2>&1; then
  gitleaks detect --source . --no-banner --exit-code 1
else
  queued="${queued} gitleaks"
fi
if [[ -n "${queued}" ]]; then
  # shellcheck disable=SC2086
  MAT_DOCKER_STEPS="${queued# }" bash scripts/check-ci-docker.sh
fi

if [[ -f .github/scaffold/scripts/issues-sync.py ]]; then
  python3 .github/scaffold/scripts/issues-sync.py --yaml docs/issues.yml --validate-only
fi

bash scripts/ci-local.sh
