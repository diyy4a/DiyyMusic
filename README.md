# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink Liquid Glass interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 0.8.2** (`versionCode 15`)

## Highlights

- New approved DiyyMusic identity using a clean pink-red double musical note.
- Updated launcher, round launcher, themed monochrome icon, in-app brand mark, and startup splash artwork.
- Fixed Android resource linking for the splash background by using a proper color resource.
- Added the missing default-language `discord_information` string required by Android resource packaging.
- Recently Played on Home uses the same persistent history source as the full Recently Played screen.
- Lyrics parsing no longer crashes the app when malformed metadata or unsupported tags are received.
- Added native splash/loading presentation while the database and player connection initialize.
- Added song download actions, expanded Player & Audio and Appearance settings, account login controls, and Discord connection controls.
- Universal ARM builds include `arm64-v8a` and `armeabi-v7a`.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v0.8.2 APK** workflow. The generated artifact is:

```text
DiyyMusic-v0.8.2-universal.apk
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
