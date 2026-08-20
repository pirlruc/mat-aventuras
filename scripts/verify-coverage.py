# Fail closed if Kover XML does not meet kotlin/profile.thresholds.yml (CI-022, KT-TEST-002).
# Gates every included module. :data and :app are required when the Android SDK is present.

from __future__ import annotations

import os
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


def android_sdk_present() -> bool:
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
    found = [("domain", ROOT / "domain" / "build" / "reports" / "kover" / "report.xml")]
    if android_sdk_present():
        found.append(("data", find_android_report("data")))
        found.append(("app", find_android_report("app")))
    return found


def find_android_report(module: str) -> Path:
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


def check_report(name: str, path: Path, thresholds: dict) -> bool:
    if not path.is_file():
        print(f"error: missing Kover report {path}", file=sys.stderr)
        return False
    root = ET.parse(path).getroot()
    line = counter_percent(root, "LINE")
    branch = counter_percent(root, "BRANCH")
    ok = True
    if line + 1e-9 < float(thresholds["statement_coverage"]):
        print(
            f"error: {name} statement coverage {line:.2f}% < {thresholds['statement_coverage']}",
            file=sys.stderr,
        )
        ok = False
    if branch + 1e-9 < float(thresholds["branch_coverage"]):
        print(
            f"error: {name} branch coverage {branch:.2f}% < {thresholds['branch_coverage']}",
            file=sys.stderr,
        )
        ok = False
    if ok:
        print(f"ok: LINE {line:.2f}% BRANCH {branch:.2f}% ({name})")
    return ok


def main() -> int:
    thresholds = load_thresholds(THRESHOLDS)
    ok = True
    for name, path in reports():
        ok = check_report(name, path, thresholds) and ok
    return 0 if ok else 1


if __name__ == "__main__":
    raise SystemExit(main())
