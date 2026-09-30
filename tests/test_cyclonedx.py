"""Gradle dependency text becomes a CycloneDX bill."""

from __future__ import annotations

import json
import subprocess
import sys
from pathlib import Path

import pytest

ROOT = Path(__file__).resolve().parents[1]


def test_rewritten_coordinate(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """An arrow rewrite replaces the requested version."""
    module = scripts["gradle-to-cyclonedx.py"]
    apk = tmp_path / "app.apk"
    apk.write_bytes(b"apk")
    deps = tmp_path / "deps.txt"
    deps.write_text(
        "+--- com.example:lib:1.0 -> 1.2\n+--- project :domain\n", encoding="utf-8"
    )
    out = tmp_path / "out" / "bom.json"
    monkeypatch.setattr(
        sys, "argv", ["gradle-to-cyclonedx.py", str(apk), str(deps), str(out)]
    )
    assert module.main() == 0
    bom = json.loads(out.read_text(encoding="utf-8"))
    assert bom["components"][0]["version"] == "1.2"


def test_plain_coordinate(scripts) -> None:
    """A line with no arrow keeps the requested version."""
    module = scripts["gradle-to-cyclonedx.py"]
    parsed = module.parse_dependencies("+--- com.example:lib:1.0\n")
    assert parsed == [("com.example", "lib", "1.0")]


def test_usage_and_missing(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """Usage, a missing APK, and a missing dependency report fail."""
    module = scripts["gradle-to-cyclonedx.py"]
    apk = tmp_path / "app.apk"
    apk.write_bytes(b"apk")
    deps = tmp_path / "deps.txt"
    deps.write_text("+--- com.example:lib:1.0\n", encoding="utf-8")
    out = tmp_path / "bom.json"
    monkeypatch.setattr(sys, "argv", ["gradle-to-cyclonedx.py"])
    assert module.main() == 2
    monkeypatch.setattr(
        sys,
        "argv",
        ["gradle-to-cyclonedx.py", str(tmp_path / "no.apk"), str(deps), str(out)],
    )
    assert module.main() == 1
    monkeypatch.setattr(
        sys,
        "argv",
        ["gradle-to-cyclonedx.py", str(apk), str(tmp_path / "no.txt"), str(out)],
    )
    assert module.main() == 1


def test_no_coordinates(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """A report with no Maven coordinates fails."""
    module = scripts["gradle-to-cyclonedx.py"]
    apk = tmp_path / "app.apk"
    apk.write_bytes(b"apk")
    empty = tmp_path / "empty.txt"
    empty.write_text("no coordinates\n", encoding="utf-8")
    monkeypatch.setattr(
        sys,
        "argv",
        ["gradle-to-cyclonedx.py", str(apk), str(empty), str(tmp_path / "bom.json")],
    )
    assert module.main() == 1


def test_cli_entry(tmp_path: Path) -> None:
    """The script entry point writes a bill for one coordinate."""
    apk = tmp_path / "app.apk"
    apk.write_bytes(b"apk")
    deps = tmp_path / "deps.txt"
    deps.write_text("+--- com.example:lib:1.0\n", encoding="utf-8")
    out = tmp_path / "bom.json"
    completed = subprocess.run(
        [
            sys.executable,
            "scripts/gradle-to-cyclonedx.py",
            str(apk),
            str(deps),
            str(out),
        ],
        cwd=ROOT,
        capture_output=True,
        text=True,
        check=False,
    )
    assert completed.returncode == 0
    assert out.is_file()
