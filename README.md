# Mat Aventuras

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
| 3 a 5 anos | Contar, formas, números, somar, lógica, memória, letras | Plataforma, subir letras, labirinto |
| 6 a 8 anos | Números, somar, subtrair, multiplicar, dividir, lógica, memória, letras, palavras | Those, plus invasores and the kart race |

## Install

Use the latest release on
[GitHub Releases](https://github.com/pirlruc/mat-aventuras/releases/latest).
Download `app-debug.apk` and the matching `app-debug.apk.sha256`.

Check the file before you install it:

```bash
sha256sum -c app-debug.apk.sha256
```

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

## For contributors

- [Architecture](docs/architecture.md)
- [Decision log](https://github.com/pirlruc/mat-aventuras/issues?q=label%3Aepic)

## License

Apache License 2.0.
