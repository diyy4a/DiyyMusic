# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 0.9.8** (`versionCode 29`)

## What changed in 0.9.8

- Fixed the Codemagic/GitHub FOSS compilation error `Unresolved reference: STATE_ENDED`.
- Smart Radio continuation now checks the Media3 state with `Player.STATE_ENDED`.
- Playback behavior remains the same as 0.9.7; this is a focused build hotfix.

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

Push the project to the `main` branch and run the **DiyyMusic v0.9.8 APK** workflow. The generated artifact is:

```text
DiyyMusic-v0.9.8-universal.apk
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
