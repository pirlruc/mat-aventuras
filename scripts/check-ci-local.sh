#!/usr/bin/env bash
# Local CI parity (CI-008). Host PATH first; no extra OS packages.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

python3 .github/scaffold/scripts/validate-yaml.py \
  docs/issues.yml \
  docs/guardrail-deviations.yml \
  docs/issues-sync-targets.yml \
  .github/dependabot.yml \
  .github/workflows/ci.yml

python3 .github/scaffold/scripts/issues-sync.py --yaml docs/issues.yml --validate-only
python3 .github/scaffold/scripts/lint-doc-links.py --root "$ROOT"

./gradlew :dominio:ktlintCheck :dominio:detekt :dominio:test :dominio:koverXmlReport :dominio:koverVerify
python3 scripts/verificar-cobertura.py
