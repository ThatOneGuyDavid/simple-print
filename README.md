# Simple Print

A small, offline Android app for printing formatted text from a Mudita Kompakt
to an X5H-2646 Bluetooth thermal printer. Type text, choose a printer, and print.
No account, Internet access, advertising, analytics, or Google Play Services.

## Download and install

Download **SimplePrint-v0.10.apk** from the [v0.10 release](https://github.com/ThatOneGuyDavid/simple-print/releases/tag/v0.10).
The release includes a SHA-256 checksum. These are experimental development builds.

1. Pair the printer in the phone's Bluetooth settings.
2. Transfer the APK to the phone and install it through your APK installer or
   Mudita Center. From a computer with ADB: `adb install -r SimplePrint-v0.10.apk`.
3. Grant Nearby Devices/Bluetooth access. Select the printer on the **Printer** tab.
4. Type on the **Print** tab and press **PRINT**. **HIDE KEYS** dismisses the keyboard.

The package is `com.local.simpleprint`. Published updates use the retained
private development signing key. If a differently signed local build is installed,
Android requires uninstalling it first. In a phone/WebADB shell, the command is:

```sh
pm uninstall --user 0 com.local.simpleprint
```

Uninstalling removes local settings. Do not prefix that phone-shell command
with `adb` or with the shell prompt.

## Features

- Small, Medium, and Large text (24, 32, and 42 printer pixels).
- Regular/Bold settings apply to new characters, allowing mixed formatting.
- Optional rectangular border with equal padding around the text block.
- Separate printer-selection tab; remembers the last paired printer.
- Clockwise 90° printing with **manual line breaks only**.
- Maximum darkness level 5/5; reference-profile energy 9500 and speed 10.
- Black-and-white interface and a text launcher icon without an outline.

| Mode | Line breaks | Position across the paper |
| --- | --- | --- |
| Normal, border on | Automatic wrapping plus manual newlines | Bordered block centered |
| Normal, border off | Automatic wrapping plus manual newlines | 8-pixel left margin |
| 90°, border on | Manual newlines only | Rotated bordered block centered |
| 90°, border off | Manual newlines only | Rotated block starts at an 8-pixel left margin |

Text stays left aligned inside its block before rotation. In 90° mode, a long
line runs along the paper; the stack of manual lines must fit across the printhead.
Both modes use the original final feed, with no extra feed for 90°.

## Build

Use JDK 17, Android SDK platform 35 and build-tools 35.0.0. The Gradle wrapper is
included. SDK components and Gradle dependencies require downloads on first use.

```sh
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`. Android Studio can also open
the project. Gradle uses your local Android debug key; no maintainer key is included.
This Gradle path is provided but was not run during this release preparation.

The dependency-light SDK build used and checked for the distributed APK is:

```sh
ANDROID_SDK_ROOT=/path/to/android-sdk bash tools/build-sdk.sh
```

It requires Bash, Java, `javac`, `keytool`, and `zip`. If only a JRE is installed,
set `ECJ_JAR=/path/to/ecj-3.40.0.jar`. The script compiles resources and Java,
runs existing JVM checks, produces `SimplePrint-v0.10.apk`, and verifies its
signature and manifest. It creates or uses your local development key at
`$HOME/.android/debug.keystore`. Maintainers set `SIMPLEPRINT_KEYSTORE` to a
separately stored private key. Do not commit keystores or credentials.

The icon is a VectorDrawable made from DejaVu Sans Bold glyph outlines. Optional
regeneration requires Python and fontTools:

```sh
python3 tools/make_icon.py /path/to/DejaVuSans-Bold.ttf
```

## Testing and limitations

The owner confirmed printing on the Mudita Kompakt and X5H-2646 during development.
The 15 mm and 5 mm extra-feed experiments were excessive in hardware tests and
were removed in v0.9. See [TESTING.md](TESTING.md) for the validation record.

- v0.10 was compiled and signed; 1,833 existing JVM checks passed. The launcher
  border is removed; printing code is unchanged from v0.9. v0.10 has **not** been
  tested on the phone, in Katapult, or with the physical printer.
- Supports the tested printer's proprietary TinyPrint-family raster protocol
  over Bluetooth Classic SPP. It is not a general ESC/POS or BLE printer app.
- Requires Android 12/API 31 or newer and an already-paired Bluetooth device.
- Fixed 384-pixel print width. Other models and firmware are not verified.
- The editor shows formatting and a border indicator, but is not an exact
  paper-scale preview. Screen wrapping can differ from printed wrapping.
- Too many manual lines in 90° mode are rejected rather than cropped or scaled.
  One job is capped at 16,384 raster rows (roughly two metres at 200 dpi).
- Darkness/energy settings follow public protocol references, not a
  manufacturer-certified thermal limit. Darkness and cutter alignment can vary.
- “Sent to printer” means data was sent; the app cannot confirm a completed
  physical print. There is no printer status/flow-control negotiation.
- No saved document/history feature. Copy important text before leaving the app.
- Editing inside an existing formatted span and complex scripts/emoji have not
  received dedicated runtime testing.

## Version history

Every recovered version has a separate imported commit and version tag. This is
a reconstructed history from saved snapshots, not original development commits.
No original commit dates were invented. Source history starts at v0.2; v0.1 is
preserved as its original APK only. Old binaries are unchanged, with hashes
in `artifacts/`. Early APKs v0.1–v0.5 have no embedded version name/code, so their
filenames identify the historical versions. Do not use them as upgrade targets.

See [CHANGELOG.md](CHANGELOG.md). Signing keys and temporary files have been
removed from **every** imported source snapshot.

## License and attribution

Simple Print is released under the [Apache License 2.0](LICENSE). Protocol work
references [TiMini-Print](https://github.com/Dejniel/TiMini-Print) and
[tinyprint-x6h](https://github.com/nazarovmi/tinyprint-x6h). The icon uses DejaVu
Sans Bold. Upstream notices and license texts are in [NOTICE](NOTICE) and
`licenses/`. Gradle wrapper files retain their upstream notices. This project is
independent of Mudita, TinyPrint, and the printer manufacturer.
