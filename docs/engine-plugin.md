# Godot / Unity plugin (MAT-003)

Godot and Unity **run on Android**. The problem is not the engine; it is
**which process** owns the native heap.

## Do you need a custom engine library?

No. You do not need a renderer written only for Mat Aventuras.

You need:

1. A **stock** Godot 4 Android `aar` **or** Unity-as-a-library export of the
   reward levels (kart and/or ring runner).
2. A **thin Activity adapter** (this repo’s contract) that:
   - lives in `android:process=":engine3d"` (kart) or `:engine2d` (runner),
   - reads Intent extras `mascot` and `name`,
   - calls `setResult` with extra `finished`,
   - never opens Room, never requests `INTERNET`, never talks to analytics.

The Compose host never holds a `GodotView` / `UnityPlayer`. It starts the
plugin Activity with `StartActivityForResult`. When that Activity `finish()`es,
Android kills the isolated process and the 200–400 MB engine heap goes with it.

Native 2D Canvas stays in the Compose process on purpose (it is small). A
Godot/Unity **2D** plugin must still use `:engine2d` because those runtimes
are not small.

## What this repo already wires

| Piece | Role |
| --- | --- |
| `EnginePluginContract` | Extra keys, process names, plugin class names, forbidden permissions |
| `EnginePluginResolver` | Prefer plugin class if it is on the classpath; else native |
| `EngineLauncher` | Builds the Intent the host fires |
| `IsolatedEngineActivity` | Base class: extras + `completeReward` |
| `EngineInputMap` | Left / centre / right touch bands for the kart |
| `libs/engine-plugin.aar` | Optional drop-in. If the file exists, Gradle compiles it in |

Default APK (no AAR): age 3 → `Platformer2dActivity`, age 7 → `Kart3dActivity`
in `:engine3d`.

With AAR on the classpath providing:

- `pt.mataventuras.plugin.RunnerPluginActivity`
- `pt.mataventuras.plugin.KartPluginActivity`

the host launches those instead. Domain simulation (`Platformer2dEngine`,
`Kart3dEngine`) stays as the fallback and as the source of truth for tests.

## Drop-in steps

1. Export Godot 4 (`gradle_build` / custom Android build) or Unity as a
   library. Strip networking, ads, Game Center, and analytics from the export.
2. Implement the two Activity classes (see `samples/engine-plugin/`).
   Subclass `IsolatedEngineActivity` **or** copy `completeReward` if the
   player Activity must extend `Godot` / `UnityPlayerActivity` instead.
3. In the plugin manifest:

```xml
<activity
    android:name="pt.mataventuras.plugin.KartPluginActivity"
    android:process=":engine3d"
    android:exported="false"
    android:excludeFromRecents="true"
    android:taskAffinity="" />
<activity
    android:name="pt.mataventuras.plugin.RunnerPluginActivity"
    android:process=":engine2d"
    android:exported="false"
    android:excludeFromRecents="true"
    android:taskAffinity="" />
```

4. Merge-time: confirm the **application** manifest still has **no**
   `INTERNET` (or `ACCESS_NETWORK_STATE`). `EnginePluginContract.manifestAllowed`
   is the checklist the host uses in tests.
5. Copy the AAR to `libs/engine-plugin.aar` and assemble. Do not commit the
   binary (`libs/*.aar` is gitignored).

## Input map (kart)

Normalised X in `[0, 1]`:

- `< 0.34` steer left
- `0.34 … 0.66` boost (centre tap)
- `> 0.66` steer right

2D runner: any tap jumps.

## Still later (MAT-003-T3)

- Actual Godot/Unity project and AAR
- Asset pipeline (kart mesh, rings, mascot tint)
- Emulator instrumented test of the swap (MAT-002-T1)
