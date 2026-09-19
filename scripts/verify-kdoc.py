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


def main() -> int:
    missing: list[str] = []
    total = 0
    for glob in SRC_GLOBS:
        for path in sorted(ROOT.glob(glob)):
            lines = path.read_text(encoding="utf-8").splitlines()
            for index, raw in enumerate(lines):
                match = DECL.match(raw.rstrip())
                if match is None:
                    continue
                indent, _kind, name = match.group(1), match.group(2), match.group(3)
                if name in SKIP_NAMES or indent != "":
                    continue
                if any(token in raw for token in ("private ", "internal ", "protected ")):
                    continue
                total += 1
                lookback = "\n".join(lines[max(0, index - 24) : index])
                if "/**" not in lookback:
                    missing.append(f"{path.relative_to(ROOT)}:{index + 1}:{name}")
    print(f"kdoc public types: {total - len(missing)}/{total} documented")
    if missing:
        print("KT-DOC-001 missing KDoc on public types:", file=sys.stderr)
        for item in missing[:40]:
            print(f"  {item}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
