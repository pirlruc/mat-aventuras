# Godot 4 plugin (MAT-003)

Mat Aventuras adopts **Godot 4** (not Unity) as the reward engine. The Compose
host still never holds a Godot view: kart and runner Activities run in
`:engine3d` and `:engine2d` so `finish()` kills the native heap.

## Why Godot 4, not Unity

Both engines run on Android. For this app the constraint is **RAM + privacy +
cold start on mid-tier tablets**, not cinematic tooling.

| | Godot 4 | Unity-as-a-library |
| --- | --- | --- |
| License | MIT (`org.godotengine:godot`) | Proprietary editor + runtime |
| Install | Maven Central AAR | Unity Hub export / custom gradle |
| Typical isolated-process heap | Smaller (tens–low hundreds of MB) | Often 200–400 MB |
| Analytics / cloud | None in the stock library | Easy to pull in services |
| `INTERNET` | Strip at manifest merge | Harder to guarantee gone |
| Mid-tier tablets | `gl_compatibility` + ETC2 | Heavier player |

Godot 4.7.1 is pinned in `gradle/libs.versions.toml`. Rendering uses
**gl_compatibility** (OpenGL ES) rather than Vulkan so mid-tier tablets stay
reliable.

## Process contract (unchanged)

1. Stock Godot Android library (`implementation(libs.godot)`).
2. Thin Activities at the contract names:
   - `pt.mataventuras.plugin.KartPluginActivity` → `:engine3d`
   - `pt.mataventuras.plugin.RunnerPluginActivity` → `:engine2d`
3. Intent extras `mascot` and `name`; result extra `finished`.
4. No Room in those processes (`MatAventurasApp` skips `AppContainer`).
5. Merged manifest has **no** `INTERNET` / network permissions (`tools:node="remove"`).
6. `allowBackup` is false. Godot's `FileProvider` and `ProcessPhoenix` are
   `tools:node="remove"` so the reward APK cannot grant URIs or spawn `:phoenix`.
7. Native GLES fallback pauses `GLSurfaceView` with the Activity. The APK
   ships `armeabi-v7a`, `arm64-v8a`, and `x86_64` (no 32-bit x86).

The Compose host starts those Activities with `StartActivityForResult`. When
they `finish()`, Android kills the isolated process.

Native Canvas/GLES engines remain as:

- the **Robolectric fallback** (Godot native `.so` cannot load in unit tests)
- the **playable fallback** if a future build drops the plugin classes

Both native fallback Activities declare `:engine2d` / `:engine3d` so a device
fallback still kills the engine heap. Robolectric ignores `android:process`.

## What this repo ships

| Piece | Role |
| --- | --- |
| `org.godotengine:godot:4.7.1.stable` | Official Android library |
| `GodotRuntime` | Embed on device; skip on Robolectric |
| `GodotRewardBinder` | Godot fragment vs native host |
| `KartPluginActivity` / `RunnerPluginActivity` | Contract Activities |
| `assets/project.godot` + kart/runner scenes | Packaged Godot project |
| `MatAventuras` singleton | GDScript ↔ extras / `completeReward` |
| `Kart3dEngine` / `Platformer2dEngine` | Domain simulation + native fallback |

Default APK: age 3 → `RunnerPluginActivity` in `:engine2d`, age 7 →
`KartPluginActivity` in `:engine3d`. On a real device that is Godot. Under
Robolectric the same Activities attach native Canvas/GLES instead of
`GodotFragment`.

## Input map (kart)

Normalised X in `[0, 1]` (`EngineInputMap`, copied in `kart.gd`):

- `< 0.34` steer left
- `0.34 … 0.66` boost (centre tap)
- `> 0.66` steer right

2D runner: any tap jumps.

## Godot project

Files live in `app/src/main/assets/` (no hidden `.godot` directory;
`use_hidden_project_data_directory=false`). `run/main_scene` is a tiny
inlined `boot.tscn` that calls `change_scene_to_file` with
`MatAventuras.rewardScene()`. Command line:

`--rendering-method gl_compatibility --rendering-driver opengl3 --scene res://kart.tscn`
(or `res://runner.tscn`). Do not pass `--path`: Godot 4.6+ Android loads
`project.godot` from APK assets and ignores path overrides, which otherwise
yields a blank screen.

The Godot boot splash image is disabled so the default robot icon cannot
loop. `config/features` is `GL Compatibility` to match GLES. When the
engine asks to restart after first-time renderer setup, the fragment
recreates the isolated Activity once (`ProcessPhoenix` stays stripped).

GDScript talks to Android:

```
Engine.get_singleton("MatAventuras").completeReward(true)
```

HUD copy is pt-PT (`Volta`, `Anéis`, `Impulso`).

Emulator instrumented coverage of Godot init remains MAT-002-T1.
