"""Fail closed if Kover XML does not meet kotlin/profile.thresholds.yml.

CI-022 and KT-TEST-002. Gates every included module. :data and :app are
required when the Android SDK is present. An overlay below the org floors
fails closed instead of being applied.
"""

from __future__ import annotations

import os
import sys
import xml.etree.ElementTree as ET  # nosec B405
from pathlib import Path

try:
    import yaml
except ImportError:
    print("error: PyYAML is required", file=sys.stderr)
    sys.exit(2)

ROOT = Path(__file__).resolve().parents[1]
THRESHOLDS_CANDIDATES = (
    ROOT / "docs" / "guardrails" / "kotlin" / "profile.thresholds.yml",
    ROOT / "config" / "kotlin.thresholds.yml",
)
REQUIRED = ("statement_coverage", "branch_coverage")


def load_thresholds(path: Path) -> dict:
    """Load statement and branch floors. Exit 1 when a required key is empty."""
    if not path.is_file():
        print(f"error: missing thresholds file {path}", file=sys.stderr)
        sys.exit(1)
    data = yaml.safe_load(path.read_text(encoding="utf-8"))
    if not isinstance(data, dict):
        print("error: thresholds file is not a mapping", file=sys.stderr)
        sys.exit(1)
    for key in REQUIRED:
        if key not in data or data[key] in (None, ""):
            print(
                f"error: required threshold {key!r} missing or empty", file=sys.stderr
            )
            sys.exit(1)
    return data


def android_sdk_present() -> bool:
    """Return true when ANDROID_HOME or local.properties sdk.dir is a directory."""
    env = os.environ.get("ANDROID_HOME") or os.environ.get("ANDROID_SDK_ROOT")
    if env and Path(env).is_dir():
        return True
    local = ROOT / "local.properties"
    if not local.is_file():
        return False
    for raw in local.read_text(encoding="utf-8").splitlines():
        if raw.startswith("sdk.dir="):
            path = raw.split("=", 1)[1].strip().replace("\\\\", "/")
            return Path(path).is_dir()
    return False


def reports() -> list[tuple[str, Path]]:
    """Return Kover report paths. Android modules are included only with an SDK."""
    found = [("domain", ROOT / "domain" / "build" / "reports" / "kover" / "report.xml")]
    if android_sdk_present():
        found.append(("data", find_android_report("data")))
        found.append(("app", find_android_report("app")))
    return found


def find_android_report(module: str) -> Path:
    """Pick the first existing Android Kover XML, or the debug default path."""
    base = ROOT / module / "build" / "reports" / "kover"
    candidates = [
        base / "reportDebug.xml",
        base / "xml" / "reportDebug.xml",
        base / "report.xml",
        base / "xml" / "report.xml",
    ]
    for path in candidates:
        if path.is_file():
            return path
    nested = sorted(base.glob("**/*.xml")) if base.is_dir() else []
    if nested:
        return nested[0]
    return candidates[0]


def counter_percent(root: ET.Element, kind: str) -> float:
    """Return the covered percent for a Kover counter type."""
    for counter in root.findall("counter"):
        if counter.get("type") == kind:
            missed = int(counter.get("missed", "0"))
            covered = int(counter.get("covered", "0"))
            total = missed + covered
            if total == 0:
                print(
                    f"error: {kind} counter has zero instrumentable lines",
                    file=sys.stderr,
                )
                sys.exit(1)
            return 100.0 * covered / total
    print(f"error: no {kind} counter in Kover report", file=sys.stderr)
    sys.exit(1)


def check_report(name: str, path: Path, thresholds: dict) -> bool:
    """Return true when one module report meets both floors."""
    if not path.is_file():
        print(f"error: missing Kover report {path}", file=sys.stderr)
        return False
    root = ET.parse(path).getroot()  # nosec B314
    line = counter_percent(root, "LINE")
    branch = counter_percent(root, "BRANCH")
    ok = True
    line_floor = thresholds["statement_coverage"]
    branch_floor = thresholds["branch_coverage"]
    if line + 1e-9 < float(line_floor):
        print(
            f"error: {name} statement coverage {line:.2f}% < {line_floor}",
            file=sys.stderr,
        )
        ok = False
    if branch + 1e-9 < float(branch_floor):
        print(
            f"error: {name} branch coverage {branch:.2f}% < {branch_floor}",
            file=sys.stderr,
        )
        ok = False
    if ok:
        print(f"ok: LINE {line:.2f}% BRANCH {branch:.2f}% ({name})")
    return ok


def thresholds_path() -> Path:
    """Prefer the pinned pack, and refuse an overlay below the org floors."""
    primary = THRESHOLDS_CANDIDATES[0]
    overlay = THRESHOLDS_CANDIDATES[1]
    if primary.is_file() and overlay.is_file():
        org = load_thresholds(primary)
        local = load_thresholds(overlay)
        for key in REQUIRED:
            if float(local[key]) < float(org[key]):
                print(
                    f"error: overlay {key} {local[key]} "
                    f"is below org default {org[key]}",
                    file=sys.stderr,
                )
                sys.exit(1)
        return overlay
    for path in THRESHOLDS_CANDIDATES:
        if path.is_file():
            return path
    print(
        "error: missing thresholds file "
        + " or ".join(str(path) for path in THRESHOLDS_CANDIDATES),
        file=sys.stderr,
    )
    sys.exit(1)


def main() -> int:
    """Return 0 when every included module meets the coverage floors."""
    thresholds = load_thresholds(thresholds_path())
    ok = True
    for name, path in reports():
        ok = check_report(name, path, thresholds) and ok
    return 0 if ok else 1


if __name__ == "__main__":
    raise SystemExit(main())
