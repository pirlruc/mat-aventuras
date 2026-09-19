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
  .github/workflows/hardening.yml
python3 scripts/verify-companion-pins.py
python3 scripts/lint-doc-links.py --root "$ROOT"

if [[ -f .github/scaffold/scripts/issues-sync.py ]]; then
  python3 .github/scaffold/scripts/issues-sync.py --yaml docs/issues.yml --validate-only
fi

bash scripts/ci-local.sh
