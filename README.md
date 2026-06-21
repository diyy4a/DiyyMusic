# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 0.9.6** (`versionCode 27`)

## What changed in 0.9.6

- Rebuilt the Now Playing layout so the artwork uses the available height instead of leaving a dead empty block.
- Grouped favorite, lyrics, and queue directly below the song information.
- Reorganized progress and playback controls into one balanced bottom dock.
- Removed karaoke-style word filling from inline and full lyrics.
- Added a small timed progress indicator only for detected instrumental gaps.
- Smoothed lyric tracking and scrolling while keeping tap-to-seek in full lyrics.
- Reworked Search into separate Top Result, Songs, Artists, Albums, and Playlists sections.
- Sanitized generic creator labels such as `Song`, `Video`, and `Music` so they are not displayed as artist names.
- Loaded the signed-in account avatar in the Library header.
- Removed the OAuth redirect instructions from the Discord screen and stopped presenting a saved toggle as a working mobile Rich Presence connection.

## Discord account linking and Rich Presence

The bundled Discord application ID is:

```text
1518124516893528125
```

Account linking uses the standard OAuth `identify` scope and this callback:

```text
http://127.0.0.1:6463/callback
```

Enable **Public Client** and register the exact callback in the Discord Developer Portal.

Android activity publishing is intentionally disabled unless an approved Discord Social SDK transport is integrated into the build. OAuth account linking alone cannot publish the currently playing song. See `DISCORD-SETUP.md`.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v0.9.6 APK** workflow. The generated artifact is:

```text
DiyyMusic-v0.9.6-universal.apk
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
