#!/usr/bin/env python3
"""KT-DOC-001: public Kotlin types carry KDoc. No numeric coverage ratio."""

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
DECL = re.compile(
    r"^(\s*)(?:(?:data|inner|enum|sealed|abstract|open|value|inline)\s+)*"
    r"(class|interface|object|typealias)\s+([A-Za-z_][A-Za-z0-9_]*)",
)
SKIP_NAMES = {"Companion"}


def is_public_decl(raw: str, match: re.Match[str]) -> bool:
    """Return true for a top-level public type declaration."""
    indent = match.group(1)
    name = match.group(3)
    if name in SKIP_NAMES or indent != "":
        return False
    hidden = ("private ", "internal ", "protected ")
    return not any(token in raw for token in hidden)


def scan_file(path: pathlib.Path) -> tuple[int, list[str]]:
    """Count public types in one file and list those without KDoc."""
    missing: list[str] = []
    total = 0
    lines = path.read_text(encoding="utf-8").splitlines()
    for index, raw in enumerate(lines):
        match = DECL.match(raw.rstrip())
        if match is None or not is_public_decl(raw, match):
            continue
        total += 1
        lookback = "\n".join(lines[max(0, index - 24) : index])
        if "/**" not in lookback:
            name = match.group(3)
            missing.append(f"{path.relative_to(ROOT)}:{index + 1}:{name}")
    return total, missing


def main() -> int:
    """Return 0 when every public Kotlin type has KDoc."""
    missing: list[str] = []
    total = 0
    for pattern in SRC_GLOBS:
        for path in sorted(ROOT.glob(pattern)):
            count, found = scan_file(path)
            total += count
            missing.extend(found)
    print(f"kdoc public types: {total - len(missing)}/{total} documented")
    if missing:
        print("KT-DOC-001 missing KDoc on public types:", file=sys.stderr)
        for item in missing[:40]:
            print(f"  {item}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
