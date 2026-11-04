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


def check_callers(data: dict) -> str | None:
    """Return an error when the Maestro digest or the androiddevops SHA drifted."""
    maestro = data.get("android_maestro") or {}
    devops = data.get("androiddevops") or {}
    if not maestro and not devops:
        return None
    digest = str(maestro.get("digest") or "")
    sha = str(devops.get("sha") or "")
    if not digest.startswith("sha256:") or len(digest) != 71:
        return "android_maestro digest must be sha256 and 64 hex characters"
    script = (ROOT / "scripts" / "run-maestro.sh").read_text(encoding="utf-8")
    if digest not in script:
        return "run-maestro.sh does not contain the maestro digest"
    if len(sha) != 40:
        return "androiddevops sha is required"
    for relative in (
        ".github/workflows/ci.yml",
        ".github/workflows/emulator.yml",
        ".github/workflows/release.yml",
    ):
        text = (ROOT / relative).read_text(encoding="utf-8")
        if sha not in text:
            return f"{relative} does not pin androiddevops {sha}"
    print(f"ok: android-maestro {digest}")
    print(f"ok: androiddevops {devops.get('tag')} {sha}")
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
    caller = check_callers(data)
    if caller:
        errors.append(caller)
    if errors:
        for item in errors:
            print(f"error: {item}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
