# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 1.1.0** (`versionCode 32`)

## Changes in 1.1.0

- Fixed Codemagic compilation errors for missing `DISCORD_REDIRECT_URI` and `DISCORD_SOCIAL_SDK_ENABLED` BuildConfig fields.
- Kept the Discord Gateway Rich Presence implementation while retaining compatibility fields for older source overlays.
- Removed the two unused MetroList Wrapped images: `wrapped_playlistv1.png` and `wrapped_playlistv2.png`.
- Cleaned project documentation so `README.md` is the only Markdown file.
- Updated Codemagic and GitHub Actions artifact names to DiyyMusic 1.1.0.

## Discord build settings

The defaults are already included in `app/build.gradle.kts`. They can still be overridden through environment variables or `local.properties`:

```properties
DISCORD_APP_ID=1518124516893528125
DISCORD_REDIRECT_URI=http://127.0.0.1:6463/callback
DISCORD_SOCIAL_SDK_ENABLED=false
```

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v1.1.0 APK** workflow. The generated artifact is:

```text
DiyyMusic-v1.1.0-universal.apk
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
