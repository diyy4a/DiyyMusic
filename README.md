# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 1.1.8** (`versionCode 40`)

## Changes in 1.1.8

- Added a "Mix" toggle in Player & Audio settings: automatic DJ-style blending between tracks. Turning it on takes over the track transition with a longer, smart crossfade and locks the manual Crossfade toggle/duration while it's active (they can't both be in control at once); turning Mix off hands control back to your manual Crossfade settings. Note: this is a scoped version — full BPM/key-matched beat-syncing like Spotify's AI DJ would need a proper audio-analysis engine, which is a much bigger undertaking than a settings toggle.
- Fixed a build error from the graphic-EQ update in 1.1.7 (`graphicsLayer` was imported from the wrong package for this project's Compose version).
- Carried forward from 1.1.7: "Your Mixes" home screen row (per-artist local playlists) and the redesigned graphic Equalizer with a live response curve.
- Carried forward from 1.1.3: main-tab switching rebuilt on `AnimatedContent` for reliable slide direction, and the doubled mini-player border/shadow fix.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v1.1.8 APK** workflow. The generated artifact is:

```text
DiyyMusic-v1.1.8-universal.apk
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
