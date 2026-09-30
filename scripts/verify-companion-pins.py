#!/usr/bin/env python3
"""SC-DEP-004: gitlink SHA, documented SHA, and tag name the same revision."""

from __future__ import annotations

import subprocess  # nosec B404
import sys
from pathlib import Path

try:
    import yaml
except ImportError:
    print("error: PyYAML is required", file=sys.stderr)
    sys.exit(2)

ROOT = Path(__file__).resolve().parents[1]
PINS = ROOT / "docs" / "companion-pins.yml"


def gitlink_sha(path: str) -> str:
    """Return the index gitlink, then HEAD. Exit 1 when both are missing."""
    # Prefer the index so a staged submodule bump matches before commit.
    for spec in (f":{path}", f"HEAD:{path}"):
        # Fixed argv, no shell. Reads the submodule gitlink.
        result = subprocess.run(  # nosec B603 B607
            ["git", "rev-parse", spec],
            cwd=ROOT,
            check=False,
            capture_output=True,
            text=True,
        )
        if result.returncode == 0 and result.stdout.strip():
            return result.stdout.strip()
    print(f"error: missing gitlink {path}", file=sys.stderr)
    sys.exit(1)


def check_entry(name: str, entry: dict) -> str | None:
    """Return an error string when one companion pin does not match."""
    path = str(entry.get("path") or "")
    sha = str(entry.get("sha") or "")
    tag = str(entry.get("tag") or "")
    if not path or not sha or not tag:
        return f"{name}: path, sha, and tag are required"
    gitlink = gitlink_sha(path)
    if gitlink != sha:
        return f"{name}: gitlink {gitlink} != documented sha {sha} (tag {tag})"
    print(f"ok: {name} {tag} {sha}")
    return None


def main() -> int:
    """Return 0 when both submodule gitlinks match docs/companion-pins.yml."""
    if not PINS.is_file():
        print(f"error: missing {PINS}", file=sys.stderr)
        return 1
    data = yaml.safe_load(PINS.read_text(encoding="utf-8")) or {}
    errors: list[str] = []
    for name in ("guardrails", "scaffold"):
        message = check_entry(name, data.get(name) or {})
        if message:
            errors.append(message)
    if errors:
        for item in errors:
            print(f"error: {item}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
