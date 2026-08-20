# libs/

Godot 4 is a Maven dependency (`org.godotengine:godot` in
`gradle/libs.versions.toml`). Do not drop a second engine AAR here — duplicate
`pt.mataventuras.plugin.*` classes would fail DEX merge.

See [docs/engine-plugin.md](../docs/engine-plugin.md).
