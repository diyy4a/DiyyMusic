# DiyyMusic

DiyyMusic is an open-source Android music client with a modern pink interface and a MetroList-based playback foundation.

## Current release

**DiyyMusic 0.9.0** (`versionCode 21`)

## What changed in 0.9.0

- Rebuilt the Now Playing screen with a calmer layout, restrained controls, and lighter visual effects.
- Added smooth synced lyrics with centered auto-scroll, tap-to-seek, and word-level karaoke highlighting when timing data is available.
- Expanded Appearance settings with theme mode, accent strength, artwork shape, motion presets, reduced motion, glass tuning, background glow, and screenshot protection.
- Reorganized Player & Audio settings into clear streaming, crossfade, playback, audio behavior, and cache sections.
- Reduced expensive redraws by isolating playback progress and active lyric updates instead of refreshing the entire player screen.
- Limited glass shadows and decorative effects so the interface stays responsive on lower-end Android devices.
- Preserved the existing MetroList playback, queue, download, favorite, radio, and Discord foundations.

## Discord branding

The Discord application ID bundled for DiyyMusic is:

```text
1518124516893528125
```

The application name, icon, OAuth scopes, and redirect configuration are controlled from the Discord Developer Portal.

## Build with Codemagic

Push the project to the `main` branch and run the **DiyyMusic v0.9.0 APK** workflow. The generated artifact is:

```text
DiyyMusic-v0.9.0-universal.apk
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
