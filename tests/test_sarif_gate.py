"""SARIF file scanning and the fail-closed CLI."""

from __future__ import annotations

import json
import subprocess
import sys
from pathlib import Path

import pytest

ROOT = Path(__file__).resolve().parents[1]


def _write(path: Path, payload: dict) -> None:
    """Write one SARIF document."""
    path.write_text(json.dumps(payload), encoding="utf-8")


def test_blocking_findings(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """A critical result fails both the critical and the medium minimums."""
    module = scripts["fail-on-sarif-severity.py"]
    sarif: dict = {
        "runs": [
            {
                "tool": {
                    "driver": {
                        "rules": [
                            {"id": "R", "properties": {"security-severity": "9.1"}}
                        ]
                    }
                },
                "results": [
                    {
                        "ruleId": "R",
                        "level": "error",
                        "message": {"text": "boom"},
                        "locations": [
                            {
                                "physicalLocation": {
                                    "artifactLocation": {"uri": "App.kt"},
                                    "region": {"startLine": 3},
                                }
                            }
                        ],
                    },
                    {"ruleId": "other", "level": "warning", "message": {"text": "meh"}},
                ],
            }
        ]
    }
    path = tmp_path / "out.sarif"
    _write(path, sarif)
    rules = module.rule_map(sarif)
    assert module.result_level(sarif["runs"][0]["results"][0], rules) == "critical"
    monkeypatch.setattr(
        sys, "argv", ["sarif.py", str(path), "--min-severity", "critical"]
    )
    assert module.main() == 1
    monkeypatch.setattr(
        sys, "argv", ["sarif.py", str(path), "--min-severity", "medium"]
    )
    assert module.main() == 1


def test_invalid_and_missing(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """Bad JSON, a missing path, and an empty directory fail closed."""
    module = scripts["fail-on-sarif-severity.py"]
    path = tmp_path / "bad.sarif"
    path.write_text("{", encoding="utf-8")
    monkeypatch.setattr(sys, "argv", ["sarif.py", str(path)])
    assert module.main() == 2
    monkeypatch.setattr(sys, "argv", ["sarif.py", str(tmp_path / "missing.sarif")])
    with pytest.raises(SystemExit) as raised:
        module.main()
    assert raised.value.code == 2
    empty = tmp_path / "empty-dir"
    empty.mkdir()
    monkeypatch.setattr(sys, "argv", ["sarif.py", str(empty)])
    assert module.main() == 2


def test_medium_overflow(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """More than forty medium findings print a remainder line and still pass."""
    module = scripts["fail-on-sarif-severity.py"]
    results = [{"level": "warning", "message": {"text": "m"}} for _ in range(41)]
    path = tmp_path / "medium.sarif"
    _write(path, {"runs": [{"results": results}]})
    monkeypatch.setattr(sys, "argv", ["sarif.py", str(path)])
    assert module.main() == 0


def test_clean_cli(tmp_path: Path) -> None:
    """The script entry point accepts a SARIF file with no results."""
    path = tmp_path / "clean.sarif"
    path.write_text('{"runs": []}\n', encoding="utf-8")
    completed = subprocess.run(
        [sys.executable, "scripts/fail-on-sarif-severity.py", str(path)],
        cwd=ROOT,
        capture_output=True,
        text=True,
        check=False,
    )
    assert completed.returncode == 0
