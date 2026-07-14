# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 1.1.6** (`versionCode 38`)

## Changes in 1.1.6

- Performance: the "liquid glass" card used for every row in every list (songs, albums, playlists, settings, etc.) was recomputing its gradient/border/highlight colors on every recomposition and always rendering a drop shadow — the single biggest cost on long lists since shadows force an extra compositing layer per row. Colors are now cached and only recalculated when the theme or glass settings actually change, and the shadow is skipped entirely for low-elevation rows (most list items) where it was barely visible anyway. This should noticeably reduce scroll jank on longer lists (library, search results, big playlists).
- Carried forward from 1.1.5: ~50 hand-drawn modern icons across common actions, settings, and media controls, plus removal of 46 unused drawable files.
- Carried forward from 1.1.3: main-tab switching rebuilt on `AnimatedContent` for reliable slide direction, and the doubled mini-player border/shadow fix.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v1.1.6 APK** workflow. The generated artifact is:

```text
DiyyMusic-v1.1.6-universal.apk
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
