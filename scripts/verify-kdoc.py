#!/usr/bin/env python3
"""KT-DOC-001: require KDoc on at least 95% of public Kotlin declarations."""

from __future__ import annotations

import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
SRC_GLOBS = (
    "domain/src/main/kotlin/**/*.kt",
    "data/src/main/kotlin/**/*.kt",
    "app/src/main/kotlin/**/*.kt",
)

PUBLIC = re.compile(
    r"^\s*public\s+(?:data\s+|inner\s+|enum\s+|sealed\s+|abstract\s+|open\s+|value\s+)?"
    r"(class|interface|object|fun|typealias)\s+",
)
# Kotlin defaults to public. Count types and functions, not properties (KT-DOC-001).
DECL = re.compile(
    r"^(\s*)(?:(?:data|inner|enum|sealed|abstract|open|value|inline|suspend|tailrec|operator|const)\s+)*"
    r"(class|interface|object|fun|typealias)\s+([A-Za-z_][A-Za-z0-9_]*)",
)
SKIP_NAMES = {"Companion", "invoke", "component1", "component2", "copy"}
SKIP_PREFIX = ("get", "set")


def is_public(indent: str, line: str, previous_modifiers: str) -> bool:
    if "private " in line or "internal " in line or "protected " in line:
        return False
    if indent in ("", "    ") and not line.lstrip().startswith("import"):
        return True
    return False


def main() -> int:
    documented = 0
    total = 0
    missing: list[str] = []
    for glob in SRC_GLOBS:
        for path in sorted(ROOT.glob(glob)):
            lines = path.read_text(encoding="utf-8").splitlines()
            for index, raw in enumerate(lines):
                line = raw.rstrip()
                match = DECL.match(line)
                if match is None:
                    continue
                indent, kind, name = match.group(1), match.group(2), match.group(3)
                if name in SKIP_NAMES:
                    continue
                if kind == "fun" and len(indent) > 4:
                    continue
                if kind in ("class", "interface", "object", "typealias") and len(indent) > 4:
                    continue
                if "private " in line or "internal " in line or "protected " in line:
                    continue
                if "override " in line:
                    continue
                if kind in ("fun", "val", "var") and name.startswith(SKIP_PREFIX) and len(name) > 3:
                    continue
                total += 1
                lookback = "\n".join(lines[max(0, index - 8) : index])
                has_kdoc = "/**" in lookback
                # File-level declarations may use a file KDoc immediately above.
                if has_kdoc:
                    documented += 1
                else:
                    missing.append(f"{path.relative_to(ROOT)}:{index + 1}:{name}")
    ratio = 0.0 if total == 0 else documented / total
    print(f"kdoc public coverage: {documented}/{total} = {ratio:.1%}")
    if ratio + 1e-9 < 0.95:
        print("missing KDoc (first 40):")
        for item in missing[:40]:
            print(f"  {item}")
        print("KT-DOC-001 requires ≥95% public KDoc.", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
