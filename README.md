# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 1.1.5** (`versionCode 37`)

## Changes in 1.1.5

- Redrew ~50 more icons by hand as new vector drawables in the same rounded, modern style (bottom nav, mini-player, and playback controls were already done in 1.1.4): common actions (close, check, add, delete, edit, share, download/upload, refresh), navigation (back/forward, chevrons), account & settings (lock, key, settings, info, history, logout/login, star), media & audio (equalizer, volume, mic, cast, lyrics, queue, playlist, library), and a batch of misc icons (album, artist, cloud, palette, security, storage, timer, trending up, and more).
- Cleaned up the icon set: deleted 46 drawable files that were never referenced anywhere in the app (leftover duplicates and dead assets from earlier iterations), instead of leaving unused files lying around.
- Carried forward from 1.1.4: transparent mini-player/nav background, redesigned startup splash, and the full-lyrics playback dock correctly sized to match the real player screen.
- Carried forward from 1.1.3: main-tab switching rebuilt on `AnimatedContent` for reliable slide direction, and the doubled mini-player border/shadow fix.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v1.1.5 APK** workflow. The generated artifact is:

```text
DiyyMusic-v1.1.5-universal.apk
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
