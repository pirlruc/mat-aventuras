#!/usr/bin/env python3
"""CycloneDX SBOM for the runtime classpath that produced a built APK.

An Android APK stores dex, not Maven coordinates, so OSV cannot match
packages inside the archive. This reads `gradle :app:dependencies` output
and records the APK's SHA-256 on the root component (SC-SBOM-001).
"""
from __future__ import annotations

import hashlib
import json
import re
import sys
from pathlib import Path

COORD = re.compile(r"([A-Za-z0-9_.\-]+):([A-Za-z0-9_.\-]+):([A-Za-z0-9_.+\-]+)")
ARROW = re.compile(r"->\s+([A-Za-z0-9_.+\-]+)")


def parse_dependencies(text: str) -> list[tuple[str, str, str]]:
    resolved: dict[tuple[str, str], str] = {}
    for line in text.splitlines():
        if "---" not in line:
            continue
        payload = line.split("---", 1)[1].strip()
        if payload.startswith("project "):
            continue
        match = COORD.match(payload)
        if match is None:
            continue
        group, name, version = match.group(1), match.group(2), match.group(3)
        arrow = ARROW.search(payload)
        if arrow is not None:
            version = arrow.group(1)
        resolved[(group, name)] = version
    return sorted((group, name, version) for (group, name), version in resolved.items())


def purl(group: str, name: str, version: str) -> str:
    return f"pkg:maven/{group}/{name}@{version}"


def bom(apk: Path, dependencies: list[tuple[str, str, str]]) -> dict:
    digest = hashlib.sha256(apk.read_bytes()).hexdigest()
    components = []
    for group, name, version in dependencies:
        coordinate = purl(group, name, version)
        components.append(
            {
                "type": "library",
                "group": group,
                "name": name,
                "version": version,
                "purl": coordinate,
                "bom-ref": coordinate,
            }
        )
    return {
        "bomFormat": "CycloneDX",
        "specVersion": "1.5",
        "version": 1,
        "metadata": {
            "component": {
                "type": "application",
                "name": apk.name,
                "bom-ref": apk.name,
                "hashes": [{"alg": "SHA-256", "content": digest}],
            }
        },
        "components": components,
    }


def main() -> int:
    if len(sys.argv) != 4:
        print("usage: gradle-to-cyclonedx.py APK DEPS_TXT SBOM_JSON", file=sys.stderr)
        return 2
    apk = Path(sys.argv[1])
    deps = Path(sys.argv[2])
    out = Path(sys.argv[3])
    if not apk.is_file():
        print(f"error: APK not found: {apk}", file=sys.stderr)
        return 1
    if not deps.is_file():
        print(f"error: dependency report not found: {deps}", file=sys.stderr)
        return 1
    packages = parse_dependencies(deps.read_text(encoding="utf-8", errors="replace"))
    if not packages:
        print("error: no Maven coordinates in the runtime classpath", file=sys.stderr)
        return 1
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(json.dumps(bom(apk, packages), indent=2) + "\n", encoding="utf-8")
    print(f"CycloneDX components={len(packages)} apk={apk.name}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
