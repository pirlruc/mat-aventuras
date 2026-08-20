#!/usr/bin/env python3
"""KT-CPLX-002: reject functions whose maintainability index is below 40.

Uses the original 0–171 MI (radon/Microsoft Halstead form) computed per
function, not per file. File-level aggregation treats a Compose screen as one
giant unit and is not comparable to the org threshold.
"""

from __future__ import annotations

import math
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
MIN_MI = 40.0
FUN = re.compile(r"^(\s*)(?:(?:private|internal|protected|public|override|suspend|inline|tailrec|operator)\s+)*fun\s+", re.M)
BRANCH = re.compile(r"\b(if|when|for|while|catch)\b")
OPERATORS = re.compile(r"[+\-*/%=<>!&|^?:]+")
WORDS = re.compile(r"[A-Za-z_][A-Za-z0-9_]*")


def maintainability(source: str) -> float:
    loc = max(1, len([line for line in source.splitlines() if line.strip() and not line.strip().startswith("//")]))
    cyclomatic = 1 + len(BRANCH.findall(source))
    operands = WORDS.findall(source)
    operators = OPERATORS.findall(source)
    n = max(1, len(operands) + len(operators))
    n2 = max(1, len(set(operands)))
    volume = n * math.log2(n2 + 1)
    # Original 0–171 scale (not the 0–100 normalisation).
    return max(0.0, 171 - 5.2 * math.log(max(volume, 1.0)) - 0.23 * cyclomatic - 16.2 * math.log(loc))


def functions(source: str) -> list[tuple[str, str]]:
    lines = source.splitlines()
    starts: list[int] = []
    names: list[str] = []
    for index, line in enumerate(lines):
        match = re.match(
            r"^(\s*)(?:(?:private|internal|protected|public|override|suspend|inline|tailrec|operator)\s+)*"
            r"fun\s+([A-Za-z_][A-Za-z0-9_]*)",
            line,
        )
        if match:
            starts.append(index)
            names.append(match.group(2))
    starts.append(len(lines))
    out: list[tuple[str, str]] = []
    for i, name in enumerate(names):
        body = "\n".join(lines[starts[i] : starts[i + 1]])
        out.append((name, body))
    if not out:
        out.append(("<file>", source))
    return out


def main() -> int:
    failures: list[str] = []
    scores: list[tuple[float, str]] = []
    for path in sorted(ROOT.glob("**/src/main/kotlin/**/*.kt")):
        if "build/" in str(path):
            continue
        rel = str(path.relative_to(ROOT))
        source = path.read_text(encoding="utf-8")
        for name, body in functions(source):
            mi = maintainability(body)
            label = f"{rel}#{name}"
            scores.append((mi, label))
            if mi < MIN_MI:
                failures.append(f"{label}: MI={mi:.1f} < {MIN_MI}")
    scores.sort()
    print("lowest maintainability scores:")
    for mi, rel in scores[:15]:
        print(f"  {mi:5.1f}  {rel}")
    if failures:
        print("KT-CPLX-002 failures:", file=sys.stderr)
        for item in failures:
            print(f"  {item}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
