# Version history

Imported from saved development snapshots. These entries describe features, not
an assertion that every version received a complete hardware test.

| Version | Change |
| --- | --- |
| 0.1 | Hello World proof of concept; original APK retained, original source unavailable. Owner confirmed successful printing. |
| 0.2 | Three text sizes, regular/bold, optional border. |
| 0.3 | Print and Printer tabs, live preview, hide-keyboard control. |
| 0.4 | Single mixed-format editor; settings affect new characters. |
| 0.5 | Border indicator in editor; equal padding and centered bordered block. |
| 0.6 | Darkness 5/5, energy 9500, border-dependent placement, manual-wrap 90° mode, text icon. |
| 0.7 | Rounded-square icon and approximately 15 mm extra feed in 90° mode. Feed proved excessive. |
| 0.8 | Reduced extra 90° feed to approximately 5 mm. Still excessive in the owner's test. |
| 0.9 | Removed extra rotated feed; restored v0.6 printer protocol behavior. |
| 0.10 | Removed the icon outline at the owner's request for Katapult. Printing code unchanged. |

Public import excludes signing keys, build caches, temporary outputs, and personal
information. Historical Gradle signing overrides were removed and SDK scripts
were changed to use a local developer key by default. These privacy/build
adjustments do not change historical app code or preserved APKs.
