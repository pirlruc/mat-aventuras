"""Privacy manifest and Kover threshold checks."""

from __future__ import annotations

from pathlib import Path

import pytest

ROOT = Path(__file__).resolve().parents[1]


def test_privacy_missing_and_real(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """A missing manifest fails and the real host manifest passes."""
    module = scripts["verify-privacy-manifest.py"]
    monkeypatch.setattr(module, "MANIFEST", tmp_path / "missing.xml")
    assert module.main() == 1
    monkeypatch.setattr(module, "MANIFEST", ROOT / "app/src/main/AndroidManifest.xml")
    assert module.main() == 0


def test_privacy_internet_and_empty(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """A live INTERNET permission and an empty manifest fail."""
    module = scripts["verify-privacy-manifest.py"]
    real = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
    broken = tmp_path / "net.xml"
    broken.write_text(
        real.replace(
            'android:name="android.permission.INTERNET" tools:node="remove"',
            'android:name="android.permission.INTERNET"',
        ),
        encoding="utf-8",
    )
    monkeypatch.setattr(module, "MANIFEST", broken)
    assert module.main() == 1
    empty = tmp_path / "empty.xml"
    empty.write_text("<manifest></manifest>", encoding="utf-8")
    monkeypatch.setattr(module, "MANIFEST", empty)
    assert module.main() == 1


def test_privacy_flags(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """Backup and cleartext flags are required to stay off."""
    module = scripts["verify-privacy-manifest.py"]
    text = (ROOT / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
    text = text.replace('android:allowBackup="false"', 'android:allowBackup="true"')
    text = text.replace(
        'android:fullBackupContent="false"', 'android:fullBackupContent="true"'
    )
    text = text.replace("@xml/data_extraction_rules", "@xml/other")
    text = text.replace(
        'android:usesCleartextTraffic="false"', 'android:usesCleartextTraffic="true"'
    )
    path = tmp_path / "flags.xml"
    path.write_text(text, encoding="utf-8")
    monkeypatch.setattr(module, "MANIFEST", path)
    assert module.main() == 1


def test_privacy_plugins(scripts) -> None:
    """Missing, exported, and wrong-process activities are errors."""
    module = scripts["verify-privacy-manifest.py"]
    element = module.ET.Element("activity")
    element.set(module.ANDROID + "exported", "true")
    element.set(module.ANDROID + "process", ":no")
    errors: list[str] = []
    module.check_plugins({"pt.mataventuras.plugin.KartPluginActivity": element}, errors)
    assert any("exported" in item for item in errors)
    assert any("process" in item for item in errors)
    assert module.fqcn(".Main") == module.PACKAGE + ".Main"
    application = module.ET.Element("application")
    application.append(module.ET.Element("activity"))
    assert module.index_components(application) == {}
    present = module.ET.Element("activity")
    present.set(module.TOOLS + "node", "merge")
    stripped: list[str] = []
    names = {"org.godotengine.godot.utils.ProcessPhoenix": present}
    module.check_stripped(names, stripped)
    assert any("must be tools:node=remove" in item for item in stripped)
    missing: list[str] = []
    module.check_stripped({}, missing)
    assert missing


def test_kover_report(scripts, tmp_path: Path) -> None:
    """A full report passes and a low report fails."""
    module = scripts["verify-coverage.py"]
    report = tmp_path / "report.xml"
    report.write_text(
        "<report><counter type='LINE' missed='0' covered='100'/>"
        "<counter type='BRANCH' missed='0' covered='100'/></report>",
        encoding="utf-8",
    )
    floors = {"statement_coverage": 95, "branch_coverage": 95}
    assert module.check_report("domain", report, floors)
    low = tmp_path / "low.xml"
    low.write_text(
        "<report><counter type='LINE' missed='50' covered='50'/>"
        "<counter type='BRANCH' missed='50' covered='50'/></report>",
        encoding="utf-8",
    )
    assert not module.check_report("domain", low, floors)
    assert not module.check_report("domain", tmp_path / "nope.xml", floors)


def test_kover_counters(scripts) -> None:
    """Zero and missing counters fail closed."""
    module = scripts["verify-coverage.py"]
    zero = module.ET.fromstring(
        "<report><counter type='LINE' missed='0' covered='0'/></report>"
    )
    with pytest.raises(SystemExit):
        module.counter_percent(zero, "LINE")
    with pytest.raises(SystemExit):
        module.counter_percent(module.ET.fromstring("<report></report>"), "LINE")


def test_sdk_absent(scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch) -> None:
    """Without an SDK only the domain report is required."""
    module = scripts["verify-coverage.py"]
    monkeypatch.setattr(module, "ROOT", tmp_path)
    monkeypatch.delenv("ANDROID_HOME", raising=False)
    monkeypatch.delenv("ANDROID_SDK_ROOT", raising=False)
    assert module.android_sdk_present() is False
    assert [name for name, _path in module.reports()] == ["domain"]
    (tmp_path / "local.properties").write_text("# no sdk\n", encoding="utf-8")
    assert module.android_sdk_present() is False
    (tmp_path / "local.properties").write_text(
        "sdk.dir=/no/such/sdk\n", encoding="utf-8"
    )
    assert module.android_sdk_present() is False


def test_sdk_present_finds_reports(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """A real sdk.dir includes Android Kover reports."""
    module = scripts["verify-coverage.py"]
    monkeypatch.setattr(module, "ROOT", tmp_path)
    monkeypatch.delenv("ANDROID_HOME", raising=False)
    sdk = tmp_path / "sdk"
    sdk.mkdir()
    (tmp_path / "local.properties").write_text(f"sdk.dir={sdk}\n", encoding="utf-8")
    assert module.android_sdk_present() is True
    nested = tmp_path / "data" / "build" / "reports" / "kover" / "custom"
    nested.mkdir(parents=True)
    (nested / "extra.xml").write_text("<report/>", encoding="utf-8")
    debug = tmp_path / "app" / "build" / "reports" / "kover" / "reportDebug.xml"
    debug.parent.mkdir(parents=True)
    debug.write_text("<report/>", encoding="utf-8")
    assert module.find_android_report("app") == debug
    assert module.find_android_report("data").name == "extra.xml"


def test_threshold_files(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """Overlay floors, a non-mapping file, and a missing pair."""
    module = scripts["verify-coverage.py"]
    org = tmp_path / "org.yml"
    overlay = tmp_path / "overlay.yml"
    org.write_text("statement_coverage: 95\nbranch_coverage: 95\n", encoding="utf-8")
    overlay.write_text(
        "statement_coverage: 95\nbranch_coverage: 95\n", encoding="utf-8"
    )
    monkeypatch.setattr(module, "THRESHOLDS_CANDIDATES", (org, overlay))
    assert module.thresholds_path() == overlay
    low = tmp_path / "low.yml"
    low.write_text("statement_coverage: 10\nbranch_coverage: 10\n", encoding="utf-8")
    monkeypatch.setattr(module, "THRESHOLDS_CANDIDATES", (org, low))
    with pytest.raises(SystemExit):
        module.thresholds_path()
    broken = tmp_path / "list.yml"
    broken.write_text("- 1\n", encoding="utf-8")
    with pytest.raises(SystemExit):
        module.load_thresholds(broken)
    with pytest.raises(SystemExit):
        module.load_thresholds(tmp_path / "absent.yml")
    only = tmp_path / "only.yml"
    only.write_text("statement_coverage: 95\nbranch_coverage: 95\n", encoding="utf-8")
    monkeypatch.setattr(
        module, "THRESHOLDS_CANDIDATES", (tmp_path / "missing.yml", only)
    )
    assert module.thresholds_path() == only
    monkeypatch.setattr(
        module, "THRESHOLDS_CANDIDATES", (tmp_path / "a.yml", tmp_path / "b.yml")
    )
    with pytest.raises(SystemExit):
        module.thresholds_path()


def test_coverage_main(
    scripts, tmp_path: Path, monkeypatch: pytest.MonkeyPatch
) -> None:
    """Main passes a good report and fails a missing one."""
    module = scripts["verify-coverage.py"]
    report = tmp_path / "report.xml"
    report.write_text(
        "<report><counter type='LINE' missed='0' covered='100'/>"
        "<counter type='BRANCH' missed='0' covered='100'/></report>",
        encoding="utf-8",
    )
    floors = tmp_path / "floors.yml"
    floors.write_text("statement_coverage: 95\nbranch_coverage: 95\n", encoding="utf-8")
    monkeypatch.setattr(module, "THRESHOLDS_CANDIDATES", (floors, floors))
    monkeypatch.setattr(module, "reports", lambda: [("domain", report)])
    assert module.main() == 0
    monkeypatch.setattr(
        module, "reports", lambda: [("domain", tmp_path / "missing.xml")]
    )
    assert module.main() == 1
