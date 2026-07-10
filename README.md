# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 1.1.2** (`versionCode 34`)

## Changes in 1.1.2

- Fixed the mini-player and bottom nav bar being hidden on every screen except the 4 main tabs (playlist/album/artist/settings screens, etc.) — they now stay visible everywhere except the full player screen.
- Fixed a leftover empty gap at the bottom of the full player screen (Scaffold was still reserving space for the mini-player/nav bar after it was hidden).
- Main tab switches (Home / Search / Library / Profile) now slide from the correct side depending on the direction you're navigating (e.g. Search → Home slides in from the left), instead of always sliding in from the right; detail screens keep the vertical push motion.
- Made the startup splash fade out smoothly instead of cutting instantly to the main screen.
- Reworked the "liquid glass" card colors across every theme (light, dark, and pure black) so the effect is clearly visible, with a stronger tinted border and a thin top-edge highlight for a more distinct glass look.
- Fixed the full-screen Lyrics view (and its playback controls) being hardcoded to a dark look regardless of the app's theme; it now follows the selected theme like the rest of the app.
- Added a startup check against GitHub Releases: if a newer DiyyMusic version is published, a dialog offers to open the download, and "Later" won't ask again for that same version.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v1.1.2 APK** workflow. The generated artifact is:

```text
DiyyMusic-v1.1.2-universal.apk
```

## Build locally

Requirements: Java 21 and Android SDK 37.

```bash
chmod +x gradlew
./gradlew :app:assembleFossDebug
```

The APK is generated under:

```text
app/build/outputs/apk/foss/debug/
```

## License

DiyyMusic is distributed under the GNU General Public License v3.0 and preserves the license and attribution requirements of its upstream open-source foundation.
