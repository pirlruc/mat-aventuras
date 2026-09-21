#!/usr/bin/env bash
# KT-SEC-003: secret scan for a local pre-commit hook.
# CI runs the same detector. Fail closed when gitleaks is not installed (CI-035).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
if ! command -v gitleaks >/dev/null 2>&1; then
  echo "error: gitleaks is required for the pre-commit secret scan (KT-SEC-003)" >&2
  echo "Install gitleaks 8.24.3, or unset core.hooksPath. CI still scans every push." >&2
  exit 1
fi
gitleaks detect --source "$ROOT" --no-banner --redact --exit-code 1
