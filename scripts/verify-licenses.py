#!/usr/bin/env python3
"""SC-LIC-001: scan Gradle version catalog licenses against allow/deny lists.

Empty allow and deny lists still run the gate: every declared module is reported,
and the job fails only when a denied license appears.
"""

from __future__ import annotations

import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
CATALOG = ROOT / "gradle" / "libs.versions.toml"
# Populate as third-party licenses are classified. Empty lists mean "deny nothing yet".
ALLOW: set[str] = set()
DENY: set[str] = {
    "GPL-2.0",
    "GPL-3.0",
    "AGPL-3.0",
    "SSPL-1.0",
}

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
}


def modules() -> list[str]:
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


def main() -> int:
    denied: list[str] = []
    print("SC-LIC-001 license scan (version catalog):")
    for module in modules():
        license_id = KNOWN.get(module, "UNKNOWN")
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
