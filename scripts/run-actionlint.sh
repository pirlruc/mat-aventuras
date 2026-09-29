#!/usr/bin/env bash
# actionlint at the commondevops ci-lint pin (CI-023). Binary stays in .ci-tools/.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
VER="1.7.12"
SHA256="8aca8db96f1b94770f1b0d72b6dddcb1ebb8123cb3712530b08cc387b349a3d8"
DEST="${ROOT}/.ci-tools"
BIN="${DEST}/actionlint"

if [[ ! -x "${BIN}" ]]; then
  mkdir -p "${DEST}"
  tmp="$(mktemp)"
  curl -fsSL -o "${tmp}" \
    "https://github.com/rhysd/actionlint/releases/download/v${VER}/actionlint_${VER}_linux_amd64.tar.gz"
  echo "${SHA256}  ${tmp}" | sha256sum -c -
  tar -xzf "${tmp}" -C "${DEST}" actionlint
  rm -f "${tmp}"
  chmod +x "${BIN}"
fi

cd "${ROOT}"
# actionlint.yaml at the repo root is picked up automatically.
"${BIN}" .github/workflows/*.yml
