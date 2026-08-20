#!/usr/bin/env python3
"""Parse docs/issues.yml and fail on missing or duplicate epic/task ids."""
from __future__ import annotations

import sys
from pathlib import Path

try:
    import yaml
except ImportError:
    print("error: PyYAML is required", file=sys.stderr)
    sys.exit(2)

ROOT = Path(__file__).resolve().parents[1]


def collect_ids(data: dict) -> list[str]:
    errors: list[str] = []
    epics: set[str] = set()
    tasks: set[str] = set()
    for milestone in data.get("milestones") or []:
        ms = str(milestone.get("name") or "?")
        for epic in milestone.get("epics") or []:
            eid = str(epic.get("id") or "")
            if not eid:
                errors.append(f"epic missing id in milestone {ms}")
                continue
            if eid in epics:
                errors.append(f"duplicate epic id: {eid}")
            if eid in tasks:
                errors.append(f"epic id collides with task id: {eid}")
            epics.add(eid)
            if not epic.get("title"):
                errors.append(f"epic {eid} missing title")
            for task in epic.get("tasks") or []:
                tid = str(task.get("id") or "")
                if not tid:
                    errors.append(f"task missing id under epic {eid}")
                    continue
                if tid in tasks:
                    errors.append(f"duplicate task id: {tid}")
                if tid in epics:
                    errors.append(f"task id collides with epic id: {tid}")
                tasks.add(tid)
                if not task.get("title"):
                    errors.append(f"task {tid} missing title")
    return errors


def main() -> int:
    path = ROOT / "docs" / "issues.yml"
    if len(sys.argv) > 1:
        path = Path(sys.argv[1])
    if not path.is_file():
        print(f"error: manifest not found: {path}", file=sys.stderr)
        return 1
    data = yaml.safe_load(path.read_text(encoding="utf-8")) or {}
    errors = collect_ids(data)
    if errors:
        print("error: " + "; ".join(errors), file=sys.stderr)
        return 1
    print(f"manifest ok: {path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
