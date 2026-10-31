#!/usr/bin/env bash
# Reproduce GitHub Actions quality + test jobs on a developer machine (CI-022).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if ! .ci-venv/bin/python -c 'import defusedxml, yaml' >/dev/null 2>&1; then
  uv venv --clear .ci-venv
  uv pip install --python .ci-venv/bin/python pyyaml defusedxml
fi
PY=".ci-venv/bin/python"

"$PY" scripts/verify-kdoc.py
"$PY" scripts/verify-licenses.py
"$PY" scripts/verify-privacy-manifest.py
"$PY" scripts/verify-companion-pins.py
"$PY" scripts/validate-issues.py docs/issues.yml

./gradlew --no-daemon \
  :domain:ktlintCheck \
  :domain:detekt \
  :domain:test \
  :domain:koverVerify

if [[ -f local.properties ]] || [[ -n "${ANDROID_HOME:-}" ]]; then
  ./gradlew --no-daemon \
    :data:ktlintCheck \
    :data:detekt \
    :app:ktlintCheck \
    :app:detekt \
    :data:testDebugUnitTest \
    :data:koverVerifyDebug \
    :app:testDebugUnitTest \
    :app:koverVerifyDebug \
    :app:lintDebug
fi

"$PY" scripts/verify-coverage.py
echo "ci-local: all enabled gates passed"
