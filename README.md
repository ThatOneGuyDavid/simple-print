# Simple Print proof of concept

This build prints typed or pasted text to an already-paired X5H-family printer. The default Print tab includes a live preview, with printer selection kept on a separate Printer tab.

## On the phone

1. Pair the printer in Android Bluetooth settings.
2. Install the APK.
3. Open **Simple Print** and grant the Bluetooth permission.
4. On the default Print tab, enter text and choose Small/Medium/Large, Regular/Bold, and Border.
5. Use **HIDE KEYBOARD** whenever needed and review the live preview.
6. Tap the compact **PRINT** button beside the settings.
7. Use the Printer tab to change or refresh the paired printer.

## Build

Open this directory in Android Studio and use **Build > Build APK(s)**, or run:

```sh
./gradlew assembleDebug
```

The APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## Protocol basis

The printer job uses the public TinyPrint-family command format: 384-pixel monochrome raster rows, Tiny RLE/raw line packets, CRC-8 framing, and Bluetooth Classic SPP. Protocol implementation is isolated in `TinyPrinter.java` so results from the first printer test can be applied without changing the UI or renderer.
