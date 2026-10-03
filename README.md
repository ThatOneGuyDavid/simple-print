# Simple Print proof of concept

This build prints typed or pasted text to an already-paired X5H-family printer.

## On the phone

1. Pair the printer in Android Bluetooth settings.
2. Install the APK.
3. Open **Simple Print** and grant the Bluetooth permission.
4. Enter text, choose Small/Medium/Large and Regular/Bold.
5. Choose whether to print the rectangular border, then tap **PRINT**.

## Build

Open this directory in Android Studio and use **Build > Build APK(s)**, or run:

```sh
./gradlew assembleDebug
```

The APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## Protocol basis

The printer job uses the public TinyPrint-family command format: 384-pixel monochrome raster rows, Tiny RLE/raw line packets, CRC-8 framing, and Bluetooth Classic SPP. Protocol implementation is isolated in `TinyPrinter.java` so results from the first printer test can be applied without changing the UI or renderer.
