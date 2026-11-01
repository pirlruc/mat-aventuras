#!/bin/sh
# Import the Godot project and fail when a script does not parse.
# Godot 4.7.2 official editor, cached under .ci-tools (gitignored).
set -eu

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ASSETS="${ROOT}/app/src/main/assets"
VERSION="4.7.2-stable"
BIN_NAME="Godot_v${VERSION}_linux.x86_64"
CACHE="${ROOT}/.ci-tools/godot"
BIN="${CACHE}/${BIN_NAME}"
URL="https://github.com/godotengine/godot-builds/releases/download/${VERSION}/${BIN_NAME}.zip"

if [ ! -x "${BIN}" ]; then
    mkdir -p "${CACHE}"
    tmp="$(mktemp -d)"
    curl -fsSL -o "${tmp}/godot.zip" "${URL}"
    unzip -q -o "${tmp}/godot.zip" -d "${CACHE}"
    chmod +x "${BIN}"
    rm -rf "${tmp}"
fi

# `--import` aborts on 4.7.2 because the editor singleton is null.
# `--editor` scans every script but still exits 0 on a parse error,
# so the log is what fails the check.
log="$(mktemp)"
"${BIN}" --headless --editor --path "${ASSETS}" --quit >"${log}" 2>&1 || {
    cat "${log}"
    rm -f "${log}"
    exit 1
}
if grep -E 'SCRIPT ERROR:|Failed to load script' "${log}"; then
    rm -f "${log}"
    exit 1
fi
rm -f "${log}"
echo "godot: project imported"
