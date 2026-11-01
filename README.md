# Mat Aventuras

[![OpenSSF Baseline](https://www.bestpractices.dev/projects/16162/baseline)](https://www.bestpractices.dev/projects/16162)

A game for children aged **3 to 5** and **6 to 8**. Counting, shapes, numbers,
early arithmetic, memory, letters, and a few prize games. Everything the child
sees and hears is **Portuguese from Portugal**. Profiles, scores, and the
parent PIN stay on the phone or tablet.

The app needs **Android 8.0 or newer**.

![Ícone de Mat Aventuras](docs/branding/app-icon.png)

## Age bands

A parent, or the child with a parent nearby, picks one band. Some lessons
belong to both.

| Band | Lessons | Prize games |
| --- | --- | --- |
| 3 a 5 anos | Contar, formas, números, somar, lógica, memória, letras, Qual é diferente, Grande e pequeno, Padrões | Plataforma, subir letras, labirinto, balões |
| 6 a 8 anos | Números, somar, subtrair, multiplicar, dividir, lógica, memória, letras, palavras, Que horas são, Euros e cêntimos | Those, plus invasores, the kart race, and the answer snake |

Version 0.1.0 gave ages 3–5 number ranges meant for older children. 0.2.0
keeps that band on small numbers and sums up to 10.

## Install

Use the latest release on
[GitHub Releases](https://github.com/pirlruc/mat-aventuras/releases/latest).
Download `app-release.apk` and the matching `app-release.apk.sha256`.

Check the file before you install it. `apksigner` is in the Android SDK
build-tools.

```bash
sha256sum -c app-release.apk.sha256
apksigner verify --print-certs app-release.apk
```

`sha256sum` checks that the APK matches the published checksum.
`apksigner` checks the signature and prints the signer certificate.
The SHA-256 fingerprint of that certificate is not listed here yet.

The app is not on a store. Android will ask you to allow an install from
your browser or the Files app:

1. Download the APK.
2. Open **Settings → Apps → Special app access → Install unknown apps**.
3. Choose the app you used to download the file (Chrome, or Files) and turn
   **Allow from this source** on.
4. Open the APK and tap **Install**.

You can turn that permission off again after the install.

## Parent area

The home screen has **Painel dos pais**. The first visit asks you to choose
a PIN of four numbers. Later visits ask for that PIN. Five wrong attempts
pause the check for a minute.

The parent area shows sessions, points, and badges for the children on this
device. It does not send them anywhere.

## Privacy

- No account, no advert, and no internet permission.
- The parent PIN is stored as a hash, never as the four digits.
- Android backup and device-to-device transfer are off, so a profile is not
  copied to another tablet.

## Screens

The first screen asks for an age band, a name, and a mascot.

![Ecrã de entrada](docs/branding/screen-entry.png)

The home screen lists the lessons for that band.

![Ecrã inicial](docs/branding/screen-home.png)

## Build from source

You need JDK 17 or newer and an Android SDK.

```bash
echo "sdk.dir=/path/to/Android/Sdk" > local.properties
./gradlew :app:assembleDebug
```

The APK is `app/build/outputs/apk/debug/app-debug.apk`.

## Dependencies

Direct libraries are pinned in `gradle/libs.versions.toml`. Gradle resolves
them at build time. Dependabot opens update pull requests. Security updates
are not delayed. Other updates wait 7 days. `dependency-review` and
`supply-chain` must pass before a change is merged.

## For contributors

Open a [GitHub issue](https://github.com/pirlruc/mat-aventuras/issues) for a
bug or an idea. Propose a code change as a pull request. The app, comments,
and docs are in English. What the child sees and hears stays Portuguese from
Portugal.

- [Architecture](docs/architecture.md)
- [Maintainers](docs/MAINTAINERS.md)
- [Decision log](https://github.com/pirlruc/mat-aventuras/issues?q=label%3Aepic)

## License

Apache License 2.0.
