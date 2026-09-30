"""License scan, companion pins, and KDoc."""

from __future__ import annotations

from pathlib import Path

import pytest

ROOT = Path(__file__).resolve().parents[1]


def test_licenses_pass(scripts) -> None:
    """The version catalog has no denied license."""
    assert scripts["verify-licenses.py"].main() == 0


def test_licenses_denied_and_allow(scripts, monkeypatch: pytest.MonkeyPatch) -> None:
    """A denied id and an allow-list miss both fail."""
    module = scripts["verify-licenses.py"]
    first = module.modules()[0]
    monkeypatch.setitem(module.KNOWN, first, "GPL-3.0")
    assert module.main() == 1
    monkeypatch.setitem(module.KNOWN, first, "Apache-2.0")
    monkeypatch.setattr(module, "ALLOW", {"MIT"})
    assert module.main() == 1


def test_license_group_name(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """group/name catalog rows become coordinates."""
    module = scripts["verify-licenses.py"]
    catalog = tmp_path / "libs.versions.toml"
    catalog.write_text(
        'mod = { module = "com.example:other" }\n'
        'lib = { group = "com.example", name = "lib" }\n',
        encoding="utf-8",
    )
    monkeypatch.setattr(module, "CATALOG", catalog)
    found = module.modules()
    assert "com.example:lib" in found
    assert "com.example:other" in found


def test_pins_mismatch(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """A missing field and a gitlink mismatch fail."""
    module = scripts["verify-companion-pins.py"]
    pins = tmp_path / "pins.yml"
    pins.write_text(
        "guardrails:\n  path: docs/guardrails\n  sha: abc\nscaffold: {}\n",
        encoding="utf-8",
    )
    monkeypatch.setattr(module, "PINS", pins)
    monkeypatch.setattr(module, "gitlink_sha", lambda path: "abc")
    assert module.main() == 1


def test_pins_match_and_missing(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """Matching pins pass. A missing file fails. The real gitlink resolves."""
    module = scripts["verify-companion-pins.py"]
    original = module.gitlink_sha
    pins = tmp_path / "pins.yml"
    pins.write_text(
        "guardrails:\n  path: docs/guardrails\n  sha: abc\n  tag: '1'\n"
        "scaffold:\n  path: .github/scaffold\n  sha: def\n  tag: '2'\n",
        encoding="utf-8",
    )
    monkeypatch.setattr(module, "PINS", pins)

    def gitlink(path: str) -> str:
        """Return the sha recorded for this test's two companions."""
        return "abc" if "guardrails" in path else "def"

    monkeypatch.setattr(module, "gitlink_sha", gitlink)
    assert module.main() == 0
    monkeypatch.setattr(module, "PINS", tmp_path / "missing.yml")
    assert module.main() == 1
    assert original("docs/guardrails").startswith("aa5184ce")
    with pytest.raises(SystemExit):
        original("no/such/gitlink")


def test_kdoc_missing_and_present(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """A type outside the KDoc window fails. A documented type passes."""
    module = scripts["verify-kdoc.py"]
    source = tmp_path / "domain" / "src" / "main" / "kotlin"
    source.mkdir(parents=True)
    (source / "Sample.kt").write_text(
        "/** Documented. */\nclass Ok\n"
        + ("\n" * 30)
        + "class Missing\n  class Nested\n",
        encoding="utf-8",
    )
    monkeypatch.setattr(module, "ROOT", tmp_path)
    monkeypatch.setattr(module, "SRC_GLOBS", ("domain/src/main/kotlin/**/*.kt",))
    total, missing = module.scan_file(source / "Sample.kt")
    assert total == 2
    assert any(item.endswith(":Missing") for item in missing)
    hidden = "class Foo private constructor"
    assert module.is_public_decl(hidden, module.DECL.match(hidden)) is False
    assert module.main() == 1
    (source / "Sample.kt").write_text(
        "/** Documented. */\nclass Ok\n", encoding="utf-8"
    )
    assert module.main() == 0
