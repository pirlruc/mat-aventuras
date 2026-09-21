#!/usr/bin/env bash
# CycloneDX SBOM of a built APK, then OSV (SC-SBOM-001 / SC-SBOM-002).
# Fail closed if the APK or either tool is missing (CI-035).
# High/Critical findings fail the job (CI-005).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SYFT_VERSION="1.52.0"
OSV_VERSION="2.6.0"

if [[ $# -ne 1 || ! -f $1 ]]; then
  echo "error: pass the built APK path" >&2
  exit 1
fi

apk_dir="$(cd "$(dirname "$1")" && pwd)"
APK="${apk_dir}/$(basename "$1")"
WORKDIR="${TMPDIR:-/tmp}/mat-aventuras-osv-sbom-${SYFT_VERSION}"
mkdir -p "$WORKDIR"
SYFT="${WORKDIR}/syft"
OSV="${WORKDIR}/osv-scanner"
SBOM="${WORKDIR}/app.cdx.json"
SARIF="${WORKDIR}/osv.sarif"

install_syft() {
  local url
  url="https://github.com/anchore/syft/releases/download/v${SYFT_VERSION}/syft_${SYFT_VERSION}_linux_amd64.tar.gz"
  curl -fsSL -o "${WORKDIR}/syft.tgz" "$url"
  tar -xzf "${WORKDIR}/syft.tgz" -C "$WORKDIR" syft
  chmod +x "$SYFT"
}

install_osv() {
  local url
  url="https://github.com/google/osv-scanner/releases/download/v${OSV_VERSION}/osv-scanner_linux_amd64"
  curl -fsSL -o "$OSV" "$url"
  chmod +x "$OSV"
}

if [[ ! -x $SYFT ]]; then
  install_syft
fi
if [[ ! -x $OSV ]]; then
  install_osv
fi
if [[ ! -x $SYFT || ! -x $OSV ]]; then
  echo "error: syft or osv-scanner is not executable" >&2
  exit 1
fi

"$SYFT" scan "file:${APK}" -o "cyclonedx-json=${SBOM}" -q
if [[ ! -s $SBOM ]]; then
  echo "error: CycloneDX SBOM was not written" >&2
  exit 1
fi

status=0
"$OSV" scan source -L "$SBOM" --format sarif --output-file "$SARIF" --verbosity error || status=$?
if [[ $status -gt 1 ]]; then
  echo "error: osv-scanner failed (${status})" >&2
  exit "$status"
fi
if [[ ! -s $SARIF ]]; then
  echo "error: osv-scanner wrote no SARIF" >&2
  exit 1
fi

python3 "${ROOT}/scripts/fail-on-sarif-severity.py" --min-severity high "$SARIF"
