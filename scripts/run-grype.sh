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
# Catalog the repo (Gradle manifests + lockfiles when present). Only fail on
# high/critical; medium is printed by grype and tracked in MAT-004-T4 for SBOM.
"$GRYPE" dir:"$ROOT" --fail-on high --only-fixed=false
