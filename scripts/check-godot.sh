#!/bin/sh
# Import the Godot project and fail when a script does not parse.
# Godot 4.7.1 official editor, cached under .ci-tools (gitignored).
set -eu

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ASSETS="${ROOT}/app/src/main/assets"
VERSION="4.7.1-stable"
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

"${BIN}" --headless --path "${ASSETS}" --import --quit
echo "godot: project imported"
