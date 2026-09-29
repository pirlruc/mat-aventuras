#!/usr/bin/env bash
# Init docs/guardrails with GUARDRAILS_READ_TOKEN (CI-022).
# Push to main fails closed when the token is missing. Every other event
# skips and keeps config/kotlin.thresholds.yml. Dependabot has no Actions
# secret (CI-024), so that skip is required.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

token="${GUARDRAILS_READ_TOKEN:-}"

if [[ -z "$token" ]]; then
  if [[ "${GITHUB_EVENT_NAME:-}" == "push" && "${GITHUB_REF:-}" == "refs/heads/main" ]]; then
    echo "GUARDRAILS_READ_TOKEN is required to init docs/guardrails on push to main" >&2
    exit 1
  fi
  echo "GUARDRAILS_READ_TOKEN unset; skipping docs/guardrails (overlay thresholds remain)"
  exit 0
fi

header="$(printf 'x-access-token:%s' "$token" | base64 | tr -d '\n')"

cleanup() {
  git config --local --unset-all http.https://github.com/.extraheader >/dev/null 2>&1 || true
}
trap cleanup EXIT

export GIT_TERMINAL_PROMPT=0
git config --local http.https://github.com/.extraheader "AUTHORIZATION: basic ${header}"
# Full clone. A shallow fetch of the pinned non-tip commit fails.
git submodule update --init docs/guardrails
