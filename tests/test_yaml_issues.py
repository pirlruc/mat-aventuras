"""YAML parse and issue-id checks."""

from __future__ import annotations

import sys
from pathlib import Path

import pytest


def test_yaml_ok(scripts, tmp_path: Path) -> None:
    """A present YAML file parses."""
    module = scripts["validate-yaml.py"]
    (tmp_path / "ok.yml").write_text("a: 1\n", encoding="utf-8")
    assert module.main(["--root", str(tmp_path), "ok.yml"]) == 0


def test_yaml_glob(scripts, tmp_path: Path) -> None:
    """A glob that matches is accepted."""
    module = scripts["validate-yaml.py"]
    (tmp_path / "ok.yml").write_text("a: 1\n", encoding="utf-8")
    assert module.main(["--root", str(tmp_path), "--glob", "*.yml"]) == 0


def test_yaml_missing(scripts, tmp_path: Path) -> None:
    """Missing files, empty globs, and bad YAML fail."""
    module = scripts["validate-yaml.py"]
    assert module.main(["--root", str(tmp_path), "missing.yml"]) == 1
    assert module.main(["--root", str(tmp_path), "--glob", "*.nope"]) == 1
    assert module.main(["--root", str(tmp_path)]) == 1
    (tmp_path / "bad.yml").write_text(":\n", encoding="utf-8")
    assert module.main(["--root", str(tmp_path), "bad.yml"]) == 1


def test_issue_id_errors(scripts) -> None:
    """Duplicate, blank, and colliding ids are reported."""
    module = scripts["validate-issues.py"]
    data = {
        "milestones": [
            {
                "name": "Phase",
                "epics": [
                    {
                        "id": "MAT-1",
                        "title": "One",
                        "tasks": [
                            {"id": "MAT-1-T1", "title": "Task"},
                            {"id": "MAT-1-T2"},
                            {"id": ""},
                            {"id": "MAT-1", "title": "collides"},
                        ],
                    },
                    {"id": "MAT-1", "title": "dup"},
                    {"title": "no id"},
                    {"id": "MAT-2"},
                ],
            }
        ]
    }
    errors = module.collect_ids(data)
    assert any("duplicate epic" in item for item in errors)
    assert any("missing id" in item for item in errors)
    assert any("collides" in item for item in errors)
    assert any("missing title" in item for item in errors)


def test_issue_main(scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch) -> None:
    """Main accepts an empty manifest and rejects a broken one."""
    module = scripts["validate-issues.py"]
    ok = tmp_path / "ok.yml"
    ok.write_text("milestones: []\n", encoding="utf-8")
    monkeypatch.setattr(sys, "argv", ["validate-issues.py", str(ok)])
    assert module.main() == 0
    bad = tmp_path / "bad.yml"
    bad.write_text("milestones:\n- epics:\n  - id: A\n", encoding="utf-8")
    monkeypatch.setattr(sys, "argv", ["validate-issues.py", str(bad)])
    assert module.main() == 1
    monkeypatch.setattr(sys, "argv", ["validate-issues.py", str(tmp_path / "nope.yml")])
    assert module.main() == 1
    monkeypatch.setattr(sys, "argv", ["validate-issues.py"])
    assert module.main() == 0
