#!/usr/bin/env bash
# Local/CI runner for MobSF's source scanner (mobsfscan).
# Kotlin/Android SAST without Docker — same gate finsilo uses for mobile hardening.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
VERSION="1.0.0"
LOCK_SHA="6146c3b38d8915e92742a2a4eef6bd276047ac91"
VENV="${TMPDIR:-/tmp}/mat-aventuras-mobsfscan-${VERSION}"
OUT="${MOBSF_SARIF:-$ROOT/app/build/reports/mobsfscan.sarif}"
mkdir -p "$(dirname "$OUT")"

install_mobsfscan() {
  if python3 -m venv "$VENV" >/dev/null 2>&1; then
    curl -fsSL \
      -o "${VENV}/requirements-mobsfscan.txt" \
      "https://raw.githubusercontent.com/pirlruc/androiddevops/${LOCK_SHA}/scripts/requirements-mobsfscan.txt"
    "$VENV/bin/pip" install --disable-pip-version-check --require-hashes \
      -r "${VENV}/requirements-mobsfscan.txt"
    echo "$VENV/bin/mobsfscan"
    return
  fi
  echo "error: python3-venv is required so mobsfscan can be hash-pinned" >&2
  exit 1
}

if [[ -x "$VENV/bin/mobsfscan" ]]; then
  MOBSF="$VENV/bin/mobsfscan"
elif command -v mobsfscan >/dev/null 2>&1; then
  MOBSF="$(command -v mobsfscan)"
else
  MOBSF="$(install_mobsfscan)"
fi
if [[ ! -x "$MOBSF" ]]; then
  echo "error: mobsfscan not installed at $MOBSF" >&2
  exit 1
fi
cd "$ROOT"
"$MOBSF" \
  --type android \
  --sarif \
  -o "$OUT" \
  --no-fail \
  "$ROOT/app/src/main" \
  "$ROOT/domain/src/main" \
  "$ROOT/data/src/main"
if [[ ! -s "$OUT" ]]; then
  echo "error: mobsfscan wrote no SARIF (fail closed)" >&2
  exit 1
fi
echo "Wrote $OUT"
python3 "$ROOT/scripts/fail-on-sarif-severity.py" --min-severity high "$OUT"
