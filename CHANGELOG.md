# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.5.0] - 2026-11-12

### Fixed

- A finished Compose prize sets `RESULT_FINISHED`, so the lesson counts it as won.
- Leaving a prize without winning lets the next prompt speak. The following prize waits again.
- Clock, memory, and word-search rounds that repeat a prompt speak again, and their boards start fresh.
- Speech asked for before the voice engine is ready is spoken when it starts. The prize introduction releases that voice when the prize closes, and the game waits until Jogar.
- The kart steer buttons own the wheel. A runner enemy hit freezes the run the same way a pit does. Runner and climb name those buttons.
- Memory cards show the face only after the flip passes halfway, so the glyph is not mirrored.
- Release builds keep `prizeLimitSeconds()`.

### Changed

- Asset directories use the current Android Gradle source-set API. Gradle tooling runs in parallel and caches task output.
- The project license is GPL-3.0-only. NOTICE reserves commercial text-and-data-mining under Directive (EU) 2019/790 Article 4(3).
- Room exports its schema with the AndroidX Room plugin. Companion pins are guardrails 1.15.0, scaffold 1.12.0, commondevops v6.2.0, androiddevops 1.7.0, and pydevops 3.5.0.
- The Compose runner brief is the number gate. Native kart and runner hosts use on-screen buttons inside the safe drawing area. Parent and leaderboard top bars stay outside the scrolling column. Compose prize buttons are at least 64 dp.

## [0.4.0] - 2026-11-04

### Fixed

- Memory finishes when every pair is open. Cards flip, the back is a
  patterned star, and quantities are dice pips.
- Shape glyphs and Continuar contrast with the button behind them.
- A prize stops speech. The next prompt waits until Continuar.
- Sair, Voltar, and Fechar sit in the top safe area. The name and PIN
  fields move above the keyboard.
- Kotlin warnings fail the build in every module.
- Voltar on a prize returns to the lesson. A life loss stays still until
  the next touch. Kart no longer paints the translucent steer bands.
- Climb, runner, chomp, invaders, and kart leave the boot overlay on
  `mat_api36`.

### Added

- A parent chooses a prize every 5, 10, or 15 finished activities. The
  default is 10. A miss does not reset the count.
- Each prize states its goal and its controls, then starts on Jogar.
- Local device flows run in the android-maestro image. CI, the
  emulator, and release signing call androiddevops 1.4.0.

## [0.3.2] - 2026-11-03

### Changed

- CI, the emulator, and release signing call androiddevops 1.2.0.
- The license gate reads the CycloneDX bill when Gradle has generated it.

### Fixed

- A release build no longer tags which option is the answer.
- The Compose prize timer keeps the same deadline after a rotation.
- The parent area asks a two-digit multiplication before the first PIN
  and before a Keystore reset. A reboot does not extend the PIN lockout
  past the clock time.
- Size and colour options are announced as "Opção n".

## [0.3.1] - 2026-11-02

### Fixed

- Prize games leave the blue loading screen and show the game. The
  platform, letter climb, maze, invaders, and kart race all start.

## [0.3.0] - 2026-11-02

### Added

- Ages 3–5: Cores e formas, Ligar os pontos, Palmas, and Sombras.
- Ages 6–8: Frações, Até 100, Problemas, Tangram, and Sílabas.
- Ages 3–5 prize games: fishing, colouring, and popping bubbles in order.
- Ages 6–8 prize games: letter breakout, a tower that asks for a sum, and
  a short run with two number gates.

### Fixed

- A prize stops on the time that has passed, so changing the tablet clock
  does not end it early or keep it open.
- Ages 6–8 can open the kart race again.
- The clock lesson shows the time on a face instead of saying the hour.
- If the device key for the parent PIN fails, a parent can reset it by
  holding the screen and answering a sum. The parent screen stays out of
  screenshots.

### Changed

- Android Gradle Plugin 9.5.0, Kotlin 2.4.21, Gradle 9.8.2, and Godot 4.7.2.
  The devcontainer stays on Temurin 17.
- A release attaches the dependency bill and signs the release files.

## [0.2.0] - 2026-11-01

### Fixed

- Ages 3–5 in 0.1.0 were given number ranges and sums meant for older
  children. This release keeps that band on small numbers, sums up to 10,
  and short logic steps. Ages 6–8 keep the wider ranges.
- A memory round stays open until every pair is found. Ages 3–5 letters
  use a 2×3 board.
- Word completion has its own board. Lesson cells stay at least 48 dp,
  and 64 dp for ages 3–5.
- The parent dashboard names lessons in Portuguese.

### Added

- Ages 3–5: Qual é diferente, Grande e pequeno, and Padrões. Ages 6–8:
  Que horas são and Euros e cêntimos.
- Two prize games drawn in the app: numbered balloons to tap (ages 3–5)
  and a snake that grows when the sum is right (ages 6–8).
- A speaker replay button, three stars toward the next prize, and a
  parent-set prize time limit of 1, 2, or 3 minutes (the default is 2).
- Leaving a lesson asks "Sim, sair" on a second tap, for both bands. A
  miss for ages 3–5 keeps the points and shows the mascot in amber.
- A loading picture while a prize starts. If it does not start, the child
  can try again or go back. A win shows "Ganhaste!". Ages 3–5 see hearts.
  The kart has an Impulso button.

### Changed

- The parent PIN record is sealed with a key that stays on the device.
  New hashes use 600000 iterations, and an older hash is replaced when
  the correct PIN unlocks.
- Install the signed `app-release.apk` from GitHub Releases and check
  `app-release.apk.sha256` before you open it.
- The security policy links to private vulnerability reports.
- The architecture note maps the controls the app claims to OWASP MASVS.

## [0.1.0] - 2026-10-30

### Changed

- Add memory (pairs of numbers, letters, or icons) and word completion.
  Memory and letters are on both age bands. Words are on 6–8 only.
- Replace the fixed ages with bands of 3–5 and 6–8 years. Modules can belong
  to both bands. Existing profiles migrate without deleting rows.
- The name field accepts 24 characters.
- Profiles, scores, and the parent PIN stay on the device. The app has no
  internet permission.

### Fixed

- Type Godot pointer samples so a Dictionary value cannot drop a prize script.
- Default the boot scene to the 2D runner when the engine plugin is absent.
- Give every Godot prize a Sair control that settles the reward as not won.
- Keep the GLES restart flag on the Activity instead of the process.
- Cap the child-name and mascot extras read by the engine.
