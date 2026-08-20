#!/usr/bin/env python3
"""Fail closed when the host manifest would leak child data or engine heaps.

Kids' profiles and the parental PIN hash must not be ADB-backupable. Reward
Activities must stay in :engine2d / :engine3d. Godot's merged FileProvider and
ProcessPhoenix must be stripped so the APK cannot share URIs or restart into
an unexpected process.
"""
from __future__ import annotations

import sys
import xml.etree.ElementTree as ET
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
    return (element.get(f"{ns}{name}") or "").strip()


def fqcn(name: str) -> str:
    if name.startswith("."):
        return PACKAGE + name
    return name


def fail(message: str) -> int:
    print(f"error: {message}", file=sys.stderr)
    return 1


def main() -> int:
    if not MANIFEST.is_file():
        return fail(f"missing {MANIFEST}")
    tree = ET.parse(MANIFEST)
    root = tree.getroot()
    errors: list[str] = []

    for uses in root.findall("uses-permission"):
        name = attr(uses, "name")
        node = attr(uses, "node", TOOLS)
        if name == "android.permission.INTERNET" and node != "remove":
            errors.append("INTERNET must be tools:node=remove")

    application = root.find("application")
    if application is None:
        return fail("missing <application>")
    if attr(application, "allowBackup") != "false":
        errors.append('android:allowBackup must be "false"')
    if attr(application, "fullBackupContent") != "false":
        errors.append('android:fullBackupContent must be "false"')
    if "@xml/data_extraction_rules" not in attr(application, "dataExtractionRules"):
        errors.append("android:dataExtractionRules must point at data_extraction_rules")
    if attr(application, "usesCleartextTraffic") != "false":
        errors.append('android:usesCleartextTraffic must be "false"')

    names: dict[str, ET.Element] = {}
    for child in list(application):
        name = fqcn(attr(child, "name"))
        if name:
            names[name] = child

    for class_name, process in PLUGIN_PROCESSES.items():
        element = names.get(class_name)
        if element is None:
            errors.append(f"missing {class_name}")
            continue
        if attr(element, "exported") != "false":
            errors.append(f"{class_name} must be android:exported=false")
        if attr(element, "process") != process:
            errors.append(f"{class_name} must use android:process={process}")

    for stripped in (
        "org.godotengine.godot.utils.ProcessPhoenix",
        "androidx.core.content.FileProvider",
        "androidx.profileinstaller.ProfileInstallReceiver",
    ):
        element = names.get(stripped)
        if element is None:
            errors.append(f"missing tools:node=remove stub for {stripped}")
            continue
        if attr(element, "node", TOOLS) != "remove":
            errors.append(f"{stripped} must be tools:node=remove")

    if errors:
        for item in errors:
            print(f"error: {item}", file=sys.stderr)
        return 1
    print("privacy manifest: allowBackup=false, engines isolated, Godot merge stubs stripped")
    return 0


if __name__ == "__main__":
    sys.exit(main())
