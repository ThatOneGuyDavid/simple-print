# Simple Print proof of concept

This build prints simply formatted text to an already-paired X5H-family printer. The default Print tab is a single WYSIWYG editor; printer selection is kept on a separate Printer tab.

## On the phone

1. Pair the printer in Android Bluetooth settings.
2. Install the APK.
3. Open **Simple Print** and grant the Bluetooth permission.
4. Choose Small/Medium/Large and Regular/Bold, then type. Formatting applies only to newly entered or pasted characters, so one print can mix styles.
5. Border applies to the complete print.
6. Use **HIDE KEYS** whenever needed and tap the compact **PRINT** button.
7. Use the Printer tab to change or refresh the paired printer.

When Border is enabled, it is visible in the editor. The printed border is fitted around the formatted text with equal padding on every side. Text remains left-justified inside the border, while the complete bordered block is centered across the paper.

## Build

Open this directory in Android Studio and use **Build > Build APK(s)**, or run:

```sh
./gradlew assembleDebug
```

The APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## Protocol basis

The printer job uses the public TinyPrint-family command format: 384-pixel monochrome raster rows, Tiny RLE/raw line packets, CRC-8 framing, and Bluetooth Classic SPP. Protocol implementation is isolated in `TinyPrinter.java` so results from the first printer test can be applied without changing the UI or renderer.
