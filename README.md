# Mat Aventuras

A math game for children aged **3** and **7**. Everything the child sees and
hears is **Portuguese from Portugal**. Nothing leaves the tablet: there is
no account, no advert, and no internet permission.

![Ícone de Mat Aventuras](docs/branding/app-icon.png)

## Who it is for

| Age | What they practise | Prize game |
| --- | --- | --- |
| **3 years** | Counting 1–10, shapes, digits 0–9 | Side-scroller: run, jump, collect coins |
| **7 years** | Addition, subtraction, multiplication, logic | 2.5D dirt race: steer, boost, pass gates |

A parent or teacher picks the age band once. Each child gets a name, a mascot
friend, and a local score.

## How to play

1. Open the app. Choose **Três anos** or **Sete anos**, or **Continuar como**
   the last child on this tablet.
2. Type the child's name and pick a mascot.
3. Home is a **grid of modules**, not a numbered campaign. Tap a module to
   start a lesson.
4. Each screen is one short puzzle:
   - four answer buttons
   - a mini-sudoku with one empty cell
   - sopa de letras — slide a finger along **every** hidden word (across,
     down, or diagonal, either way)
   - a missing puzzle piece
   - a symbol code (the voice names every symbol)
5. A green tick or red cross stays on screen for about a second so the child
   can see it. After **three correct answers in a row**, a prize game opens
   in landscape.
6. **Age 3 prize:** drag a finger forward to run and leap, drag back to
   reverse. Collect the coins. Falling in a hole puts the runner back on the
   last safe ground.
7. **Age 7 prize:** touch the **left** or **right** third of the screen to
   steer, and the **middle** for a short boost. Finish three laps. Gates
   along the dirt add to the HUD; missing one does not end the race.

Leave a lesson from the on-screen exit control. Age 7 asks for confirmation.

## For parents

Open **Painel dos pais** and set a four-digit PIN the first time. Later visits
ask for that PIN. The app stores only a hash, never the digits.

The dashboard shows time on task and modules that need more practice.
**Classificação** and **Recompensas** (badges) are local to this device.

Profiles, the PIN, and the leaderboard stay in on-device storage. Android
backup and device-to-device transfer are turned off so they cannot copy to
another tablet.

## Install from this repository

You need **JDK 17+** and an **Android SDK**. Then:

```bash
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
./gradlew :app:assembleDebug
```

Install `app/build/outputs/apk/debug/app-debug.apk` on the tablet
(USB debugging or `adb install`). The app is not published on a store from
this repository.

Without an SDK you can still run the math and engine tests:

```bash
./gradlew :domain:test :domain:koverVerify :domain:detekt
```

## Language and privacy

- **UI, TTS, and dialogue:** Portuguese from Portugal (pt-PT).
- **Code, comments, and technical docs:** English.
- **Network:** the merged manifest strips `INTERNET` and related permissions.

## For contributors

Architecture, engine isolation, and the Godot plugin contract:

- [docs/architecture.md](docs/architecture.md)
- [docs/engine-plugin.md](docs/engine-plugin.md)
- Decision log: Epics in [docs/issues.yml](docs/issues.yml) (no ADR files)
- Agent notes: [docs/ai-agent-handoff.md](docs/ai-agent-handoff.md)

Process: [github-issue-adr](https://github.com/pirlruc/methodologies/tree/1.2.0/github-issue-adr) @ `1.2.0`.
Guardrails: [pirlruc/guardrails](https://github.com/pirlruc/guardrails) @ `1.3.0`.
Scaffold: [pirlruc/github-scaffold](https://github.com/pirlruc/github-scaffold) @ `1.2.0`.

Full CI with the SDK:

```bash
bash scripts/ci-local.sh
```

## License

Apache License 2.0.
