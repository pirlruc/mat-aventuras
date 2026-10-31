# Mat Aventuras

A game for children aged **3 to 5** and **6 to 8**. Counting, shapes, numbers,
early arithmetic, memory, letters, and a few prize games. Everything the child
sees and hears is **Portuguese from Portugal**. Profiles, scores, and the
parent PIN stay on the device.

![Ícone de Mat Aventuras](docs/branding/app-icon.png)

## Age bands

A parent picks one band. A lesson can belong to both.

| Band | Lessons | Prize games |
| --- | --- | --- |
| 3 a 5 anos | Contar, formas, números, somar, lógica, memória, letras | Plataforma, subir letras, labirinto |
| 6 a 8 anos | Números, somar, subtrair, multiplicar, dividir, lógica, memória, letras, palavras | Those, plus invasores and the kart race |

## Games

| Game | What the child does | Bands |
| --- | --- | --- |
| Contar | Count stars from 1 to 10 | 3–5 |
| Formas | Match a shape | 3–5 |
| Números | Digits, and a small sudoku | 3–5 and 6–8 |
| Somar | Add | 3–5 and 6–8 |
| Subtrair | Subtract | 6–8 |
| Multiplicar | Times tables | 6–8 |
| Dividir | Exact division | 6–8 |
| Lógica | Finish a sequence | 3–5 and 6–8 |
| Memória | Find pairs of numbers, letters, or icons | 3–5 and 6–8 |
| Letras | Match an upper-case letter to its lower-case pair | 3–5 and 6–8 |
| Palavras | Fill the missing letter of a Portuguese word | 6–8 |
| Plataforma | Run, jump, and collect coins | 3–5 and 6–8 |
| Subir letras | Climb letter floors | 3–5 and 6–8 |
| Labirinto | Eat the dots | 3–5 and 6–8 |
| Invasores | Clear the fleet | 6–8 |
| Corrida | Steer a kart through three laps | 6–8 |

Three correct answers in a row open a prize in landscape. Leave a lesson from
the on-screen exit control.

## Install

Install the debug APK from the GitHub Release tagged
[0.1.0](https://github.com/pirlruc/mat-aventuras/releases/tag/0.1.0).
Pin that tag. The release page lists the SHA-256 of `app-debug.apk`.

The app is not on a store.

## Privacy and permissions

- No account, no advert, and no `INTERNET` permission.
- The parent PIN is stored as a hash, never as the four digits.
- Android backup and device-to-device transfer are off, so a profile is not
  copied to another tablet.

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
