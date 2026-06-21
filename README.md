# DiyyMusic

DiyyMusic is an open-source Android music client with a custom pink Liquid Glass interface and the playback, queue, library, lyrics, download, and streaming foundation derived from MetroList.

## Current version

**DiyyMusic 0.8.1** (`versionCode 14`)

## Changes in 0.8.1

- Home Recently Played now reads the same Room event history as the full history page, so valid recent songs no longer disappear from Home.
- The lyrics parser no longer crashes on provider tags such as `{agent:...}` or malformed lyric text.
- Added a native launch splash and an in-app logo loading screen to replace the blank white startup gap.
- Added Download, Cancel Download, and Remove Download actions to the full player and song options sheet.
- Added normal Google/YouTube Music login through the MetroList WebView flow. Manual cookie fields remain under Advanced.
- Added Discord Connect, Disconnect, account status, avatar, and Rich Presence controls using the existing OAuth/RPC backend.
- Expanded Player and Audio settings with streaming quality, crossfade duration, gapless handoff, queue persistence, autoplay, normalization, silence skipping, audio offload, Bluetooth resume, and related controls.
- Expanded Appearance settings with System/Light/Dark theme selection, pure black mode, refresh rate, Liquid Glass outline, compact player artwork, and screenshot protection.
- Universal APK builds include `arm64-v8a` and `armeabi-v7a`.

## Build with Codemagic

Push the project to the `main` branch. Codemagic runs **DiyyMusic v0.8.1 APK** and publishes:

```text
DiyyMusic-v0.8.1-universal.apk
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
