#!/usr/bin/env python3
"""Fail closed if Kover XML does not meet kotlin/profile.thresholds.yml (CI-022, KT-TEST-002)."""

from __future__ import annotations

import sys
import xml.etree.ElementTree as ET
from pathlib import Path

try:
    import yaml
except ImportError:
    print("error: PyYAML is required", file=sys.stderr)
    sys.exit(2)

ROOT = Path(__file__).resolve().parents[1]
THRESHOLDS = ROOT / "docs" / "guardrails" / "kotlin" / "profile.thresholds.yml"
REPORT = ROOT / "domain" / "build" / "reports" / "kover" / "report.xml"
REQUIRED = ("statement_coverage", "branch_coverage")


def load_thresholds(path: Path) -> dict:
    if not path.is_file():
        print(f"error: missing thresholds file {path}", file=sys.stderr)
        sys.exit(1)
    data = yaml.safe_load(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        print("error: thresholds file is not a mapping", file=sys.stderr)
        sys.exit(1)
    for key in REQUIRED:
        if key not in data or data[key] in (None, ""):
            print(f"error: required threshold {key!r} missing or empty", file=sys.stderr)
            sys.exit(1)
    return data


def counter_percent(root: ET.Element, kind: str) -> float:
    for counter in root.findall("counter"):
        if counter.get("type") == kind:
            missed = int(counter.get("missed", "0"))
            covered = int(counter.get("covered", "0"))
            total = missed + covered
            if total == 0:
                print(f"error: {kind} counter has zero instrumentable lines", file=sys.stderr)
                sys.exit(1)
            return 100.0 * covered / total
    print(f"error: no {kind} counter in Kover report", file=sys.stderr)
    sys.exit(1)


def main() -> int:
    thresholds = load_thresholds(THRESHOLDS)
    if not REPORT.is_file():
        print(f"error: missing Kover report {REPORT}", file=sys.stderr)
        return 1
    root = ET.parse(REPORT).getroot()
    line = counter_percent(root, "LINE")
    branch = counter_percent(root, "BRANCH")
    ok = True
    if line + 1e-9 < float(thresholds["statement_coverage"]):
        print(
            f"error: statement coverage {line:.2f}% < {thresholds['statement_coverage']}",
            file=sys.stderr,
        )
        ok = False
    if branch + 1e-9 < float(thresholds["branch_coverage"]):
        print(
            f"error: branch coverage {branch:.2f}% < {thresholds['branch_coverage']}",
            file=sys.stderr,
        )
        ok = False
    if not ok:
        return 1
    print(f"ok: LINE {line:.2f}% BRANCH {branch:.2f}% (domain)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
