#!/usr/bin/env bash
# Reproduce GitHub Actions quality + test jobs on a developer machine (CI-022).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

python3 scripts/verify-kdoc.py
python3 scripts/verify-maintainability.py
python3 scripts/verify-licenses.py

./gradlew --no-daemon \
  :domain:ktlintCheck \
  :domain:detekt \
  :domain:test \
  :domain:koverVerify

if [[ -f local.properties ]] || [[ -n "${ANDROID_HOME:-}" ]]; then
  ./gradlew --no-daemon \
    :data:testDebugUnitTest \
    :data:koverVerifyDebug \
    :app:testDebugUnitTest \
    :app:koverVerifyDebug \
    :app:lintDebug
fi

python3 scripts/verify-coverage.py
echo "ci-local: all enabled gates passed"
