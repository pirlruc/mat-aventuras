#!/usr/bin/env python3
"""Fail closed when the host manifest would leak child data or engine heaps.

Kids' profiles and the parental PIN hash must not be ADB-backupable. Reward
Activities must stay in :engine2d / :engine3d. Godot's merged FileProvider and
ProcessPhoenix must be stripped so the APK cannot grant URIs or reincarnate the
Compose launcher. A GLES restart is a host relaunch (`restart` result extra),
not ProcessPhoenix.
"""

from __future__ import annotations

import sys
import xml.etree.ElementTree as ET  # nosec B405
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"
PACKAGE = "pt.mataventuras.app"
ANDROID = "{http://schemas.android.com/apk/res/android}"
TOOLS = "{http://schemas.android.com/tools}"

PLUGIN_PROCESSES = {
    "pt.mataventuras.plugin.KartPluginActivity": ":engine3d",
    "pt.mataventuras.plugin.RunnerPluginActivity": ":engine2d",
    "pt.mataventuras.app.engine.Kart3dActivity": ":engine3d",
    "pt.mataventuras.app.engine.Platformer2dActivity": ":engine2d",
}


def attr(element: ET.Element, name: str, ns: str = ANDROID) -> str:
    """Return a trimmed Android or tools attribute."""
    return (element.get(f"{ns}{name}") or "").strip()


def fqcn(name: str) -> str:
    """Expand a leading-dot class name with the application package."""
    if name.startswith("."):
        return PACKAGE + name
    return name


def fail(message: str) -> int:
    """Print an error and return the failure status."""
    print(f"error: {message}", file=sys.stderr)
    return 1


def check_internet(root: ET.Element, errors: list[str]) -> None:
    """Require the INTERNET permission to be removed."""
    for uses in root.findall("uses-permission"):
        name = attr(uses, "name")
        node = attr(uses, "node", TOOLS)
        if name == "android.permission.INTERNET" and node != "remove":
            errors.append("INTERNET must be tools:node=remove")


def check_application_flags(application: ET.Element, errors: list[str]) -> None:
    """Require backup and cleartext flags to stay off."""
    if attr(application, "allowBackup") != "false":
        errors.append('android:allowBackup must be "false"')
    if attr(application, "fullBackupContent") != "false":
        errors.append('android:fullBackupContent must be "false"')
    rules = attr(application, "dataExtractionRules")
    if "@xml/data_extraction_rules" not in rules:
        errors.append("android:dataExtractionRules must point at data_extraction_rules")
    if attr(application, "usesCleartextTraffic") != "false":
        errors.append('android:usesCleartextTraffic must be "false"')


def index_components(application: ET.Element) -> dict[str, ET.Element]:
    """Index application children by fully qualified android:name."""
    names: dict[str, ET.Element] = {}
    for child in list(application):
        name = fqcn(attr(child, "name"))
        if name:
            names[name] = child
    return names


def check_plugins(names: dict[str, ET.Element], errors: list[str]) -> None:
    """Require reward activities to be unexported and process-isolated."""
    for class_name, process in PLUGIN_PROCESSES.items():
        element = names.get(class_name)
        if element is None:
            errors.append(f"missing {class_name}")
            continue
        if attr(element, "exported") != "false":
            errors.append(f"{class_name} must be android:exported=false")
        if attr(element, "process") != process:
            errors.append(f"{class_name} must use android:process={process}")


def check_stripped(names: dict[str, ET.Element], errors: list[str]) -> None:
    """Require Godot merge stubs to be tools:node=remove."""
    stripped = (
        "org.godotengine.godot.utils.ProcessPhoenix",
        "androidx.core.content.FileProvider",
        "androidx.profileinstaller.ProfileInstallReceiver",
    )
    for class_name in stripped:
        element = names.get(class_name)
        if element is None:
            errors.append(f"missing tools:node=remove stub for {class_name}")
            continue
        if attr(element, "node", TOOLS) != "remove":
            errors.append(f"{class_name} must be tools:node=remove")


def main() -> int:
    """Return 0 when the host manifest matches the privacy rules."""
    if not MANIFEST.is_file():
        return fail(f"missing {MANIFEST}")
    root = ET.parse(MANIFEST).getroot()  # nosec B314
    application = root.find("application")
    if application is None:
        return fail("missing <application>")
    errors: list[str] = []
    check_internet(root, errors)
    check_application_flags(application, errors)
    names = index_components(application)
    check_plugins(names, errors)
    check_stripped(names, errors)
    if errors:
        for item in errors:
            print(f"error: {item}", file=sys.stderr)
        return 1
    print(
        "privacy manifest: allowBackup=false, engines isolated, "
        "Godot merge stubs stripped"
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
