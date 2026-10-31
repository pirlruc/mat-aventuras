#!/bin/sh
# Run CI steps missing on the host inside ci-lint.
# The package is private. This repo has Actions read; a local pull needs a GHCR login.
# POSIX sh. The image digest matches commondevops ci-lint 5.6.1 (still current).
#
# Env:
#   MAT_CI_IMAGE     — override the image (digest pin by default)
#   MAT_DOCKER_STEPS — space-separated step names (required)
#
# Steps: actionlint shellcheck yamllint zizmor gitleaks
set -eu

SCRIPT_DIR="$(dirname "$0")"
SCRIPT_DIR="$(cd "${SCRIPT_DIR}" && pwd)"
ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
# ghcr.io/pirlruc/ci-lint (Debian). Tag is documentation; the digest is the pin.
IMAGE="${MAT_CI_IMAGE:-ghcr.io/pirlruc/ci-lint@sha256:f9001c4c8726f72107545094237d4fc0592baca023cfa299a532634666d88111}"

if [ -z "${MAT_DOCKER_STEPS:-}" ]; then
	echo "MAT_DOCKER_STEPS is required (e.g. shellcheck yamllint zizmor gitleaks)" >&2
	exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
	echo "docker is required for host-unavailable CI checks" >&2
	exit 1
fi

echo "==> Pulling ${IMAGE}"
docker pull "${IMAGE}"

echo "==> Running in Docker: ${MAT_DOCKER_STEPS}"
docker run --rm \
	-v "${ROOT}:/workspace:ro" \
	-w /workspace \
	-e "MAT_DOCKER_STEPS=${MAT_DOCKER_STEPS}" \
	--entrypoint sh \
	"${IMAGE}" \
	-c '
    set -eu
    run_step() {
      case "$1" in
        actionlint) actionlint .github/workflows/*.yml ;;
        shellcheck) shellcheck --severity=error scripts/*.sh ;;
        yamllint) yamllint -c .yamllint.yml .github docs config ;;
        zizmor) zizmor --min-severity=low .github/workflows .github/dependabot.yml ;;
        # The ci-lint image has no git. --no-git scans the tree, not history.
        gitleaks) gitleaks detect --no-git --source . --no-banner --exit-code 1 ;;
        *) echo "unknown step: $1" >&2; return 1 ;;
      esac
    }
    for step in ${MAT_DOCKER_STEPS}; do
      echo "==> ${step}"
      run_step "${step}"
    done
  '
