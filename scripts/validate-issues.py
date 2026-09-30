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


def _remember(ident: str, seen: set[str], other: set[str], kind: str) -> list[str]:
    """Record one id and report duplicates or epic/task collisions."""
    errors: list[str] = []
    if ident in seen:
        errors.append(f"duplicate {kind} id: {ident}")
    if ident in other:
        other_kind = "task" if kind == "epic" else "epic"
        errors.append(f"{kind} id collides with {other_kind} id: {ident}")
    seen.add(ident)
    return errors


def _check_task(
    task: dict, epic_id: str, epics: set[str], tasks: set[str]
) -> list[str]:
    """Return id and title errors for one task."""
    tid = str(task.get("id") or "")
    if not tid:
        return [f"task missing id under epic {epic_id}"]
    errors = _remember(tid, tasks, epics, "task")
    if not task.get("title"):
        errors.append(f"task {tid} missing title")
    return errors


def _check_epic(
    epic: dict, milestone: str, epics: set[str], tasks: set[str]
) -> list[str]:
    """Return id, title, and task errors for one epic."""
    eid = str(epic.get("id") or "")
    if not eid:
        return [f"epic missing id in milestone {milestone}"]
    errors = _remember(eid, epics, tasks, "epic")
    if not epic.get("title"):
        errors.append(f"epic {eid} missing title")
    for task in epic.get("tasks") or []:
        errors.extend(_check_task(task, eid, epics, tasks))
    return errors


def collect_ids(data: dict) -> list[str]:
    """Return manifest id errors. An empty list means the ids are unique."""
    errors: list[str] = []
    epics: set[str] = set()
    tasks: set[str] = set()
    for milestone in data.get("milestones") or []:
        name = str(milestone.get("name") or "?")
        for epic in milestone.get("epics") or []:
            errors.extend(_check_epic(epic, name, epics, tasks))
    return errors


def main() -> int:
    """Return 0 when the manifest ids are present and unique."""
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
