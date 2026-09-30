"""Load hyphenated CI scripts for the unit tests."""

from __future__ import annotations

import importlib.util
import sys
from pathlib import Path

import pytest

ROOT = Path(__file__).resolve().parents[1]
SCRIPTS = ROOT / "scripts"


def load(filename: str):
    """Load one script from scripts/ by filename."""
    path = SCRIPTS / filename
    name = "ci_" + filename.replace("-", "_").replace(".py", "")
    spec = importlib.util.spec_from_file_location(name, path)
    assert spec is not None and spec.loader is not None
    module = importlib.util.module_from_spec(spec)
    sys.modules[name] = module
    spec.loader.exec_module(module)
    return module


@pytest.fixture(scope="module")
def scripts():
    """Load the CI helpers once per test module."""
    sys.path.insert(0, str(SCRIPTS))
    names = (
        "validate-yaml.py",
        "validate-issues.py",
        "verify-licenses.py",
        "verify-companion-pins.py",
        "verify-kdoc.py",
        "verify-privacy-manifest.py",
        "verify-coverage.py",
        "fail-on-sarif-severity.py",
        "gradle-to-cyclonedx.py",
        "lint-doc-links.py",
    )
    return {name: load(name) for name in names}
