# Validation record

## Hardware evidence from development

- The owner reported that the original Hello World APK printed successfully on
  the Mudita Kompakt with the X5H-2646.
- The owner subsequently used the formatting/editor versions and requested
  successive changes to the UI and printed border.
- After testing v0.6, the owner requested extra 90° feed and a rounded icon.
- The owner reported that v0.7's 15 mm extra feed was excessive, and then that
  v0.8's 5 mm extra feed was also excessive. v0.9 removed that addition.
- No explicit final cutter-alignment confirmation for v0.9 is recorded.

These are owner observations, not a laboratory test matrix or a compatibility
claim for other printers.

## v0.10 preparation

- Compiled actual source with Android SDK 35/build-tools 35.0.0, Java 17 runtime,
  and Eclipse Java compiler 3.40.0 using `tools/build-sdk.sh`.
- Existing JVM suite: **1,833 checks passed**. Checks exercise four-mode geometry,
  fit/length limits, command values, packet CRCs, and decoding of raw/RLE rows.
  This number counts assertions, not 1,833 independent test cases.
- APK signature verification passed. Manifest: package `com.local.simpleprint`,
  version 0.10/code 10, minSdk 31, only `BLUETOOTH_CONNECT` permission.
- Icon XML comparison with v0.9: only the border path removed; background and
  lettering paths preserved. No Java app source differs from v0.9.
- Public source build uses an external private signing key for the distributed
  APK; no signing keys are present in Git history or release source.

No Android emulator, UI instrumentation, physical print, or Katapult launcher
test was run for v0.10. Gradle/Android Studio builds were not exercised in this
release preparation. Archived APKs are original development outputs, not rebuilds.
