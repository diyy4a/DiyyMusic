# DiyyMusic

DiyyMusic is an open-source Android music client with a custom pink Liquid Glass interface and the proven playback, queue, library, lyrics, and streaming foundation derived from MetroList.

## Current version

**DiyyMusic 0.8.0** (`versionCode 13`)

## Implemented in the 0.8.0 source

- Online and local song actions now send to the real Media3/MetroList playback queue instead of mock UI state.
- Search, Home recommendations, albums, playlists, artists, recent songs, favorites, and downloads can start playback.
- Play/pause, previous, next, seek, volume, shuffle, repeat, queue selection, and retry playback are wired to `PlayerConnection`.
- Favorite actions persist through the Room database, including newly started online songs that were not inserted yet.
- Synced and plain lyrics can be fetched from the existing MetroList lyrics providers, displayed, auto-scrolled, and tapped to seek.
- The top-right song menu opens working Lyrics, Queue, Start Radio, Retry Playback, and Favorite actions.
- Profile stat cards open Playlists, Favorites, Downloads, and Recently Played directly.
- Account & Token, Appearance, Playback & Audio, Connected Services, and About open their own feature pages directly.
- Debug builds are unshrunk to avoid R8 removing playback/network provider behavior.
- Universal APK builds include `arm64-v8a` and `armeabi-v7a`.
- The v0.7 Liquid Glass UI, pink logo, modern icons, and clean sliders are retained.

## Build with Codemagic

Push the project to the `main` branch. Codemagic runs **DiyyMusic v0.8.0 APK** and publishes:

```text
DiyyMusic-v0.8.0-universal.apk
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

DiyyMusic is distributed under the GNU General Public License v3.0. The project preserves the license and attribution obligations of its upstream open-source foundation.
