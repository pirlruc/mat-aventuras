#!/bin/sh
# Fail when config/kotlin.thresholds.yml is looser than the pinned
# guardrails Kotlin profile. doc_coverage may be absent (KT-DOC-001).
# A present key may be stricter. POSIX sh (CI-022).
set -eu

SCRIPT_DIR="$(dirname "$0")"
SCRIPT_DIR="$(cd "${SCRIPT_DIR}" && pwd)"
ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
VENDORED="${ROOT}/config/kotlin.thresholds.yml"
UPSTREAM="${ROOT}/docs/guardrails/kotlin/profile.thresholds.yml"

if [ ! -f "${VENDORED}" ] || [ ! -f "${UPSTREAM}" ]; then
    echo "error: missing ${VENDORED} or ${UPSTREAM}" >&2
    exit 1
fi

value_of() {
    file="$1"
    key="$2"
    line="$(grep -E "^${key}:" "${file}" || true)"
    if [ -z "${line}" ]; then
        return 1
    fi
    val="${line#*:}"
    val="${val%%#*}"
    val="$(printf '%s' "${val}" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
    printf '%s' "${val}"
}

is_number() {
    case "$1" in
    '' | *[!0-9]*)
        return 1
        ;;
    esac
    return 0
}

# Coverage floors are looser when smaller. Caps are looser when larger.
looser() {
    key="$1"
    left="$2"
    right="$3"
    case "${key}" in
    statement_coverage | branch_coverage | doc_coverage)
        [ "${left}" -lt "${right}" ]
        ;;
    max_cyclomatic_complexity | pr_size_soft_limit_lines | lint_exception_max_days)
        [ "${left}" -gt "${right}" ]
        ;;
    *)
        [ "${left}" != "${right}" ]
        ;;
    esac
}

status=0
keys="$(grep -E '^[a-z0-9_]+:' "${UPSTREAM}" | sed 's/:.*//' | sort -u)"
vendored_keys="$(grep -E '^[a-z0-9_]+:' "${VENDORED}" | sed 's/:.*//' | sort -u)"

while IFS= read -r key; do
    [ -n "${key}" ] || continue
    if ! value_of "${VENDORED}" "${key}" >/dev/null; then
        if [ "${key}" = "doc_coverage" ]; then
            echo "omitted: doc_coverage (KT-DOC-001)"
            continue
        fi
        echo "error: ${key} missing from ${VENDORED}" >&2
        status=1
        continue
    fi
    left="$(value_of "${VENDORED}" "${key}")"
    right="$(value_of "${UPSTREAM}" "${key}")"
    if ! is_number "${left}" || ! is_number "${right}"; then
        if [ "${left}" != "${right}" ]; then
            echo "error: ${key} drifted" >&2
            echo "  vendored: ${left}" >&2
            echo "  upstream: ${right}" >&2
            status=1
        else
            echo "in sync: ${key}"
        fi
        continue
    fi
    if looser "${key}" "${left}" "${right}"; then
        echo "error: ${key} is looser than the org default" >&2
        echo "  vendored: ${left}" >&2
        echo "  upstream: ${right}" >&2
        status=1
        continue
    fi
    echo "in sync: ${key}"
done <<EOF
${keys}
EOF

while IFS= read -r key; do
    [ -n "${key}" ] || continue
    if ! value_of "${UPSTREAM}" "${key}" >/dev/null; then
        echo "error: ${key} is not an org threshold" >&2
        status=1
    fi
done <<EOF
${vendored_keys}
EOF

if [ "${status}" -ne 0 ]; then
    exit 1
fi
echo "Vendored Kotlin thresholds are at least the org defaults."
