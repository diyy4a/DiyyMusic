# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 0.9.7** (`versionCode 28`)

## What changed in 0.9.7

- Removed the guessed instrumental markers from inline and full lyrics.
- Rich Presence can be switched on again after account linking; the preference is no longer forced back off when the Android transport is unavailable.
- Added a **Retry Rich Presence** action and clearer linked/transport status.
- Added a logout confirmation dialog and returned the Logout row to the normal DiyyMusic accent styling.
- Logout now clears the signed-in account identity and session before returning to Home, while local songs and downloads remain intact.
- Added **Smart Radio continuation**: when a playlist, album, or manual queue reaches its final track, DiyyMusic fetches a personalized recommendation mix from the signed-in YouTube Music session and continues playing automatically.
- Smart Radio filters tracks already present in the queue and appends up to 30 unique recommendations.

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

Push the project to the `main` branch and run the **DiyyMusic v0.9.7 APK** workflow. The generated artifact is:

```text
DiyyMusic-v0.9.7-universal.apk
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
