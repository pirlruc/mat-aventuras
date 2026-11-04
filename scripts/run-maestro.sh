#!/usr/bin/env bash
# Local device flows. GitHub Actions skips this. No device skips this.
# Maestro stays in the android-maestro image. Do not install it on the host.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
IMAGE="ghcr.io/pirlruc/android-maestro@sha256:aba49b8d5f8d87c291a6e6c59b9163d071ade024f85de61e87be1c9869b030da"
APP="pt.mataventuras.app"

if [[ -n "${GITHUB_ACTIONS:-}" ]]; then
    echo "maestro: skipped on GitHub Actions"
    exit 0
fi

if ! command -v adb >/dev/null 2>&1; then
    echo "maestro: skipped, adb is not on PATH"
    exit 0
fi

if ! adb devices | awk 'NR > 1 && $2 == "device" { found = 1 } END { exit !found }'; then
    echo "maestro: skipped, no device"
    exit 0
fi

if ! adb shell pm path "$APP" >/dev/null 2>&1; then
    export JAVA_HOME="${JAVA_HOME:-/opt/android-studio/jbr}"
    export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"
    (cd "$ROOT" && ./gradlew :app:installDebug --no-daemon)
fi

sdk="${ANDROID_HOME:-$HOME/Android/Sdk}"
os="$(docker info --format '{{.OperatingSystem}}' 2>/dev/null || true)"
args=(--rm)
if [[ "$os" == *"Docker Desktop"* ]]; then
    adb -a start-server >/dev/null 2>&1 || true
    args+=(--add-host=host.docker.internal:host-gateway)
    args+=(-e "ADB_SERVER_SOCKET=tcp:host.docker.internal:5037")
else
    args+=(--network host)
fi

docker pull "$IMAGE"
docker run "${args[@]}" \
    -v "${sdk}/platform-tools:/opt/platform-tools:ro" \
    -e PATH="/opt/platform-tools:/opt/maestro/bin:/opt/java/openjdk/bin:/usr/bin" \
    -v "$ROOT:/workspace" \
    -w /workspace \
    "$IMAGE" \
    maestro test .maestro
