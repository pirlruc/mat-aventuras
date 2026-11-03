#!/usr/bin/env python3
"""SC-LIC-001: scan Gradle version catalog licenses against allow/deny lists.

Empty allow and deny lists still run the gate: every declared module is reported,
and the job fails only when a denied license appears.
"""

from __future__ import annotations

import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
CATALOG = ROOT / "gradle" / "libs.versions.toml"
THRESHOLDS = ROOT / "docs" / "guardrails" / "supply-chain" / "profile.thresholds.yml"
# Populate as third-party licenses are classified. Empty lists mean "deny nothing yet".
ALLOW: set[str] = set()
# Same ids as supply-chain profile.thresholds.yml license_deny_list.
_DENY_FALLBACK = {
    "AGPL-3.0",
    "SSPL-1.0",
    "BUSL-1.1",
    "Commons-Clause",
    "CC-BY-NC",
}

BILL = ROOT / "build" / "reports" / "cyclonedx" / "bom.json"


def load_deny() -> set[str]:
    """Merge the org license_deny_list with the local fallback."""
    denied = set(_DENY_FALLBACK)
    if not THRESHOLDS.is_file():
        return denied
    match = re.search(
        r"license_deny_list:\s*\[([^\]]*)\]",
        THRESHOLDS.read_text(encoding="utf-8"),
    )
    if match:
        denied.update(re.findall(r'"([^"]+)"', match.group(1)))
    return denied


DENY: set[str] = load_deny()

# Known SPDX identifiers for catalog artifacts (offline, no Maven fetch).
KNOWN: dict[str, str] = {
    "org.jetbrains.kotlin:kotlin-stdlib": "Apache-2.0",
    "org.jetbrains.kotlinx:kotlinx-coroutines-android": "Apache-2.0",
    "androidx.core:core-ktx": "Apache-2.0",
    "androidx.activity:activity-compose": "Apache-2.0",
    "androidx.lifecycle:lifecycle-runtime-ktx": "Apache-2.0",
    "androidx.navigation:navigation-compose": "Apache-2.0",
    "androidx.room:room-runtime": "Apache-2.0",
    "androidx.room:room-ktx": "Apache-2.0",
    "androidx.compose:compose-bom": "Apache-2.0",
    "androidx.compose.ui:ui": "Apache-2.0",
    "androidx.compose.material3:material3": "Apache-2.0",
    "com.google.android.material:material": "Apache-2.0",
    "junit:junit": "EPL-1.0",
    "dev.detekt:detekt-gradle-plugin": "Apache-2.0",
    "io.gitlab.arturbosch.detekt:detekt-gradle-plugin": "Apache-2.0",
    "org.jlleitschuh.gradle:ktlint-gradle": "MIT",
    "org.jetbrains.kotlinx:kover-gradle-plugin": "Apache-2.0",
    "org.godotengine:godot": "MIT",
    "androidx.fragment:fragment-ktx": "Apache-2.0",
    "androidx.security:security-crypto": "Apache-2.0",
}


def modules() -> list[str]:
    """Return sorted Maven coordinates declared in the version catalog."""
    text = CATALOG.read_text(encoding="utf-8")
    found = re.findall(r'module\s*=\s*"([^"]+)"', text)
    found += re.findall(r'group\s*=\s*"([^"]+)"\s*,\s*name\s*=\s*"([^"]+)"', text)
    modules: list[str] = []
    for item in found:
        if isinstance(item, tuple):
            modules.append(f"{item[0]}:{item[1]}")
        else:
            modules.append(item)
    return sorted(set(modules))


def bill_rows(path: pathlib.Path) -> list[tuple[str, str]]:
    """Return component name and SPDX id pairs from a CycloneDX JSON bill."""
    document = json.loads(path.read_text(encoding="utf-8"))
    rows: list[tuple[str, str]] = []
    for component in document.get("components", []):
        name = str(component.get("bom-ref") or component.get("name") or "unknown")
        licenses = component.get("licenses") or []
        if not licenses:
            rows.append((name, "UNKNOWN"))
            continue
        for entry in licenses:
            license_node = entry.get("license") or entry.get("expression") or {}
            if isinstance(license_node, str):
                rows.append((name, license_node))
                continue
            rows.append((name, str(license_node.get("id") or license_node.get("name") or "UNKNOWN")))
    return rows


def main() -> int:
    """Return 0 when no component uses a denied license."""
    denied: list[str] = []
    if BILL.is_file():
        print(f"SC-LIC-002 license scan ({BILL.relative_to(ROOT)}):")
        pairs = bill_rows(BILL)
    else:
        print("SC-LIC-001 license scan (version catalog):")
        pairs = [(module, KNOWN.get(module, "UNKNOWN")) for module in modules()]
    for module, license_id in pairs:
        print(f"  {module}: {license_id}")
        if license_id in DENY:
            denied.append(f"{module} uses denied license {license_id}")
        if ALLOW and license_id not in ALLOW and license_id != "UNKNOWN":
            denied.append(f"{module} uses {license_id} which is outside the allow list")
    if denied:
        for item in denied:
            print(item, file=sys.stderr)
        return 1
    print("no denied licenses")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
