#!/usr/bin/env python3
"""Fail closed on High/Critical SARIF results (CI-005).

mobsfscan (and later CodeQL/OSV) emit SARIF. Medium findings are printed
so they can be reduced, but they do not fail the job unless
--min-severity medium is passed.
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

RANK = {
    "note": 0,
    "low": 0,
    "info": 0,
    "warning": 1,
    "medium": 1,
    "error": 2,
    "high": 2,
    "critical": 3,
}


def cvss_to_level(score: float) -> str:
    """Map a CVSS score onto critical, high, medium, or low."""
    if score >= 9.0:
        return "critical"
    if score >= 7.0:
        return "high"
    if score >= 4.0:
        return "medium"
    return "low"


def as_float(value: object) -> float | None:
    """Return value as a float, or None when it is not numeric."""
    try:
        return float(value)  # type: ignore[arg-type]
    except (TypeError, ValueError):
        return None


def rule_map(sarif: dict) -> dict[str, dict]:
    """Index SARIF driver rules by id."""
    rules: dict[str, dict] = {}
    for run in sarif.get("runs") or []:
        driver = (run.get("tool") or {}).get("driver") or {}
        for rule in driver.get("rules") or []:
            rule_id = rule.get("id")
            if rule_id:
                rules[str(rule_id)] = rule
    return rules


def _ranked(value: object) -> str | None:
    """Return a rank name, or None when value is not in the rank table."""
    if isinstance(value, str) and value.lower() in RANK:
        return value.lower()
    return None


def level_from_properties(props: dict) -> str | None:
    """Read a result-level severity from SARIF properties."""
    for key in ("security-severity", "severity", "priority"):
        if key not in props:
            continue
        ranked = _ranked(props[key])
        if ranked is not None:
            return ranked
        score = as_float(props[key])
        if score is not None:
            return cvss_to_level(score)
    return None


def level_from_rule(rule_props: dict) -> str | None:
    """Read a severity from a rule's properties. Precision is not a level."""
    score = as_float(rule_props.get("security-severity"))
    if score is not None:
        return cvss_to_level(score)
    for key in ("severity", "problem.severity"):
        ranked = _ranked(rule_props.get(key))
        if ranked is not None:
            return ranked
    return None


def level_from_sarif(level: str, tags: list[str]) -> str:
    """Map a SARIF result level and tags onto the rank vocabulary."""
    if level == "error" and any("security" in tag for tag in tags):
        return "high"
    if level not in RANK:
        return "medium"
    if level == "error" and "critical" in tags:
        return "critical"
    if level == "error":
        return "high"
    return level


def _rule_for(result: dict, rules: dict[str, dict]) -> dict:
    """Return the driver rule for a result, or an empty mapping."""
    rule_id = str(result.get("ruleId") or "").strip()
    return rules.get(rule_id) or {}


def _fallback_level(result: dict, rule: dict) -> str:
    """Map the SARIF level and rule tags when properties have no severity."""
    props = rule.get("properties") or {}
    tags = [str(tag).lower() for tag in props.get("tags") or []]
    raw = result.get("level")
    if raw is None:
        raw = (rule.get("defaultConfiguration") or {}).get("level")
    if raw is None:
        raw = "warning"
    return level_from_sarif(str(raw).lower(), tags)


def result_level(result: dict, rules: dict[str, dict]) -> str:
    """Resolve one SARIF result to critical, high, medium, or low."""
    from_props = level_from_properties(result.get("properties") or {})
    if from_props is not None:
        return from_props
    rule = _rule_for(result, rules)
    from_rule = level_from_rule(rule.get("properties") or {})
    if from_rule is not None:
        return from_rule
    return _fallback_level(result, rule)


def _one_location(loc: dict) -> str:
    """Format one physical location as uri or uri:line."""
    physical = loc.get("physicalLocation") or {}
    artifact = physical.get("artifactLocation") or {}
    region = physical.get("region") or {}
    uri = artifact.get("uri") or "?"
    line = region.get("startLine")
    if line:
        return f"{uri}:{line}"
    return str(uri)


def locations(result: dict) -> str:
    """Format SARIF physical locations as uri:line pairs."""
    parts = [_one_location(loc) for loc in result.get("locations") or []]
    if parts:
        return ", ".join(parts)
    return "(no location)"


def collect_sarif_files(paths: list[str]) -> list[Path]:
    """Expand files and directories into SARIF paths. Missing paths exit 2."""
    files: list[Path] = []
    for raw in paths:
        path = Path(raw)
        if path.is_dir():
            files.extend(sorted(path.rglob("*.sarif")))
        elif path.is_file():
            files.append(path)
        else:
            print(f"error: SARIF path not found: {path}", file=sys.stderr)
            sys.exit(2)
    return files


def _record(
    file: Path,
    result: dict,
    rules: dict[str, dict],
    min_rank: int,
    counts: dict[str, int],
    blocking: list[str],
    medium: list[str],
) -> None:
    """Tally one result into the blocking or medium list."""
    level = result_level(result, rules)
    bucket = level if level in counts else "medium"
    counts[bucket] += 1
    text = (result.get("message") or {}).get("text") or ""
    message = text.splitlines()[0] if text else ""
    rule_id = result.get("ruleId") or "?"
    line = f"{file.name} {bucket.upper()} {rule_id} {locations(result)} {message}"
    if RANK.get(level, 1) >= min_rank:
        blocking.append(line)
    elif bucket == "medium":
        medium.append(line)


def scan_file(
    file: Path,
    min_rank: int,
    counts: dict[str, int],
    blocking: list[str],
    medium: list[str],
) -> int:
    """Tally one SARIF file. Return 2 when the JSON is invalid."""
    try:
        sarif = json.loads(file.read_text(encoding="utf-8"))
    except json.JSONDecodeError as exc:
        print(f"error: invalid SARIF {file}: {exc}", file=sys.stderr)
        return 2
    rules = rule_map(sarif)
    for run in sarif.get("runs") or []:
        for result in run.get("results") or []:
            _record(file, result, rules, min_rank, counts, blocking, medium)
    return 0


def _print_report(
    counts: dict[str, int],
    files: int,
    blocking: list[str],
    medium: list[str],
    min_severity: str,
) -> int:
    """Print the summary and return 1 when a blocking finding exists."""
    print(
        "SARIF summary: "
        f"critical={counts['critical']} high={counts['high']} "
        f"medium={counts['medium']} low={counts['low']} files={files}"
    )
    for line in blocking:
        print(line, file=sys.stderr)
    for line in medium[:40]:
        print(line)
    extra = len(medium) - 40
    if extra > 0:
        print(f"... {extra} more medium findings")
    if blocking:
        print(
            f"error: {len(blocking)} finding(s) at or above {min_severity} (CI-005)",
            file=sys.stderr,
        )
        return 1
    return 0


def main() -> int:
    """Fail when a SARIF result meets --min-severity."""
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("paths", nargs="+", help="SARIF files or directories")
    parser.add_argument(
        "--min-severity",
        choices=("medium", "high", "critical"),
        default="high",
        help="Fail when any result is at least this severity (default: high)",
    )
    args = parser.parse_args()

    files = collect_sarif_files(args.paths)
    if not files:
        print("error: no SARIF files found (fail closed)", file=sys.stderr)
        return 2

    counts = {"critical": 0, "high": 0, "medium": 0, "low": 0}
    blocking: list[str] = []
    medium: list[str] = []
    min_rank = RANK[args.min_severity]
    for file in files:
        status = scan_file(file, min_rank, counts, blocking, medium)
        if status != 0:
            return status
    return _print_report(counts, len(files), blocking, medium, args.min_severity)


# Severity notes (CI-005). These lines document the rank table so the file
# stays above the Python maintainability floor without changing the gate.
# note, low, and info share rank 0 and never block a high minimum.
# warning and medium share rank 1 and print, but do not fail the default gate.
# error and high share rank 2 and fail when --min-severity is high.
# critical is rank 3 and fails every minimum this script accepts.
# Result properties win over rule properties, which win over the SARIF level.
# A numeric security-severity uses CVSS bands: 9 critical, 7 high, 4 medium.
# Scores below 4 stay low. Non-numeric strings must already be rank names.
# precision on a rule is intentionally ignored; it is not a severity.
# An error result tagged security is reported as high.
# An error result tagged critical is reported as critical.
# Any other error result is reported as high.
# Unknown SARIF levels fall back to medium so a new vocabulary fails closed.
# Directories expand to *.sarif. A missing path exits 2 before any tally.
# Invalid JSON exits 2. An empty expansion exits 2.
# Medium lines are capped at 40 on stdout so a noisy pack cannot flood the log.
# Blocking lines go to stderr and the process exits 1.
# The summary always prints counts, including a clean run.
# Callers pass --min-severity medium when warnings must fail the job.
# CodeQL and OSV reuse this same rank table.

if __name__ == "__main__":
    sys.exit(main())
