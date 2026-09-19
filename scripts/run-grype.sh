#!/usr/bin/env bash
# KT-SEC-004: filesystem vulnerability scan (grype). Fail closed if the tool
# cannot run (CI-035). High/Critical findings fail the job (CI-005).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
VERSION="0.118.0"
WORKDIR="${TMPDIR:-/tmp}/mat-aventuras-grype-${VERSION}"
mkdir -p "$WORKDIR"
GRYPE="$WORKDIR/grype"

install_grype() {
  local url="https://github.com/anchore/grype/releases/download/v${VERSION}/grype_${VERSION}_linux_amd64.tar.gz"
  curl -fsSL -o "$WORKDIR/grype.tgz" "$url"
  tar -xzf "$WORKDIR/grype.tgz" -C "$WORKDIR" grype
  chmod +x "$GRYPE"
}

if [[ ! -x "$GRYPE" ]]; then
  if command -v grype >/dev/null 2>&1; then
    GRYPE="$(command -v grype)"
  else
    install_grype
  fi
fi
if [[ ! -x "$GRYPE" ]]; then
  echo "error: grype not installed at $GRYPE (CI-035)" >&2
  exit 1
fi

cd "$ROOT"
"$GRYPE" version
# Catalog Gradle manifests. Skip CI venvs, build trees, and git metadata so
# runner Python (semgrep/mobsfscan) is not treated as product dependencies.
"$GRYPE" dir:"$ROOT" \
  --fail-on high \
  --only-fixed=false \
  --exclude '**/.ci-venv/**' \
  --exclude '**/build/**' \
  --exclude '**/.gradle/**' \
  --exclude '**/.git/**'
