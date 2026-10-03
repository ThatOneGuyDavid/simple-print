# Simple Print 0.8

Minimal, offline, Android 12+ text printing for the Mudita Kompakt and the
X5H-2646. Package: `com.local.simpleprint`. No Internet permission or Google
services. The basic Bluetooth protocol was verified by the owner on v0.1.

## Changes in 0.8

- In 90° mode only, feeds an additional approximately 5 mm after the image
  (39 blank raster rows at the reference profile’s 200 dpi), before the existing
  final paper feed. Works with or without the printed border.
- Reduced from the 15 mm added in v0.7 following the owner’s cutter-alignment test.
- Retains the rounded-square SIMPLE PRINT icon from v0.7.

## Using it

1. Install `SimplePrint-v0.8.apk`. If necessary, uninstall the older app from
   the phone shell with `pm uninstall --user 0 com.local.simpleprint`.
2. Grant Nearby Devices/Bluetooth access and select a paired printer on the
   **Printer** tab. It is remembered.
3. On **Print**, choose Small (24 pixels), Medium (32), or Large (42).
   Large is the original Hello World size. Regular/Bold applies to new text.
4. Type or paste. Existing text keeps its sizes/styles.
5. Select Border and/or 90°, then press Print. Hide Keys dismisses the keyboard.

## Output modes

| Mode | Line breaks | Position on paper |
| --- | --- | --- |
| Normal + Border | Automatic wrapping and manual newlines | Bordered text block centered |
| Normal, no border | Automatic wrapping and manual newlines | 8-pixel left margin |
| 90° + Border | Manual newlines only | Complete block rotated clockwise and centered |
| 90°, no border | Manual newlines only | Rotated block starts 8 pixels from the left edge |

The text remains left-aligned inside its block before rotation. Border padding
is 12 printer pixels on each side. The printhead is 384 pixels wide.

In 90° mode, the editor scrolls horizontally and does not wrap. Long text lines
run along the paper feed direction after rotation. The stack of manually entered
lines must fit across the 384-pixel printhead (including margins and border).
Too many lines produces an explanation BEFORE Bluetooth connection or paper
movement. The app never silently shrinks or crops the design.

The phone's editor displays formatting and a border indicator, but is not an
exact paper-scale preview: screen width differs from printer width. It stays
upright for editing in 90° mode; only the printed output rotates.

Darkness is fixed at level 5/5. Energy is 9500, the value used by the public
X6H/X5H profile, instead of the earlier app's 8000. The previously suggested
11000 was not a verified model-specific limit and is not used.
Speed remains 10. No claim of a manufacturer-verified thermal maximum is made.

One raster job is limited to 16,384 rows to bound memory use on the phone
(roughly two metres of paper at 200 dpi). Over-limit jobs are rejected with a
message to split the print; this is not a wrap, crop, or scaling behavior.
Real-world darkness and long-print behavior still require a hardware test.

## Validation of this build

The SDK build completed successfully. All 3,814 JVM checks passed, covering
four-mode placement, fit/length limits, extra feed only in rotated mode, darkness/energy command values,
packet checksums, and exact reconstruction of mixed raw/compressed raster rows.
APK signature verification passed. The development signing key is unchanged.
The manifest reports version 0.8 (code 8), Android 12 minimum, and only the
Bluetooth connection permission. The icon was visually inspected.

Android UI/text drawing and actual printer output were not runtime-tested here.
Please check the new cutter alignment with a short 90° print on the Kompakt/X5H.

## Build and test

Android Studio / JDK 17 / Android SDK 35:

```sh
./gradlew assembleDebug
```

Or the dependency-light build path used for the delivered APK:

```sh
ANDROID_SDK_ROOT=/path/to/android-sdk bash tools/build-sdk.sh
```

Requires SDK platform 35, build-tools 35.0.0, Java, zip, keytool, and javac.
If only a JRE is available, set `ECJ_JAR=/path/to/ecj-3.40.0.jar`.
This script runs the JVM geometry/packet tests, compiles resources and classes,
produces `SimplePrint-v0.8.apk`, and verifies its signature and manifest.

Signing keys are excluded from this public snapshot. Local builds use a developer-owned key.

Source map: MainActivity (UI), TextRenderer (mixed-style layouts and rotation),
PrintGeometry (fit and placement), TinyPrinter (Bluetooth and protocol).

The icon is a small vector asset. Regenerate with `python3 tools/make_icon.py
/path/to/DejaVuSans-Bold.ttf` (fontTools required). Licenses are included.
