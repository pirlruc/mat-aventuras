# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- Android Gradle Plugin 9.5.0, Kotlin 2.4.21, Gradle 9.8.2, and Godot 4.7.2.
  The devcontainer stays on Temurin 17.
- The install notes name 0.2.0, the dependency bill, vibration, a
  Portuguese voice, and how to clear a forgotten PIN.

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
