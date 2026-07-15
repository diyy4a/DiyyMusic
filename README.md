# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 1.1.7** (`versionCode 39`)

## Changes in 1.1.7

- Added "Your Mixes" to the Home screen: a Spotify-Mix-style row of auto-generated, per-artist playlists built entirely from your local library (top artists by play time, each mix shuffles that artist's songs on tap) — no network calls involved.
- Redesigned the Equalizer into an actual graphic EQ: vertical faders per band (instead of a stacked list of horizontal sliders), a live frequency-response curve above them, and a "Reset to flat" button.
- Carried forward from 1.1.6: cached glass-card colors and skipped shadows on low-elevation list rows for smoother scrolling.
- Carried forward from 1.1.3: main-tab switching rebuilt on `AnimatedContent` for reliable slide direction, and the doubled mini-player border/shadow fix.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v1.1.7 APK** workflow. The generated artifact is:

```text
DiyyMusic-v1.1.7-universal.apk
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
